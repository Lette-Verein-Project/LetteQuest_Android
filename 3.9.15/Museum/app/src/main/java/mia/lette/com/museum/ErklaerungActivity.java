package mia.lette.com.museum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import mia.lette.com.museum.zxing.client.android.CaptureActivity;


public class ErklaerungActivity extends Activity implements View.OnClickListener {
    public Button questBtn;
    public Button puzzleBtn;
    public Button mapBtn;
    ArrayList<Quest> myDataP = null;
    ArrayList<Quest> myDataT = null;
    String stringUrl;
    boolean connected;
    Intent intentScan;
    private SharedPreferences pref;
    int progress;
    TextView connectMessage;
    Button connectButton;
    FrameLayout connectLayout;
    public ProgressBar xmlpbar;
    private Intent puzzleIntent;
    private Intent mapIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        xmlpbar = (ProgressBar)findViewById(R.id.xmlProgressbar);

        setContentView(R.layout.activity_erklaerung);

        questBtn = (Button) findViewById(R.id.questBtn);
        questBtn.setOnClickListener(this);
        puzzleBtn = (Button)findViewById(R.id.puzzleBtn);
        puzzleBtn.setOnClickListener(this);
        mapBtn = (Button)findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(this);
        connectLayout = (FrameLayout) findViewById(R.id.connectLayout);
        connectMessage = (TextView) findViewById(R.id.connectMessage);
        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this);
        connectButton.setText("ok");
        stringUrl = "http://www.cubitor.de/lette/lettequest/quests.xml";
        //stringUrl = "http://kyl.lima-city.de/quests.xml";
        intentScan = new Intent(this, CaptureActivity.class);
        puzzleIntent = new Intent(this,PuzzleActivity.class);
        mapIntent = new Intent(this,MapActivity.class);
        questBtn.setEnabled(false);
        puzzleBtn.setEnabled(false);
        mapBtn.setEnabled(false);
        connectToNetwork();
    }

    public void connectToNetwork(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
            Log.d("Hallo", "ist verbunden mit folgender URL " + stringUrl);
            connected = true;
            connectLayout.setVisibility(View.INVISIBLE);
        } else {
            Log.d("Hallo", "nicht verbunden");
            connected = false;
            Intent settingsintent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(settingsintent);
            connectLayout.setVisibility(View.VISIBLE);
            questBtn.setEnabled(false);
            puzzleBtn.setEnabled(false);
            mapBtn.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_erklaerung, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){

    }

    @Override
    public void onClick(View v) {
       if(v.getId() == R.id.connectButton){
           connectToNetwork();
       }
if(connected == true && progress == 100) {
    switch (v.getId()) {
        case R.id.questBtn:
            Log.d("haha", "sdasdasdas");
            startActivity(intentScan);
            Log.d("haha", "sdasdasdas");
            break;
        case R.id.puzzleBtn:

            startActivity(puzzleIntent);
            break;
        case R.id.mapBtn:

            startActivity(mapIntent);
            break;
        default:
            break;
    }
    }else {
    connectToNetwork();
    }

     /*   if(questBtn.getId() == v.getId()){

        }
        if(puzzleBtn.getId() == v.getId()){

        }
        if(mapBtn.getId() == v.getId()){
            Intent mapScreen = new Intent(this, MapActivity.class);
            startActivity(mapScreen);
        }
*/
    }

    private class DownloadWebpageTask extends AsyncTask<String, Integer, String> {
        private final String DEBUG_TAG = null;

        @Override
        protected String doInBackground(String... urls) {
            xmlpbar = (ProgressBar)findViewById(R.id.xmlProgressbar);
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            if(progress[0] == 100) {
                xmlpbar.setProgress(100);
                questBtn.setEnabled(true);
                puzzleBtn.setEnabled(true);
                mapBtn.setEnabled(true);
                xmlpbar.setVisibility(View.INVISIBLE);
            }

            Log.v("onProgressUpdate", "Progress so far: " + progress[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
int i = 0;
            try {
                i++;

                XmlParser p_parser = new XmlParser();
                XmlParser t_parser = new XmlParser();

                InputStream p_stream = new ByteArrayInputStream(result.getBytes());
                InputStream t_stream = new ByteArrayInputStream(result.getBytes());
                Log.i("Test", "klappt es hier noch");
                myDataP = (ArrayList<Quest>) p_parser.parse(p_stream, "p");
                myDataT = (ArrayList<Quest>) t_parser.parse(t_stream, "t");

                Log.i("haha", "P_Size: " + myDataP.size());
                Log.i("haha", "T_Size: " + myDataT.size());

                intentScan.putExtra("pPassQ", myDataP);
                intentScan.putExtra("tPassQ", myDataT);

                puzzleIntent.putExtra("pPassQ", myDataP);
                puzzleIntent.putExtra("tPassQ", myDataT);

                mapIntent.putExtra("pPassQ", myDataP);
                mapIntent.putExtra("tPassQ", myDataT);



                progress = 100;
            } catch (XmlPullParserException e) {
                Log.i("haha", "Exception parse mit number xml :" + e.getColumnNumber() + "Line NUmber: " + e.getLineNumber());
                Log.i("haha", "Exception parse xml :" + e);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Given a URL, establishes an HttpUrlConnection and retrieves
        // the web page content as a InputStream, which it returns as
        // a string.
        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 10000;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                publishProgress(25);
                int response = conn.getResponseCode();
                Log.v("onProgressUpdate", "The response is: " + response);
                is = conn.getInputStream();
                publishProgress(50);
                // Convert the InputStream into a string

                Reader reader = null;
                reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
                char[] buffer = new char[len];
                String contentAsString = new String();
                int count;
                while ((count = reader.read(buffer)) > 0) {  contentAsString += new String(buffer, 0, count); }
                publishProgress(75);

                publishProgress(90);
                Log.v("onProgressUpdate", contentAsString);
                is.close();
                publishProgress(100);
                return contentAsString;
                // return is;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        // Reads an InputStream and converts it to a String.
       /* public String readIt(InputStream stream, int len) throws IOException,
                UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            publishProgress(75);
            Log.v("onProgressUpdate", "" + buffer.length);
            return new String(buffer);
        }*/

    }
}
