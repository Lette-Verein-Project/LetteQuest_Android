/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mia.lette.com.museum.zxing.client.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import mia.lette.com.museum.QR_Code;
import mia.lette.com.museum.Quest;
import mia.lette.com.museum.QuestActivity;
import mia.lette.com.museum.R;
import mia.lette.com.museum.zxing.BarcodeFormat;
import mia.lette.com.museum.zxing.Result;
import mia.lette.com.museum.zxing.ResultMetadataType;
import mia.lette.com.museum.zxing.ResultPoint;
import mia.lette.com.museum.zxing.client.android.camera.CameraManager;
import mia.lette.com.museum.zxing.client.android.result.ResultHandler;
import mia.lette.com.museum.zxing.client.android.result.ResultHandlerFactory;
import mia.lette.com.museum.zxing.qrcode.encoder.QRCode;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener {

  private static final String TAG = CaptureActivity.class.getSimpleName();

  private SharedPreferences pref;

  private static final int SHARE_ID = Menu.FIRST;
  private static final int HISTORY_ID = Menu.FIRST + 1;
  private static final int SETTINGS_ID = Menu.FIRST + 2;
  private static final int HELP_ID = Menu.FIRST + 3;
  private static final int ABOUT_ID = Menu.FIRST + 4;

  private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;
  private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

  private static final String PACKAGE_NAME = "com.google.zxing.client.android";
  private static final String PRODUCT_SEARCH_URL_PREFIX = "http://www.google";
  private static final String PRODUCT_SEARCH_URL_SUFFIX = "/m/products/scan";
  private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };
  private static final String RETURN_CODE_PLACEHOLDER = "{CODE}";
  private static final String RETURN_URL_PARAM = "ret";

    boolean kameraStatus = true;

  public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

  private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES =
      EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
                 ResultMetadataType.SUGGESTED_PRICE,
                 ResultMetadataType.ERROR_CORRECTION_LEVEL,
                 ResultMetadataType.POSSIBLE_COUNTRY);

  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private Result savedResultToShow;
  private ViewfinderView viewfinderView;
  private TextView statusView;
  private View resultView;
  private Result lastResult;
  private boolean hasSurface;
  private boolean copyToClipboard;
  private IntentSource source;
  private String sourceUrl;
  private String returnUrlTemplate;
  private Collection<BarcodeFormat> decodeFormats;
  private String characterSet;
  private String versionName;
  private InactivityTimer inactivityTimer;
  private BeepManager beepManager;
  ArrayList<Quest> myDataP = null;
  ArrayList<Quest> myDataT = null;


  private final DialogInterface.OnClickListener aboutListener =
      new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.zxing_url)));
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      startActivity(intent);
    }
  };

  ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public Handler getHandler() {
    return handler;
  }

  CameraManager getCameraManager() {
    return cameraManager;
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);





    myDataP = (ArrayList<Quest>) this.getIntent().getSerializableExtra("pPassQ");
    myDataT = (ArrayList<Quest>) this.getIntent().getSerializableExtra("tPassQ");
    if(myDataP == null) Log.i("Hallo", "myDataP: null");
    else Log.i("Hallo", "myDataP: " + myDataP.size());

    if(myDataT == null) Log.i("Hallo", "myDataT: null");
    else Log.i("Hallo", "myDataT: " + myDataT.size());

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_einscann);



    hasSurface = false;
    inactivityTimer = new InactivityTimer(this);
    beepManager = new BeepManager(this);

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


  }

  @Override
  protected void onResume() {
    super.onResume();

    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
    // want to open the camera driver and measure the screen size if we're going to show the help on
    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
    // off screen.
    cameraManager = new CameraManager(getApplication());
    viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
    viewfinderView.setCameraManager(cameraManager);

      resultView = findViewById(R.id.result_view);
      statusView = (TextView) findViewById(R.id.status_view);

    handler = null;
    lastResult = null;

    resetStatusView();

    SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    beepManager.updatePrefs();

    inactivityTimer.onResume();

    Intent intent = getIntent();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    source = IntentSource.NONE;
    decodeFormats = null;
    characterSet = null;

    if (intent != null) {

      String action = intent.getAction();
      String dataString = intent.getDataString();

      if (Intents.Scan.ACTION.equals(action)) {

        // Scan the formats the intent requested, and return the result to the calling activity.
        source = IntentSource.NATIVE_APP_INTENT;
        decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);

        if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
          int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
          int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
          if (width > 0 && height > 0) {
            cameraManager.setManualFramingRect(width, height);
          }
        }
        
        String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
        if (customPromptMessage != null) {
          statusView.setText(customPromptMessage);
        }

      } else if (dataString != null &&
                 dataString.contains(PRODUCT_SEARCH_URL_PREFIX) &&
                 dataString.contains(PRODUCT_SEARCH_URL_SUFFIX)) {

        // Scan only products and send the result to mobile Product Search.
        source = IntentSource.PRODUCT_SEARCH_LINK;
        sourceUrl = dataString;
        decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;

      } else if (isZXingURL(dataString)) {

        // Scan formats requested in query string (all formats if none specified).
        // If a return URL is specified, send the results there. Otherwise, handle it ourselves.
        source = IntentSource.ZXING_LINK;
        sourceUrl = dataString;
        Uri inputUri = Uri.parse(sourceUrl);
        returnUrlTemplate = inputUri.getQueryParameter(RETURN_URL_PARAM);
        decodeFormats = DecodeFormatManager.parseDecodeFormats(inputUri);

      }

      characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

    }
  }
  
  private static boolean isZXingURL(String dataString) {
    if (dataString == null) {
      return false;
    }
    for (String url : ZXING_URLS) {
      if (dataString.startsWith(url)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }
    inactivityTimer.onPause();
    cameraManager.closeDriver();
    if (!hasSurface) {
      SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
      SurfaceHolder surfaceHolder = surfaceView.getHolder();
      surfaceHolder.removeCallback(this);
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    inactivityTimer.shutdown();
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (source == IntentSource.NATIVE_APP_INTENT) {
        setResult(RESULT_CANCELED);
        finish();
        return true;
      } else if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
        restartPreviewAfterDelay(0L);
        return true;
      }
    } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
      // Handle these events so they don't launch the Camera app
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(Menu.NONE, SHARE_ID, Menu.NONE, R.string.menu_share)
        .setIcon(android.R.drawable.ic_menu_share);
    menu.add(Menu.NONE, HISTORY_ID, Menu.NONE, R.string.menu_history)
        .setIcon(android.R.drawable.ic_menu_recent_history);
    menu.add(Menu.NONE, SETTINGS_ID, Menu.NONE, R.string.menu_settings)
        .setIcon(android.R.drawable.ic_menu_preferences);

    return true;
  }

  // Don't display the share menu item if the result overlay is showing.
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.findItem(SHARE_ID).setVisible(lastResult == null);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    switch (item.getItemId()) {
      case SETTINGS_ID:
        intent.setClassName(this, PreferencesActivity.class.getName());
        startActivity(intent);
        break;
      case ABOUT_ID:
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_about) + versionName);
        builder.setMessage(getString(R.string.msg_about) + "\n\n" + getString(R.string.zxing_url));
        builder.setIcon(R.drawable.launcher_icon);
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.show();
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (resultCode == RESULT_OK) {
      if (requestCode == HISTORY_REQUEST_CODE) {
        int itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER, -1);
        if (itemNumber >= 0) {
        }
      }
    }
  }

  private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
    // Bitmap isn't used yet -- will be used soon
    if (handler == null) {
      savedResultToShow = result;
    } else {
      if (result != null) {
        savedResultToShow = result;
      }
      if (savedResultToShow != null) {
        Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
        handler.sendMessage(message);
      }
      savedResultToShow = null;
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (holder == null) {
      Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
    }
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  /**
   * A valid barcode has been found, so give an indication of success and show the results.
   *
   * @param rawResult The contents of the barcode.
   * @param barcode   A greyscale bitmap of the camera data which was decoded.
   */
  public void handleDecode(Result rawResult, Bitmap barcode) {
    inactivityTimer.onActivity();
    lastResult = rawResult;
    ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);

    if (barcode == null) {
      // This is from history -- no saved barcode
      handleDecodeInternally(rawResult, resultHandler, null);
    } else {
      beepManager.playBeepSoundAndVibrate();
      drawResultPoints(barcode, rawResult);
      switch (source) {
        case NATIVE_APP_INTENT:
        case PRODUCT_SEARCH_LINK:
          handleDecodeExternally(rawResult, resultHandler, barcode);
          break;
        case ZXING_LINK:
          if (returnUrlTemplate == null){
            handleDecodeInternally(rawResult, resultHandler, barcode);
          } else {
            handleDecodeExternally(rawResult, resultHandler, barcode);
          }
          break;
        case NONE:
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
          if (prefs.getBoolean(PreferencesActivity.KEY_BULK_MODE, false)) {
            Toast.makeText(this, R.string.msg_bulk_mode_scanned, Toast.LENGTH_SHORT).show();
            // Wait a moment or else it will scan the same barcode continuously about 3 times
            restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
          } else {
            handleDecodeInternally(rawResult, resultHandler, barcode);
          }
          break;
      }
    }
  }

  /**
   * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
   *
   * @param barcode   A bitmap of the captured image.
   * @param rawResult The decoded results which contains the points to draw.
   */
  private void drawResultPoints(Bitmap barcode, Result rawResult) {
    ResultPoint[] points = rawResult.getResultPoints();
    if (points != null && points.length > 0) {
      Canvas canvas = new Canvas(barcode);
      Paint paint = new Paint();
      paint.setColor(getResources().getColor(R.color.result_image_border));
      paint.setStrokeWidth(3.0f);
      paint.setStyle(Paint.Style.STROKE);
      Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
      canvas.drawRect(border, paint);

      paint.setColor(getResources().getColor(R.color.result_points));
      if (points.length == 2) {
        paint.setStrokeWidth(4.0f);
        drawLine(canvas, paint, points[0], points[1]);
      } else if (points.length == 4 &&
                 (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                  rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
        // Hacky special case -- draw two lines, for the barcode and metadata
        drawLine(canvas, paint, points[0], points[1]);
        drawLine(canvas, paint, points[2], points[3]);
      } else {
        paint.setStrokeWidth(10.0f);
        for (ResultPoint point : points) {
          canvas.drawPoint(point.getX(), point.getY(), paint);
        }
      }
    }
  }

  private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b) {
    canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
  }

  // Put up our own UI for how to handle the decoded contents.
  private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
    //statusView.setVisibility(View.GONE);
    //viewfinderView.setVisibility(View.GONE);
    //resultView.setVisibility(View.VISIBLE);


    CharSequence displayContents = resultHandler.getDisplayContents();
    CharSequence a = resultHandler.getType().toString();
    Log.d("haha","der Test des QRCodes" + a);
      //To do:
    String frage = ""+displayContents.charAt(2) + displayContents.charAt(3) ;
    int fragezahl = 1;
      parsingUeberpruefung(displayContents);
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      if(prefs.getBoolean("ueberpruefung",false) == false){
          Log.d("parsingFehlrt","Why");
          return;
      }
      ueberpruefung("" + displayContents.charAt(0), fragezahl, (int) displayContents.charAt(4) - 48, (int) displayContents.charAt(6) - 48);



    QR_Code qrCode = new QR_Code(""+displayContents.charAt(0),fragezahl, (int) displayContents.charAt(4) -48, (int) displayContents.charAt(6) -48);

      Intent fragenScreen = new Intent(this, QuestActivity.class);
      fragenScreen.putExtra("pPassQ", myDataP);
      fragenScreen.putExtra("tPassQ", myDataT);
      pref = getSharedPreferences("questDaten",MODE_APPEND);
      pref.edit().putString("code", qrCode.getFragentyp()).commit();
      pref.edit().putInt("frage", qrCode.getFrage()).commit();
      startActivity(fragenScreen);

    // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
    int scaledSize = Math.max(22, 32 - displayContents.length() / 4);

    TextView supplementTextView = (TextView) findViewById(R.id.contents_supplement_text_view);
    supplementTextView.setText("");
    supplementTextView.setOnClickListener(null);
    if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
        PreferencesActivity.KEY_SUPPLEMENTAL, true)) {
    }

    int buttonCount = resultHandler.getButtonCount();
    ViewGroup buttonView = (ViewGroup) findViewById(R.id.result_button_view);
    buttonView.requestFocus();
    for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
      TextView button = (TextView) buttonView.getChildAt(x);

    }

    /*if (copyToClipboard && !resultHandler.areContentsSecure()) {
      ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
      clipboard.setText(displayContents);
    }*/
  }

  // Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
  private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
    viewfinderView.drawResultBitmap(barcode);

    // Since this message will only be shown for a second, just tell the user what kind of
    // barcode was found (e.g. contact info) rather than the full contents, which they won't
    // have time to read.
    statusView.setText(getString(resultHandler.getDisplayTitle()));

   /*if (copyToClipboard && !resultHandler.areContentsSecure()) {
      ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
      clipboard.setText(resultHandler.getDisplayContents());
    }*/

    if (source == IntentSource.NATIVE_APP_INTENT) {
      
      // Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
      // the deprecated intent is retired.
      Intent intent = new Intent(getIntent().getAction());
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      intent.putExtra(Intents.Scan.RESULT, rawResult.toString());
      intent.putExtra(Intents.Scan.RESULT_FORMAT, rawResult.getBarcodeFormat().toString());
      byte[] rawBytes = rawResult.getRawBytes();
      if (rawBytes != null && rawBytes.length > 0) {
        intent.putExtra(Intents.Scan.RESULT_BYTES, rawBytes);
      }
      Map<ResultMetadataType,?> metadata = rawResult.getResultMetadata();
      if (metadata != null) {
        Integer orientation = (Integer) metadata.get(ResultMetadataType.ORIENTATION);
        if (orientation != null) {
          intent.putExtra(Intents.Scan.RESULT_ORIENTATION, orientation.intValue());
        }
        String ecLevel = (String) metadata.get(ResultMetadataType.ERROR_CORRECTION_LEVEL);
        if (ecLevel != null) {
          intent.putExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL, ecLevel);
        }
        Iterable<byte[]> byteSegments = (Iterable<byte[]>) metadata.get(ResultMetadataType.BYTE_SEGMENTS);
        if (byteSegments != null) {
          int i = 0;
          for (byte[] byteSegment : byteSegments) {
            intent.putExtra(Intents.Scan.RESULT_BYTE_SEGMENTS_PREFIX + i, byteSegment);
            i++;
          }
        }
      }
      sendReplyMessage(R.id.return_scan_result, intent);
      
    } else if (source == IntentSource.PRODUCT_SEARCH_LINK) {
      
      // Reformulate the URL which triggered us into a query, so that the request goes to the same
      // TLD as the scan URL.
      int end = sourceUrl.lastIndexOf("/scan");
      String replyURL = sourceUrl.substring(0, end) + "?q=" + resultHandler.getDisplayContents() + "&source=zxing";      
      sendReplyMessage(R.id.launch_product_query, replyURL);
      
    } else if (source == IntentSource.ZXING_LINK) {
      
      // Replace each occurrence of RETURN_CODE_PLACEHOLDER in the returnUrlTemplate
      // with the scanned code. This allows both queries and REST-style URLs to work.
      if (returnUrlTemplate != null) {
        String codeReplacement = String.valueOf(resultHandler.getDisplayContents());
        try {
          codeReplacement = URLEncoder.encode(codeReplacement, "UTF-8");
        } catch (UnsupportedEncodingException e) {
          // can't happen; UTF-8 is always supported. Continue, I guess, without encoding
        }
        String replyURL = returnUrlTemplate.replace(RETURN_CODE_PLACEHOLDER, codeReplacement);
        sendReplyMessage(R.id.launch_product_query, replyURL);
      }
      
    }
  }
  
  private void sendReplyMessage(int id, Object arg) {
    Message message = Message.obtain(handler, id, arg);
    long resultDurationMS = getIntent().getLongExtra(Intents.Scan.RESULT_DISPLAY_DURATION_MS,
                                                     DEFAULT_INTENT_RESULT_DURATION_MS);
    if (resultDurationMS > 0L) {
      handler.sendMessageDelayed(message, resultDurationMS);
    } else {
      handler.sendMessage(message);
    }
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    try {
      cameraManager.openDriver(surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (handler == null) {
        handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
      }
      decodeOrStoreSavedBitmap(null, null);
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      displayFrameworkBugMessageAndExit();
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializing camera", e);
      displayFrameworkBugMessageAndExit();
    }
  }

  private void displayFrameworkBugMessageAndExit() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.app_name));
    builder.setMessage(getString(R.string.msg_camera_framework_bug));

    builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }

  public void restartPreviewAfterDelay(long delayMS) {
    if (handler != null) {
      handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
    }
    resetStatusView();
  }

  private void resetStatusView() {
    resultView.setVisibility(View.GONE);
    statusView.setText(R.string.msg_default_status);
    statusView.setVisibility(View.VISIBLE);
    viewfinderView.setVisibility(View.VISIBLE);
    lastResult = null;
  }

  public void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }

  @Override
  public void onClick(View v) {
      if(kameraStatus = true){
          cameraManager.stopPreview();
          kameraStatus = false;
      }else if(kameraStatus =false){
cameraManager.startPreview();
          kameraStatus = true;
      }

  }

  /**
   * �berpr�ft ob die eingescannten daten zu gebrauchen sind
   * @param fragentyp
   * @param frage
   * @param raum
   * @param stockwerk
   * @return
   */
  public void ueberpruefung(String fragentyp,int frage,int raum,int stockwerk){
      Log.d("pruefen","tada" + fragentyp);
      if(fragentyp.equals("p") || fragentyp.equals("t")) {

          if (fragentyp.equals("p")) {
              pref = getSharedPreferences("questDaten", MODE_APPEND);
              pref.edit().putBoolean("prefAuslesen", false).commit();
              SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
              prefs.edit().putBoolean("ueberpruefung",true).commit();
              for (int i = 0; i == 99; ) {
                  if (frage == i) {
                      i = 96;
                  }
                  i += 3;
              }
          }
          if (fragentyp.equals("t")) {
              pref = getSharedPreferences("questDaten", MODE_APPEND);
              pref.edit().putBoolean("prefAuslesen", true).commit();
              SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
              prefs.edit().putBoolean("ueberpruefung",true).commit();
          }
      }
    else {
      qrCodestimmtNicht();
    }
  }


  public void parsingUeberpruefung(CharSequence displayText){
if(displayText.length() != 9) {
    Log.d("charFehler", "tada");
    qrCodestimmtNicht();
    return;
}

    for (int t = 0; t < 9; t++) {
        Log.d("charFehler", "wieso");
        if (displayText.charAt(t) == '\u0000') {
            Log.d("charFehler", "wirklich");
            qrCodestimmtNicht();
            return;
        }
    }
    if (displayText.charAt(2) == ' ' || displayText.charAt(5) == ' ' || displayText.charAt(8) == ' ') {
        Log.d("parsingFehlrt", "Aha");
        qrCodestimmtNicht();
        return;
    }
    String frage = "" + displayText.charAt(2) + displayText.charAt(3);
    String stockwerk = "" + displayText.charAt(5) + displayText.charAt(6);
    String raum = "" + displayText.charAt(8) + displayText.charAt(9);
    int zahl = 1;
    Log.d("parsingFehlrt", "" + displayText.charAt(2));
    if (displayText.charAt(2) == ' ' || displayText.charAt(5) == ' ' || displayText.charAt(8) == ' ') {
        Log.d("parsingFehlrt", "Aha");
        qrCodestimmtNicht();
        return;
    }
    try {
        zahl = Integer.parseInt(frage);
    } catch (NumberFormatException e) {
        Log.d("parsingFehlrt", "Die Frage");
        qrCodestimmtNicht();
        return;
    }
    try {
        zahl = Integer.parseInt(stockwerk);
    } catch (NumberFormatException e) {
        Log.d("parsingFehlrt", "Das Stockwerk");
        qrCodestimmtNicht();
        return;
    }
    try {
        zahl = Integer.parseInt(raum);
    } catch (NumberFormatException e) {
        Log.d("parsingFehlrt", "Der Raum");
        qrCodestimmtNicht();
        return;
    }

      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      prefs.edit().putBoolean("ueberpruefung",true).commit();

  }

  public void qrCodestimmtNicht(){
    Context context = getApplicationContext();
    CharSequence text = "Bitte nur die Qr Codes aus dem Ephraim Palais benutzen";
    int duration = Toast.LENGTH_LONG;
    Toast toast = Toast.makeText(context, text, duration);
    toast.show();
    onPause();
    Runnable r = new Runnable() {
      @Override
      public void run() {

      }
    };
    Handler mHandler = new Handler();
    mHandler.postDelayed(r, 100);
    onResume();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    prefs.edit().putBoolean("ueberpruefung",false).commit();
  }
}
