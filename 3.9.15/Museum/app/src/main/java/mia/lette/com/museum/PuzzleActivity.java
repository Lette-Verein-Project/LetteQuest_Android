package mia.lette.com.museum;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View.OnClickListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import mia.lette.com.museum.zxing.client.android.CaptureActivity;


public class PuzzleActivity extends Activity implements OnClickListener {
    public Button questBtn;
    public Button puzzleBtn;
    public Button mapBtn;
    public Button gewinnspielBtn;
    public Button kleinergewinnBtn;

    public Intent activityWechsel;

    ArrayList <Quest> myDataP;
    ArrayList <Quest> myDataT;
    ArrayList <ImageView> puzzleImages;

   SharedPreferences prefe;

    ImageView obenLinksImage;
    ImageView obenRechtsImage;
    ImageView mitteLinksImage;
    ImageView mitteRechtsImage;
    ImageView untenLinksImage;
    ImageView untenRechtsImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        questBtn = (Button)findViewById(R.id.questBtn);
        questBtn.setOnClickListener(this);
        puzzleBtn = (Button)findViewById(R.id.puzzleBtn);
        puzzleBtn.setOnClickListener(this);
        puzzleBtn.setEnabled(false);
        mapBtn = (Button)findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(this);
        kleinergewinnBtn = (Button) findViewById(R.id.kleinerGewinnBtn);
        kleinergewinnBtn.setOnClickListener(this);
        gewinnspielBtn = (Button) findViewById(R.id.gewinnspielBtn);
        gewinnspielBtn.setOnClickListener(this);

        myDataP = (ArrayList<Quest>) this.getIntent().getSerializableExtra("pPassQ");
        myDataT = (ArrayList<Quest>) this.getIntent().getSerializableExtra("tPassQ");

        ArrayList<Boolean> richtigeFragen = new ArrayList<Boolean>();


        obenLinksImage = (ImageView) findViewById(R.id.puzzleobenlinksImage);
        obenRechtsImage = (ImageView) findViewById(R.id.puzzleobenrechtsImage);
        mitteLinksImage = (ImageView) findViewById(R.id.puzzlemittelinksImage);
        mitteRechtsImage = (ImageView) findViewById(R.id.puzzlemitterechtsImage);
        untenLinksImage = (ImageView) findViewById(R.id.puzzleuntenlinksImage);
        untenRechtsImage = (ImageView) findViewById(R.id.puzzleuntenrechtsImage);

        puzzleImages = new ArrayList<>();
        puzzleImages.add(obenLinksImage);
        puzzleImages.add(obenRechtsImage);
        puzzleImages.add(mitteLinksImage);
        puzzleImages.add(mitteRechtsImage);
        puzzleImages.add(untenLinksImage);
        puzzleImages.add(untenRechtsImage);

        ArrayList<Boolean> vergleichsArray = new ArrayList<>();
        for (int i = 0; i < myDataP.size()/3; i++ ){
            prefe = getSharedPreferences("questDaten",MODE_PRIVATE);
            richtigeFragen.add(i, prefe.getBoolean("Fragen" + i, false));

            Log.v("test", "dsadasdasdasd" + prefe + "  " + prefe.getBoolean("Fragen" + i, false));
            if(richtigeFragen.get(i) == true) {

                ImageView a = puzzleImages.get(i);
                a.setVisibility(View.VISIBLE);
            }
            vergleichsArray.add(i,true);
        }


      if(!richtigeFragen.contains(false)) {
          kleinergewinnBtn.setEnabled(true);
          gewinnspielBtn.setEnabled(true);
      }



        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_puzzle, menu);
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
        switch (v.getId()) {
            case R.id.questBtn:
                activityWechsel = new Intent(this, CaptureActivity.class);
                activityWechsel.putExtra("pPassQ", myDataP);
                activityWechsel.putExtra("tPassQ", myDataT);
                startActivity(activityWechsel);
                break;
            case R.id.puzzleBtn:
                activityWechsel = new Intent(this, PuzzleActivity.class);
                activityWechsel.putExtra("pPassQ", myDataP);
                activityWechsel.putExtra("tPassQ", myDataT);
                startActivity(activityWechsel);
                break;
            case R.id.mapBtn:
                activityWechsel = new Intent(this, MapActivity.class);
                activityWechsel.putExtra("pPassQ", myDataP);
                activityWechsel.putExtra("tPassQ", myDataT);
                startActivity(activityWechsel);
                break;
            default:
                break;
        }
    }
}
