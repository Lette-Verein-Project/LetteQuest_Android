package mia.lette.com.museum;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import mia.lette.com.museum.zxing.client.android.CaptureActivity;

@SuppressWarnings("deprecation")
public class EndActivity extends Activity implements OnClickListener {
	/**
	 * Test geht es???? ???????
	 */
	Intent intentQuest;
	Intent intentScan;
	Intent intentPuzzle;
	Intent intentMap;

	ArrayList<Quest> myDataP;
	ArrayList<Quest> myDataT;
	
	TextView isAnswerTrueView;
	TextView scoreView;
	Button nextQuestionButton;
	Button questB;
	Button puzzleB;
	Button mapB;
	int frageAnzahl;
	int frageDavor;
	SharedPreferences pref;
	Highscore highscore = new Highscore();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_end);
		pref = getSharedPreferences("questDaten",MODE_PRIVATE);
		
		isAnswerTrueView = (TextView) this.findViewById(R.id.isAnswerTrueView);
		scoreView = (TextView) this.findViewById(R.id.scoreView);
		nextQuestionButton = (Button) this.findViewById(R.id.nextQuestionButton);
		questB = (Button) this.findViewById(R.id.questBtn);
		puzzleB = (Button) this.findViewById(R.id.puzzleBtn);
		mapB = (Button) this.findViewById(R.id.mapBtn);
		
		nextQuestionButton.setOnClickListener(this);
		questB.setOnClickListener(this);
		puzzleB.setOnClickListener(this);
		mapB.setOnClickListener(this);

		char code = this.getIntent().getCharExtra("code", 'x');
		myDataP = (ArrayList<Quest>) this.getIntent().getSerializableExtra("pPassQ");
		myDataT = (ArrayList<Quest>) this.getIntent().getSerializableExtra("tPassQ");

		int versuche = pref.getInt("versuche",0);
		boolean isAnswerCorrect = pref.getBoolean("isAnswerCorrect", false);

        int points = pref.getInt("score", 0);
        scoreView.setText(""+points);

		Log.i("test", "test:" + frageDavor);
		if(isAnswerCorrect) {
			isAnswerTrueView.setText("Richtig");
			showHideButton(false, true);
			pref.edit().putInt("versuche", 0).commit();
			pref.edit().putBoolean("Fragen" + pref.getInt("frage", 0) / 3, true).commit();
			highscore.setP(""+points);
			highscore.updateHighscore();
					zahlenReset();
		} else {
			isAnswerTrueView.setText("Falsch");
			showHideButton(true, false);
			if(versuche == 2){
				pref.edit().putInt("versuche",0).commit();
				showHideButton(false, true);
				zahlenReset();
				pref.edit().putBoolean("falschbeantworteteFragen" + pref.getInt("frage", 0)/3, true).commit();
			}else {
                if (code == 'p') {


                    switch (pref.getInt("frage", 0)) {
                        case 0:
                            fragenWert(2, 2, 0, 1);
                        case 3:
                            fragenWert(5, 2, 3, 4);
                        case 6:
                            fragenWert(8, 2, 6, 7);
                        case 9:
                            fragenWert(11, 2, 9, 10);
                        case 12:
                            fragenWert(14, 2, 12, 13);
                        case 15:
                            fragenWert(17, 2, 15, 16);
                    }

                }
				versuche++;
				pref.edit().putInt("versuche", versuche).commit();
			}
		}
		

		
		boolean reachEnd = this.getIntent().getBooleanExtra("reachEnd", false);
		if(reachEnd || code == 't') {
            pref.edit().putInt("versuche",0).commit();
            showHideButton(false, true);
            zahlenReset();
        }
		
		intentQuest = new Intent(this,QuestActivity.class);
		intentQuest.putExtra("pPassQ", myDataP);
		intentQuest.putExtra("tPassQ", myDataT);
		intentQuest.putExtra("code", code);
		intentScan = new Intent(this,CaptureActivity.class);
		intentPuzzle = new Intent(this,PuzzleActivity.class);
		intentMap = new Intent(this , MapActivity.class);
	}
	
	public void showHideButton(boolean nQ, boolean menu) {
		if(!nQ) nextQuestionButton.setVisibility(View.GONE);
		if(!menu) {
			questB.setVisibility(View.GONE);
			puzzleB.setVisibility(View.GONE);
			mapB.setVisibility(View.GONE);
		} else {
			questB.setVisibility(View.VISIBLE);
			puzzleB.setVisibility(View.VISIBLE);
			mapB.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.end, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Button button = (Button) v;
		if(button == nextQuestionButton) this.startActivity(intentQuest);
		else if (button == questB) {
			intentScan.putExtra("showMenu", true);
			intentScan.putExtra("pPassQ", myDataP);
			intentScan.putExtra("tPassQ", myDataT);
			this.startActivity(intentScan);
		}
		else if (button == puzzleB) {
			intentPuzzle.putExtra("showMenu", true);
			intentPuzzle.putExtra("pPassQ", myDataP);
			intentPuzzle.putExtra("tPassQ", myDataT);
			this.startActivity(intentPuzzle);
		}
		else if (button == mapB) {
			intentMap.putExtra("showMenu", true);
			intentMap.putExtra("pPassQ", myDataP);
			intentMap.putExtra("tPassQ", myDataT);

			this.startActivity(intentMap);
		}
	}

	public void fragenWert(int fragenZahl, int fragenMultiplikator, int fragenSummand, int fragenSummandMax){
		pref = getSharedPreferences("questDaten",MODE_PRIVATE);
		frageAnzahl = pref.getInt("fragenAnzahl", pref.getInt("aktuelleFrage", 0));
		Log.i("test", "aktuelle Frage: " + pref.getInt("aktuelleFrage", 0));
		Log.i("test", "aktuelle frageAnzahl: " + frageAnzahl);
		if(pref.getInt("frageDavor", -1)== -1){
			frageDavor = frageAnzahl;
		}
		else{
			frageDavor = pref.getInt("frageDavor",0);
		}

		while (frageAnzahl == pref.getInt("aktuelleFrage", 0)) {
			if (frageAnzahl == fragenZahl) {
				frageAnzahl = (int) (Math.random() * fragenMultiplikator)+ fragenSummand;
			} else {
				frageAnzahl = (int) (Math.random() * fragenMultiplikator) + fragenSummandMax;
			}

			while (frageAnzahl == frageDavor){

				if (frageAnzahl == fragenZahl) {
					frageAnzahl = (int) (Math.random() * fragenMultiplikator) + fragenSummand;
				} else {
					frageAnzahl = (int) (Math.random() * fragenMultiplikator) + fragenSummandMax;
				}
			}

		}
		pref.edit().putInt("fragenAnzahl", frageAnzahl).commit();
		frageDavor = pref.getInt("aktuelleFrage", 0);
		pref.edit().putInt("frageDavor", frageDavor).commit();
		Log.v("test",""+frageDavor);
	}

	public void zahlenReset(){
		pref.edit().remove("fragenAnzahl").commit();
        pref.edit().remove("frageDavor").commit();
        Log.v("test", "" + pref.getInt("frageDavor", -1));
        Log.v("test", "" +pref.getInt("fragenAnzahl", -1));

    }
}
