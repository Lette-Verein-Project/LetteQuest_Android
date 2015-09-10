package mia.lette.com.museum;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import mia.lette.com.museum.zxing.client.android.CaptureActivity;

@SuppressWarnings("deprecation")
public class QuestActivity extends Activity implements OnClickListener {

	public Quest[] quest = new Quest[9];
	public ArrayList<Quest> myDataP;
	public ArrayList<Quest> myDataT;
	public char code;
	public static int i;
	public static int points;

	private SharedPreferences pref;

	long updatedTime = 0L;

	Intent intentEnd;
	TextView timeView;
	TextView questionView;
	Button answer1Button;
	Button answer2Button;
	Button answer3Button;
	Button answer4Button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quest);
		myDataP = (ArrayList<Quest>) this.getIntent().getSerializableExtra("pPassQ");
		myDataT = (ArrayList<Quest>) this.getIntent().getSerializableExtra("tPassQ");
		pref = getSharedPreferences("questDaten", MODE_PRIVATE);
		if(pref.getBoolean("Fragen"+  pref.getInt("frage", 0)/3,false) == true || pref.getBoolean("falschbeantworteteFragen" + pref.getInt("frage", 0)/3, false) == true){
			Intent intentcap = new Intent(this,CaptureActivity.class);
			intentcap.putExtra("pPassQ", myDataP);
			intentcap.putExtra("tPassQ", myDataT);
			this.startActivity(intentcap);
		}
		if(pref.getBoolean("prefAuslesen", false) == false){
			this.sharedPreferencesAuslesen();
		}
		else {
			i = pref.getInt("fragenAnzahl",0);
			Log.i("test", "das Ergebnis von i: " + i);
			pref.edit().putInt("aktuelleFrage", i).commit();

		}


		code = pref.getString("code", "x").charAt(0);


		timeView = (TextView) this.findViewById(R.id.timeView);
		questionView = (TextView) this.findViewById(R.id.questionView);
		answer1Button = (Button) this.findViewById(R.id.Answer1);
		answer2Button = (Button) this.findViewById(R.id.Answer2);
		answer3Button = (Button) this.findViewById(R.id.Answer3);
		answer4Button = (Button) this.findViewById(R.id.Answer4);

		answer1Button.setOnClickListener(this);
		answer2Button.setOnClickListener(this);
		answer3Button.setOnClickListener(this);
        answer4Button.setOnClickListener(this);



		if (code == 'p') {
			Log.d("Hallo", "timerView" + timeView);
			timeView.setVisibility(View.GONE);
			int zahl = 0;
			for (Quest q : myDataP) {
				quest[zahl] = q;
				zahl++;
			}
		} else if (code == 't') {


            timeView.setVisibility(View.VISIBLE);
            Log.d("Hallo", "timerView" + timeView);
            new CountDownTimer(30000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeView.setText("" + millisUntilFinished / 1000);
                    updatedTime = millisUntilFinished / 1000;
                    Log.d("Hallo", "" + millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    timeView.setText("Time's up!");
                    updatedTime = 0;
                }
            }.start();
            int zahl = 0;
            for (Quest q : myDataT) {
                quest[zahl] = q;
                Log.v("test",quest[zahl].getTitle());
                zahl++;
            }
        }


		this.displayQuest();

		intentEnd = new Intent(this, EndActivity.class);
		intentEnd.putExtra("code", code);

	}

	public void displayQuest() {
		if(i <= quest.length -1 ) {
			questionView.setText(quest[i].getTitle());
			answer1Button.setText(quest[i].getAnswer1());
			answer2Button.setText(quest[i].getAnswer2());
			answer3Button.setText(quest[i].getAnswer3());
			answer4Button.setText(quest[i].getAnswer4());
		}else {
			Log.v("test", "zu wenige Fragen im Array");
			Intent scannen = new Intent(this, CaptureActivity.class);
			scannen.putExtra("pPassQ", myDataP);
			scannen.putExtra("tPassQ", myDataT);
			pref.edit().putBoolean("prefAuslesen", false).commit();
			pref.edit().remove("aktuelleFrage").commit();
			startActivity(scannen);
		}
	}

	public void sharedPreferencesAuslesen (){

		i = pref.getInt("frage", 0);

		switch (i){
			case 0: i = (int) (Math.random()*3);
				Log.v("test","richtig " + i);
				break;
			case 3: i = (int) (Math.random()*3)+3;
				Log.v("test","richtig " + i);
				break;
			case 6: i = (int) (Math.random()*3)+6;
				Log.v("test","richtig " + i);
				break;
			case 9: i = (int) (Math.random()*3)+9;
				break;
			case 12: i = (int) (Math.random()*3)+12;
				break;
			case 15: i = (int) (Math.random() * 3) + 15;
				break;
		}
		pref.edit().putInt("aktuelleFrage", i).commit();
		pref.edit().putBoolean("prefAuslesen", true).commit();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.quest, menu);
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
	public void onBackPressed() {

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Button button = (Button) v;
	String a = ""+ button.getText();
		if (a.equals("\"" + quest[i].ok + "\"")){

			if(code == 'p') points += 10;

			else if(code == 't') {

				points += Math.ceil((30 - (updatedTime / 1000)) / 3);
				intentEnd.putExtra("score", points);

			}
			pref.edit().putBoolean("isAnswerCorrect",true).commit();
		}else {

			pref.edit().putBoolean("isAnswerCorrect", false).commit();
		}
		pref.edit().putInt("score", points).commit();
		/*if (i < 2)
			i++;
		else {
			i = 0;
			intentEnd.putExtra("reachEnd", true);
		}*/
		
		intentEnd.putExtra("pPassQ", myDataP);
		intentEnd.putExtra("tPassQ", myDataT);
		this.startActivity(intentEnd);
	}
	

}
