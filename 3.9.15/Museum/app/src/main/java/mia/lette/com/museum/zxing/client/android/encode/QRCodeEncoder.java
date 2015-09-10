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

package mia.lette.com.museum.zxing.client.android.encode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import mia.lette.com.museum.zxing.BarcodeFormat;
import mia.lette.com.museum.zxing.EncodeHintType;
import mia.lette.com.museum.zxing.MultiFormatWriter;
import mia.lette.com.museum.zxing.Result;
import mia.lette.com.museum.zxing.WriterException;
import mia.lette.com.museum.zxing.client.android.Contents;
import mia.lette.com.museum.zxing.client.android.Intents;
import mia.lette.com.museum.zxing.client.android.R;
import mia.lette.com.museum.zxing.client.result.ParsedResult;
import mia.lette.com.museum.zxing.client.result.ResultParser;
import mia.lette.com.museum.zxing.common.BitMatrix;

/**
 * This class does the work of decoding the user's request and extracting all the data
 * to be encoded in a barcode.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class QRCodeEncoder {

  private static final String TAG = QRCodeEncoder.class.getSimpleName();

  private static final int WHITE = 0xFFFFFFFF;
  private static final int BLACK = 0xFF000000;

  private final Activity activity;
  private String contents;
  private String displayContents;
  private String title;
  private BarcodeFormat format;
  private final int dimension;
  private final boolean useVCard;

  QRCodeEncoder(Activity activity, Intent intent, int dimension, boolean useVCard) throws WriterException {
    this.activity = activity;
    this.dimension = dimension;
    this.useVCard = useVCard;
    String action = intent.getAction();
    if (action.equals(Intents.Encode.ACTION)) {
      encodeContentsFromZXingIntent(intent);
    } else if (action.equals(Intent.ACTION_SEND)) {
      encodeContentsFromShareIntent(intent);
    }
  }

  String getContents() {
    return contents;
  }

  String getDisplayContents() {
    return displayContents;
  }

  String getTitle() {
    return title;
  }

  boolean isUseVCard() {
    return useVCard;
  }

  // It would be nice if the string encoding lived in the core ZXing library,
  // but we use platform specific code like PhoneNumberUtils, so it can't.
  private boolean encodeContentsFromZXingIntent(Intent intent) {
     // Default to QR_CODE if no format given.
    String formatString = intent.getStringExtra(Intents.Encode.FORMAT);
    format = null;
    if (formatString != null) {
      try {
        format = BarcodeFormat.valueOf(formatString);
      } catch (IllegalArgumentException iae) {
        // Ignore it then
      }
    }
    if (format == null || format == BarcodeFormat.QR_CODE) {
      String type = intent.getStringExtra(Intents.Encode.TYPE);
      if (type == null || type.length() == 0) {
        return false;
      }
      this.format = BarcodeFormat.QR_CODE;
      encodeQRCodeContents(intent, type);
    } else {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    }
    return contents != null && contents.length() > 0;
  }

  // Handles send intents from multitude of Android applications
  private void encodeContentsFromShareIntent(Intent intent) throws WriterException {
    // Check if this is a plain text encoding, or contact
    if (intent.hasExtra(Intent.EXTRA_TEXT)) {
      encodeContentsFromShareIntentPlainText(intent);
    } else {
      // Attempt default sharing.
      encodeContentsFromShareIntentDefault(intent);
    }
  }

  private void encodeContentsFromShareIntentPlainText(Intent intent) throws WriterException {
    // Notice: Google Maps shares both URL and details in one text, bummer!
    //String theContents = ContactEncoder.trim(intent.getStringExtra(Intent.EXTRA_TEXT));
    // We only support non-empty and non-blank texts.
    // Trim text to avoid URL breaking.
      throw new WriterException("Empty EXTRA_TEXT");
    }
  // Handles send intents from the Contacts app, retrieving a contact as a VCARD.
  private void encodeContentsFromShareIntentDefault(Intent intent) throws WriterException {
    format = BarcodeFormat.QR_CODE;
    Bundle bundle = intent.getExtras();
    if (bundle == null) {
      throw new WriterException("No extras");
    }
    Uri uri = (Uri) bundle.getParcelable(Intent.EXTRA_STREAM);
    if (uri == null) {
      throw new WriterException("No EXTRA_STREAM");
    }
    byte[] vcard;
    String vcardString;
    try {
      InputStream stream = activity.getContentResolver().openInputStream(uri);
      int length = stream.available();
      if (length <= 0) {
        throw new WriterException("Content stream is empty");
      }
      vcard = new byte[length];
      int bytesRead = stream.read(vcard, 0, length);
      if (bytesRead < length) {
        throw new WriterException("Unable to fully read available bytes from content stream");
      }
      vcardString = new String(vcard, 0, bytesRead, "UTF-8");
    } catch (IOException ioe) {
      throw new WriterException(ioe);
    }
    Log.d(TAG, "Encoding share intent content:");
    Log.d(TAG, vcardString);
    Result result = new Result(vcardString, vcard, null, BarcodeFormat.QR_CODE);
    ParsedResult parsedResult = ResultParser.parseResult(result);

  }

  private void encodeQRCodeContents(Intent intent, String type) {
    if (type.equals(Contents.Type.TEXT)) {
      String data = intent.getStringExtra(Intents.Encode.DATA);
      if (data != null && data.length() > 0) {
        contents = data;
        displayContents = data;
        title = activity.getString(R.string.contents_text);
      }
    } else if (type.equals(Contents.Type.EMAIL)) {
        title = activity.getString(R.string.contents_email);
    } else if (type.equals(Contents.Type.PHONE)) {
        title = activity.getString(R.string.contents_phone);
      }
    }

        Collection<String> phones = new ArrayList<String>(Contents.PHONE_KEYS.length);
        Collection<String> emails = new ArrayList<String>(Contents.EMAIL_KEYS.length);


  private static Iterable<String> toIterable(String[] values) {
    return values == null ? null : Arrays.asList(values);
  }

  Bitmap encodeAsBitmap() throws WriterException {
    String contentsToEncode = contents;
    if (contentsToEncode == null) {
      return null;
    }
    Map<EncodeHintType,Object> hints = null;
    String encoding = guessAppropriateEncoding(contentsToEncode);
    if (encoding != null) {
      hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
      hints.put(EncodeHintType.CHARACTER_SET, encoding);
    }
    MultiFormatWriter writer = new MultiFormatWriter();
    BitMatrix result = writer.encode(contentsToEncode, format, dimension, dimension, hints);
    int width = result.getWidth();
    int height = result.getHeight();
    int[] pixels = new int[width * height];
    for (int y = 0; y < height; y++) {
      int offset = y * width;
      for (int x = 0; x < width; x++) {
        pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
      }
    }

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    return bitmap;
  }

  private static String guessAppropriateEncoding(CharSequence contents) {
    // Very crude at the moment
    for (int i = 0; i < contents.length(); i++) {
      if (contents.charAt(i) > 0xFF) {
        return "UTF-8";
      }
    }
    return null;
  }

}
