package mia.lette.com.museum;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Frances Schmidt on 23.07.2015.
 */
public class FragenWaehler extends Activity {
    SharedPreferences pref;
    int frageAnzahl;
    int frageDavor;

    public void fragenWert(int fragenZahl, int fragenMultiplikator, int fragenSummand, int fragenSummandMax){
        pref = getSharedPreferences("questDaten",MODE_PRIVATE);
        frageAnzahl = pref.getInt("fragenAnzahl", pref.getInt("aktuelleFrage", 0));
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

                pref.edit().putInt("fragenAnzahl", frageAnzahl).commit();
                frageDavor = pref.getInt("aktuelleFrage", 0);
                pref.edit().putInt("frageDavor", frageDavor).commit();




            }
        }

    }

}
