package com.audiolab.areago;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PaseoPreview extends Activity  {
	
	LocationManager locManager;
	LocationListener locationListener;
	Paseo walk;
	double[] nl = new double[2];
	BroadcastReceiver receiver = null;
	WifiManager wifi;
	TextView status;
	WifiConfiguration wifiConf;
	
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paseo_preview); 
		setTitle("AREAGO : "+getIntent().getStringExtra("titulo"));
		
		String JSONPoints = getIntent().getStringExtra("json");
		String lat = getIntent().getStringExtra("lat");
		String lon = getIntent().getStringExtra("lon");
		
		// Definimos paseo
		walk = new Paseo(getIntent().getIntExtra("id", 0));
		walk.setTitle(getIntent().getStringExtra("titulo"));
		walk.setDescription(getIntent().getStringExtra("descripcion"));
		if (JSONPoints.length()>0) { walk.create_points(JSONPoints); } else {Toast.makeText(this,"Este paseo no tiene puntos",Toast.LENGTH_LONG).show();}
		
		
		((TextView)findViewById(R.id.titulo)).setText(getIntent().getStringExtra("titulo"));
		((TextView)findViewById(R.id.descripcion)).setText(getIntent().getStringExtra("descripcion"));
		
    	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	locationListener = new PaseoLocationListener();
    	
    	Criteria crit = new Criteria();
    	crit.setAccuracy(Criteria.ACCURACY_FINE);
    	locManager.getLastKnownLocation(locManager.getBestProvider(crit, true));
    	
    	//Wifi
    	//setup wifi
//    	wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//    	
//    	
//    	//wifi info
//    	WifiInfo info = wifi.getConnectionInfo();
//		((TextView)findViewById(R.id.wifi)).setText("WiFi Status: " + info.toString());
//		//wifi_connections
//		
//		
//		// Register Broadcast Receiver
//		if (receiver == null) receiver = new WiFiScanReceiver(this);
//		registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//		
//		wifi.startScan();
	}
    	
	public void onResume() {

		super.onResume();
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		//wifi.updateNetwork(wifi);
	}

	public void onPause() {
		super.onPause();
		locManager.removeUpdates(locationListener);
	}
	
	public void onStop() {
		super.onStop();
		this.walk.stop();
		//unregisterReceiver(receiver);
	}
	
	// INFO: Gestión del GPS
	private class PaseoLocationListener implements LocationListener {
	
    	public void onLocationChanged(Location location) {
    		// TODO Auto-generated method stub
    		if (location != null) {
    			//Toast.makeText(getBaseContext(),"Location changed",Toast.LENGTH_LONG).show();
    			((TextView)findViewById(R.id.gps)).setText("Posición: "+location.getLatitude()+"/"+location.getLongitude()+"/"+location.getAccuracy());
    			SoundPoint nl = new SoundPoint(location);
    			Log.d("AREAGO","Location changed");
    			
    			// Miramos si el punto actual está dentro del radio de acción de algun punto del paseo.
    			// Si nos hemos movido
    			String info = walk.check_collisions(nl);
    			((TextView)findViewById(R.id.log_view)).setText(info);
    			}
    		}

    	public void onProviderDisabled(String provider) {
    		// TODO Auto-generated method stub
    		((TextView)findViewById(R.id.gps)).setText(provider+" desconectado");
    		Log.d("AREAGO","GPS Disable");
    	}

    	public void onProviderEnabled(String provider) {
    		// TODO Auto-generated method stub
    		((TextView)findViewById(R.id.gps)).setText("GPS Conectado:"+provider);
    		Log.d("AREAGO","GPS Enabled");
    	}

    	public void onStatusChanged(String provider, int status, Bundle extras) {
    		// TODO Auto-generated method stub
    		String st = "";
    		switch (status) {
    		case android.location.LocationProvider.AVAILABLE:
    			st="Disponible";
    		case android.location.LocationProvider.OUT_OF_SERVICE:
    			st="Desactivado";
    		case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
    			st="Temporalmente desactivado";
    		}
    		((TextView)findViewById(R.id.gps)).setText("GPS Status:"+st);
    		Log.d("AREAGO","Status"+st);
    	}
	}


    	
}


