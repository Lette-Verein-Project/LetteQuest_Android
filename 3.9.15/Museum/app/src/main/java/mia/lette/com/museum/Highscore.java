package mia.lette.com.museum;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Highscore extends Activity {
	/**
	 * Called when the activity is first created.
	 */
	private String n;  // = "KellerMarvin";
	private String p;  // = "45";
	private String d;  // = "123";
	private String e;  // = "KellerMarvin@lette-verein.de";
	private URL req;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			req = new URL("http://192.168.13.146:8080/LetteQuest/Servlet");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	setD(Settings.Secure.ANDROID_ID);
		setP("10");
		setN("Maurice");
		setE("sdasdasd");
		//Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(req+"?points="+p+"&name="+n));
		//startActivity(intent);
		updateHighscore();
		
	}
	
	public void setN(String n) {
		this.n = n;
	}
	
	public void setP(String p) {
		this.p = p;
	}
	
	public void setD(String d) {
		this.d = d;
	}
	
	public void setE(String e) {
		this.e = e;
	}
	
	public void updateHighscore() {
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					//req = new URL(req + "?points="+p+"&name="+n+"&email="+e+"&deviceid="+d);
                   /* req = new URL(req + "?points="+p+"&name="+n);
					HttpURLConnection client1 =  ((HttpURLConnection) req.openConnection());
					client1.setRequestMethod("GET");
					Log.v("Test", "sdasdasdas");
					client1.connect();
                    Log.v("Test", "connected");*/

					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet(
						req+"?points="+p+"&name="+n);//"&email="+e+"&deviceid="+d
					//);
					
					HttpResponse response = null;
					try {
					response = client.execute(request);
						Log.w("info", "request: " + request);
						Log.w("info", "response: " + response);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/*
					HttpEntity entity = response.getEntity();
					Log.w("info", "entity: " + entity);
					
					try {
						InputStreamReader reader = new InputStreamReader(entity
								.getContent(), "utf8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					*/
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		thread.start();
	}
	
}
