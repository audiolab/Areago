package com.audiolab.areago;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class PaseoPreview extends Activity  {
	
	LocationManager locManager;
	LocationListener locationListener;
	Paseo walk;
	double[] nl = new double[2];
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paseo_preview); 
		setTitle("AREAGO : "+getIntent().getStringExtra("titulo"));
		
		String JSONPoints = getIntent().getStringExtra("json");
		String lat = getIntent().getStringExtra("lat");
		String lon = getIntent().getStringExtra("lon");
		
		walk = new Paseo(getIntent().getIntExtra("id", 0),getIntent().getStringExtra("titulo"),getIntent().getStringExtra("descripcion"));
		walk.create_points(JSONPoints);
		
		
		((TextView)findViewById(R.id.titulo)).setText(getIntent().getStringExtra("titulo"));
		((TextView)findViewById(R.id.descripcion)).setText(getIntent().getStringExtra("descripcion"));
		
    	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	locationListener = new PaseoLocationListener();
    	
    	Criteria crit = new Criteria();
    	crit.setAccuracy(Criteria.ACCURACY_FINE);
    	locManager.getLastKnownLocation(locManager.getBestProvider(crit, true));
		
	}
    	
	public void onResume() {

		super.onResume();
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
	}

	public void onPause() {
		super.onPause();
		locManager.removeUpdates(locationListener);
	}
	
	private class PaseoLocationListener implements LocationListener {
	
    	public void onLocationChanged(Location location) {
    		// TODO Auto-generated method stub
    		if (location != null) {
    			//Toast.makeText(getBaseContext(),"Location changed",Toast.LENGTH_LONG).show();
    			((TextView)findViewById(R.id.gps)).setText("Posición: "+location.getLatitude()+"/"+location.getLongitude()+"/"+location.getAccuracy());
    			SoundPoint nl = new SoundPoint(location);
    			Log.d("AREAGO","Location changed");
    			
    			// Comparamos punto actual con puntos del paseo
    			// Si nos hemos movido
    			walk.check_collisions(nl);
    			}
    		}

    	public void onProviderDisabled(String provider) {
    		// TODO Auto-generated method stub
    		((TextView)findViewById(R.id.gps)).setText(provider+" desconectado");
    		Log.d("AREAGO","Disable");
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
