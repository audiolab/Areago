package com.audiolab.areago;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class PaseoPreview extends Activity  {
	
	LocationManager locManager;
	LocationListener locationListener;
	Paseo walk;
	double[] nl = new double[2];

	TextView status;
	
	//para el wifi
	BroadcastReceiver receiver = null;
	WifiConfiguration wifiConf;
	WifiManager wifi;

	ScheduledExecutorService exec;
	
	final Runnable Scanning = new Runnable() {
        public void run() {
            wifi.startScan();
       }
	};
	
	KeyguardManager  myKeyGuard;
	KeyguardLock lock;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("AREAGO","En PaseoPreview");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.paseo_preview);
		setTitle("AREAGO : "+getIntent().getStringExtra("titulo"));
		
		//gestion bloqueo pantalla
		myKeyGuard = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        lock = myKeyGuard.newKeyguardLock(KEYGUARD_SERVICE);
		//Gestion de volumen con audiomanager
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    	Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
		String JSONPoints = getIntent().getStringExtra("json");
		
		// Definimos paseo
		walk = new Paseo(getIntent().getIntExtra("id", 0));
		walk.setTitle(getIntent().getStringExtra("titulo"));
		walk.setDescription(getIntent().getStringExtra("descripcion"));
		walk.setExcerpt(getIntent().getStringExtra("excerpt"));
		walk.setVibrator(v);
		walk.setBitmap(getIntent().getStringExtra("path_image"));
		if (JSONPoints.length()>0) { walk.create_points(JSONPoints); } else {Toast.makeText(this,"Este paseo no tiene puntos",Toast.LENGTH_LONG).show();}
		
		if (walk.hasImage()) { ((ImageView)findViewById(R.id.imagen)).setImageBitmap(walk.getBitmap()); }
		else { ((ImageView)findViewById(R.id.imagen)).setImageResource(R.drawable.areago_default);}
		
		((ImageView)findViewById(R.id.imagen)).setAdjustViewBounds(true);

		((TextView)findViewById(R.id.titulo_imagen)).setText(walk.getTitle());
		String html = "<p style='font-style:oblique;text-align:justify;padding-left:30px;'>"+walk.getExcerpt()+"</p>"+
				"<div content='description' style='text-align:justify;'>"+walk.getDescription()+"</div>";
		//((WebView)findViewById(R.id.webview)).loadData(html, "text/html; charset=UTF-8", null);
		((WebView)findViewById(R.id.webview)).loadDataWithBaseURL("fake://not/needed", html, "text/html", "utf-8", "");
		
		Log.d("AREAGO","onCreate");

	}
	
	public void onStart() {
		super.onStart();
		
		Log.d("AREAGO","onStart");
		
    	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	locationListener = new PaseoLocationListener();
    	
    	Criteria crit = new Criteria();
    	crit.setAccuracy(Criteria.ACCURACY_FINE);
    	locManager.getLastKnownLocation(locManager.getBestProvider(crit, true));
		
    	//configure wifi
    	wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	if (receiver == null) receiver = new WiFiScanReceiver(this);
    	registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
	}
    	
	public void onResume() {

		super.onResume();
		
		Log.d("AREAGO","onResume");
		
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		lock.disableKeyguard();
		((TextView)findViewById(R.id.gps)).setText("Layer: "+walk.getLayer());
		if (receiver == null) { 
			receiver = new WiFiScanReceiver(this);
			registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		}
		
		if (exec == null ) {
			exec = Executors.newScheduledThreadPool(1);
			exec.scheduleAtFixedRate(Scanning, 0, 5, TimeUnit.SECONDS);
		}


	}

	public void onPause() {
		super.onPause();
		Log.d("AREAGO","onPause");
		// No debería cambiar nada, y dejar la aplicación seguir corriendo mientras hacemos otra historia
	}
	
	public void onStop() {
		
		super.onStop();
		
		while (!exec.isShutdown()) { exec.shutdownNow(); }
		this.walk.pause();
		this.walk.stop();
		try {
			locManager.removeUpdates(locationListener);
			((TextView)findViewById(R.id.status_gps)).setVisibility(View.VISIBLE);
    		((TextView)findViewById(R.id.status_gps)).setText("Dispositivo GPS desactivado");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		try { 
			unregisterReceiver(receiver); // TODO: Peta cuando no está ejecutandose el receiver y se quiere parar...
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
		Log.d("AREAGO","onStop");
	}
	
	public void onDestroy() {
		super.onDestroy();
		while (!exec.isShutdown()) { exec.shutdownNow(); }
		Log.d("AREAGO","onDestroy");
		
//		while (!exec.isShutdown()) { exec.shutdownNow(); }
//		this.walk.pause();
//		this.walk.stop();
//		try {
//			locManager.removeUpdates(locationListener);
//			((TextView)findViewById(R.id.status_gps)).setVisibility(View.VISIBLE);
//    		((TextView)findViewById(R.id.status_gps)).setText("Dispositivo GPS desactivado");
//		} catch (IllegalArgumentException e) {
//			e.printStackTrace();
//		}
//		
//		try { 
//			unregisterReceiver(receiver); // TODO: Peta cuando no está ejecutandose el receiver y se quiere parar...
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//		}
		
	}
	
	// INFO: Gestión del GPS
	private class PaseoLocationListener implements LocationListener {
	
    	public void onLocationChanged(Location location) {
    		// TODO Auto-generated method stub    	
    		if (location != null) {
    			((TextView)findViewById(R.id.gps)).setText("Layer: "+walk.getLayer()+" Posición: "+location.getLatitude()+"/"+location.getLongitude()+"/"+location.getAccuracy());
        		((TextView)findViewById(R.id.status_gps)).setVisibility(View.GONE);
    			SoundPoint nl = new SoundPoint(location);
    			Log.d("AREAGO","Location changed");
    			
    			// Miramos si el punto actual está dentro del radio de acción de algun punto del paseo.
    			// Si nos hemos movido
    			walk.check_collisions(nl);

    			}
    		}

    	public void onProviderDisabled(String provider) {
    		// TODO Auto-generated method stub
    		((TextView)findViewById(R.id.gps)).setText(provider+" desconectado");
    		Log.d("AREAGO","GPS Disable");
    		((TextView)findViewById(R.id.status_gps)).setText("Dispositivo GPS desactivado");
    		((TextView)findViewById(R.id.status_gps)).setVisibility(View.VISIBLE);
    		walk.location_pause();
    	}

    	public void onProviderEnabled(String provider) {
    		// TODO Auto-generated method stub
    		((TextView)findViewById(R.id.gps)).setText("GPS Conectado:"+provider);
    		((TextView)findViewById(R.id.status_gps)).setText("Dispositivo GPS activado");
    		((TextView)findViewById(R.id.status_gps)).setVisibility(View.VISIBLE);
    		Log.d("AREAGO","GPS Enabled");
    	}

    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		// TODO Auto-generated method stub
    		String st = "";
    		switch (status) {
    		case android.location.LocationProvider.AVAILABLE:
    			st=getString(R.string.gps_disponible);
    			break;
    		case android.location.LocationProvider.OUT_OF_SERVICE:
    			st=getString(R.string.gps_no_disponible);
    			walk.location_pause();
    			Log.d("AREAGO","Pausamos el paseo por fuera de servicio");
    			break;
    		case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
    			st=getString(R.string.gps_temporalmente_no_disponible);
    			walk.location_pause();
    			Log.d("AREAGO","Pausamos el paseo por temporalmente no disponible");
    			break;
    		}
    		((TextView)findViewById(R.id.gps)).setText("GPS Status:"+st);
    		Log.d("AREAGO","GPS Status: "+st);
    		((TextView)findViewById(R.id.status_gps)).setText(getString(R.string.dipositivo_gps)+st);
    		((TextView)findViewById(R.id.status_gps)).setVisibility(View.VISIBLE);
    		Log.d("AREAGO","Status"+st);
    	}
	}


    	
}


