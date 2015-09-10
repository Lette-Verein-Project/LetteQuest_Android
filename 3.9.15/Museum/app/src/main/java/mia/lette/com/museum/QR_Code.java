package mia.lette.com.museum;

import android.util.Log;

/**
 * Created by Lette-Verein on 30.07.2015.
 */
public class QR_Code {
    private String fragentyp;
    private int frage;
    private int raum;
    private int stockwerk;

    public QR_Code(String fragentyp,int frage,int raum,int stockwerk) {

            this.fragentyp = fragentyp;
            this.frage = frage;
            this.raum = raum;
            this.stockwerk = stockwerk;

    }




    public String getFragentyp() {
        Log.v("test","" + fragentyp);
        return fragentyp;
    }

    public int getFrage() {
        Log.v("test","" + frage);
        return frage;
    }

    public int getRaum() {
        Log.v("test","" + raum);
        return raum;
    }

    public int getStockwerk() {
        Log.v("test","" + stockwerk);
        return stockwerk;
    }

}
