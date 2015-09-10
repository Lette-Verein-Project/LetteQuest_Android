package mia.lette.com.museum;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


public class MainActivity extends Activity implements View.OnClickListener {
    public Button mitwirkendeBtn;
    public Button anleitungBtn;
    public Button zurueckVonAnleitungBtn;
    public Button startBtn;
    public Button bestaetigenBtn;
    public Button infoBtn;
    public Button zurueckVonInfoBtn;
    public EditText nameText;
    public FrameLayout anleitungsLayout;
    public FrameLayout infoLayout;
    public LinearLayout startLayout;

    SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent sadasdasd = new Intent(this, Highscore.class);
        startActivity(sadasdasd);
        pref = getSharedPreferences("questDaten",MODE_PRIVATE);
        pref.edit().clear().commit();

        mitwirkendeBtn = (Button)findViewById(R.id.mitwirkendeBtn);
        mitwirkendeBtn.setOnClickListener(this);
        anleitungBtn = (Button)findViewById(R.id.anleitungBtn);
        anleitungBtn.setOnClickListener(this);
        zurueckVonAnleitungBtn = (Button)findViewById(R.id.zurueckVonAnleitungBtn);
        zurueckVonAnleitungBtn.setOnClickListener(this);
        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(this);
        startBtn.setEnabled(false);
        bestaetigenBtn = (Button)findViewById(R.id.bestaetigenBtn);
        bestaetigenBtn.setOnClickListener(this);
        infoBtn = (Button)findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(this);
        zurueckVonInfoBtn = (Button)findViewById(R.id.zurueckVonInfoBtn);
        zurueckVonInfoBtn.setOnClickListener(this);
        nameText = (EditText)findViewById(R.id.nameText);
        startLayout = (LinearLayout)findViewById(R.id.startLayout);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        if(mitwirkendeBtn.getId() == v.getId()){
            Intent mitwirkendeScreen = new Intent(this, MitwirkendeActivity.class);
            startActivity(mitwirkendeScreen);
        }
        if(anleitungBtn.getId() == v.getId()){
            anleitungsLayout = (FrameLayout)findViewById(R.id.AnleitungsLayout);
            anleitungsLayout.setVisibility(View.VISIBLE);
            startLayout.setVisibility(View.INVISIBLE);
        }
        if(zurueckVonAnleitungBtn.getId() == v.getId()){
            anleitungsLayout = (FrameLayout)findViewById(R.id.AnleitungsLayout);
            anleitungsLayout.setVisibility(View.INVISIBLE);
            startLayout.setVisibility(View.VISIBLE);
        }
        if(infoBtn.getId() == v.getId()){
            infoLayout = (FrameLayout)findViewById(R.id.infoLayout);
            infoLayout.setVisibility(View.VISIBLE);
            startLayout.setVisibility(View.INVISIBLE);
        }
        if(zurueckVonInfoBtn.getId() == v.getId()){
            infoLayout = (FrameLayout)findViewById(R.id.infoLayout);
            infoLayout.setVisibility(View.INVISIBLE);
            startLayout.setVisibility(View.VISIBLE);
        }
        if(bestaetigenBtn.getId() == v.getId()){

           if(!nameText.getText().toString().equals("")){

             startBtn.setEnabled(true);
           }
        }
        if(startBtn.getId() == v.getId()){
            Intent erklaerungsScreen = new Intent(this, ErklaerungActivity.class);
            startActivity(erklaerungsScreen);
        }

    }

    @Override
    public void onBackPressed(){

    }
}
