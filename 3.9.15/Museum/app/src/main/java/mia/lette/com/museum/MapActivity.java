package mia.lette.com.museum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import mia.lette.com.museum.zxing.client.android.CaptureActivity;


public class MapActivity extends Activity implements View.OnClickListener {
    public Button questBtn;
    public Button puzzleBtn;
    public Button mapBtn;
    public Button ersteEtageBtn;
    public Button zweiteEtageBtn;
    public Button dritteEtageBtn;
    public ImageView mapImageview;
    public Intent activityWechsel;
    public ArrayList<Quest> myDataP;
    public ArrayList<Quest> myDataT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        questBtn = (Button) findViewById(R.id.questBtn);
        questBtn.setOnClickListener(this);
        puzzleBtn = (Button) findViewById(R.id.puzzleBtn);
        puzzleBtn.setOnClickListener(this);
        mapBtn = (Button) findViewById(R.id.mapBtn);
        mapBtn.setEnabled(false);
        ersteEtageBtn = (Button) findViewById(R.id.ersteEtageBtn);
        ersteEtageBtn.setOnClickListener(this);
        zweiteEtageBtn = (Button) findViewById(R.id.zweiteEtageBtn);
        zweiteEtageBtn.setOnClickListener(this);
        dritteEtageBtn = (Button) findViewById(R.id.dritteEtageBtn);
        dritteEtageBtn.setOnClickListener(this);
        mapImageview = (ImageView) findViewById(R.id.mapView);

        myDataP = (ArrayList<Quest>) this.getIntent().getSerializableExtra("pPassQ");
        myDataT = (ArrayList<Quest>) this.getIntent().getSerializableExtra("tPassQ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);

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
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.questBtn:
                activityWechsel = new Intent(this,CaptureActivity.class);
                activityWechsel.putExtra("pPassQ", myDataP);
                activityWechsel.putExtra("tPassQ", myDataT);
                startActivity(activityWechsel);
                break;
            case R.id.puzzleBtn:
                activityWechsel = new Intent(this,PuzzleActivity.class);
                activityWechsel.putExtra("pPassQ", myDataP);
                activityWechsel.putExtra("tPassQ", myDataT);
                startActivity(activityWechsel);
                break;
            case R.id.mapBtn:
                activityWechsel = new Intent(this,MapActivity.class);
                activityWechsel.putExtra("pPassQ", myDataP);
                activityWechsel.putExtra("tPassQ", myDataT);
                startActivity(activityWechsel);
                break;
            case R.id.ersteEtageBtn:
                mapImageview.setImageResource(R.drawable.grundriss_1og);
                break;
            case R.id.zweiteEtageBtn:
                mapImageview.setImageResource(R.drawable.grundriss_2og);
                break;
            case R.id.dritteEtageBtn:
                mapImageview.setImageResource(R.drawable.grundriss_3og);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed(){

    }
}
