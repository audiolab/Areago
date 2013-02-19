package com.audiolab.areago;



import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	ProgressDialog dialog;
	String url = "http://www.xavierbalderas.com/areago/areago/listado";
	String string="";
	LocationManager locManager;
	LocationListener locationListener;
	Double lat;
	Double lon;
	
	
	
	private InputStream OpenHttpConnection (String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
	
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
	
		if (!(conn instanceof HttpURLConnection))
			throw new IOException("not http connection");
		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setConnectTimeout(5000);
			httpConn.connect();
			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK){
					in = httpConn.getInputStream();
			}
		} catch (Exception ex) {
			Log.d("AREAGO", ex.getLocalizedMessage());
			throw new IOException("Error connecting");
		}
		
		return in;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	// TODO: En Android 4 hace falta añadirle el boton de preferences
    	// TODO: Hay que entrar y salir para volver a cargar los paseos.
    	// TODO: 	Lanzamiento de capas a traves de triggers
    	// 			Existe una capa 0 que es la básica que siempre se ejecuta, y luego hay capas que se entran a partir de triggers
    	//			En la capa 0 se pueden poner ambientes... y en las otras capas se pueden añadir otras historias con el Atributo CAPA
    	//			Cambiamos en properties del feature una propiedad que sea capa
    	//			Añadimos un nuevo tipo llamado CHANGE_LAYER con un atributo CAPA_DESTINO
    	
    	
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);        
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        setTitle("AREAGO : Inicio");
        
        //Cargar datos de las preferencias par las globales
        SharedPreferences appPrefs = getSharedPreferences("com.audiolab.areago_preferences",MODE_PRIVATE);
        
        if (!appPrefs.contains("editUrlServer")) {
        	SharedPreferences.Editor prefEditor = appPrefs.edit();
        	prefEditor.putString("editUrlServer", "http://www.xavierbalderas.com/areago/areago/listado");
        	prefEditor.commit();
        	// TODO: No me gusta como se visualiza la actividad.. la inserto a mano de momento hasta mejorar esta opción.
        }
        url = appPrefs.getString("editUrlServer", "");
        

    }

    
    private boolean init_gps() {
    	// TODO Auto-generated method stub
    	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	if ( !locManager.isProviderEnabled("gps") ) return false;
    	return true;
	}

//    private class AreagoLocationListener implements LocationListener {
//    	
//    	public void onLocationChanged(Location location) {
//    		// TODO Auto-generated method stub
//    		if (location != null) {
//    			//Toast.makeText(getBaseContext(),"Location changed",Toast.LENGTH_LONG).show();
//    			((TextView)findViewById(R.id.gps)).setText("Posición: "+location.getLatitude()+"/"+location.getLongitude()+"/"+location.getAccuracy());
//    			lat = location.getLatitude();
//    			lon = location.getLongitude();
//    		}
//    		
//    	}
//
//    	public void onProviderDisabled(String provider) {
//    		// TODO Auto-generated method stub
//    		((TextView)findViewById(R.id.gps)).setText(provider+" desconectado");
//    		
//    		
//    	}
//
//    	public void onProviderEnabled(String provider) {
//    		// TODO Auto-generated method stub
//    		((TextView)findViewById(R.id.gps)).setText("GPS Conectado:"+provider);
//    		
//
//    	}
//
//    	public void onStatusChanged(String provider, int status, Bundle extras) {
//    		// TODO Auto-generated method stub
//    		String st = "";
//    		switch (status) {
//    		case android.location.LocationProvider.AVAILABLE:
//    			st="Disponible";
//    		case android.location.LocationProvider.OUT_OF_SERVICE:
//    			st="Desactivado";
//    		case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
//    			st="Temporalmente desactivado";
//    		}
//    		((TextView)findViewById(R.id.gps)).setText("GPS Status:"+st);
//    	}
//
//    }


	private boolean isWifiEnabled() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    	return wifiManager.isWifiEnabled();
	}
	
	private boolean isDataConnected() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getActiveNetworkInfo();
		if (mWifi != null) return mWifi.isConnected();
		return false;
	}
	
public void onResume() {
	
	super.onResume();
	
	// Iniciamos la animación
	Animation fadeIN = AnimationUtils.loadAnimation(this, R.anim.fadein);
	
	findViewById(R.id.imageView1).setClickable(false);
	findViewById(R.id.imageView1).setVisibility(View.VISIBLE);
	findViewById(R.id.imageView1).startAnimation(fadeIN);
	
	// Lo cambiamos para que tenga algo de delay
	fadeIN = AnimationUtils.loadAnimation(this, R.anim.fadein_largo);
	
	((TextView)findViewById(R.id.ErrorWifi)).setVisibility(View.INVISIBLE);
	((TextView)findViewById(R.id.ErrorGPS)).setVisibility(View.INVISIBLE);
	((TextView)findViewById(R.id.ErrorStorage)).setVisibility(View.INVISIBLE);
	
	//Wireless
    if (!isWifiEnabled()) { 
    	Log.d("AREAGO","Wireless Inactivo. No podras escuchar los puntos Wifi.");
    	findViewById(R.id.imageView1).setClickable(false);
    	findViewById(R.id.ErrorWifi).setClickable(true);
    	((TextView)findViewById(R.id.ErrorWifi)).setText(R.string.error_wifi);
    	((TextView)findViewById(R.id.ErrorWifi)).setVisibility(View.VISIBLE);
    	((TextView)findViewById(R.id.ErrorWifi)).startAnimation(fadeIN);

    }
    //GPS
    else if (!init_gps()) {
    	Log.d("AREAGO","Esta apagado del GPS");
    	findViewById(R.id.imageView1).setClickable(false);
    	findViewById(R.id.ErrorGPS).setClickable(true);
    	((TextView)findViewById(R.id.ErrorGPS)).setText(R.string.error_gps);
    	((TextView)findViewById(R.id.ErrorGPS)).setVisibility(View.VISIBLE);
    	((TextView)findViewById(R.id.ErrorGPS)).startAnimation(fadeIN);
    }  
    	//Storage
    else if ( !android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) ) {	
        findViewById(R.id.imageView1).setClickable(false);
        ((TextView)findViewById(R.id.ErrorStorage)).setText(R.string.error_no_escribir_tarjeta);
    	((TextView)findViewById(R.id.ErrorStorage)).setVisibility(View.VISIBLE);
    	((TextView)findViewById(R.id.ErrorStorage)).startAnimation(fadeIN);
        return;
    }
    	//Wireless
    else if (!isDataConnected()) { 
        	Log.d("AREAGO","Sin conexión de datos. No se decaragarn nuevos paseos.");
        	findViewById(R.id.imageView1).setClickable(true); // permitimos seguir con los paseos en memeria
        	findViewById(R.id.ErrorWifi).setClickable(true);
        	((TextView)findViewById(R.id.ErrorWifi)).setText(R.string.error_data);
        	((TextView)findViewById(R.id.ErrorWifi)).setVisibility(View.VISIBLE);
        	((TextView)findViewById(R.id.ErrorWifi)).startAnimation(fadeIN);
    } else {
    	findViewById(R.id.imageView1).setClickable(true);
    }
}

public void onPause() {
	super.onPause();
}

	public void EnableWifi (View v) {
		startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
    	Intent i = getIntent();
		startActivity(i);
	}

	public void EnableGPS (View v) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        Intent i = getIntent();
		startActivity(i);
	}
	
	public void checkStorage (View v) {
		startActivity(new Intent(android.provider.Settings.ACTION_MEMORY_CARD_SETTINGS));
    	Intent i = getIntent();
		startActivity(i);
	}

	public void onClick(View view) {
    	
    				final ProgressDialog dialog = ProgressDialog.show(this,getString(R.string.cargando_rutas),getString(R.string.espera),true);
    				
    				new Thread(new Runnable(){
    					public void run(){
    					try {
    							// TODO: Activar cuando esté en funcionamiento la web
    						Intent i = new Intent("com.audiolab.areago.ListActivityPaseos");
    						if (isDataConnected()) i.putExtra("json", init_rutas());
    						dialog.dismiss();
    						startActivity(i);
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
    					}

						private String init_rutas() {
							// TODO Auto-generated method stub
							int BUFFER_SIZE = 2000;
							String str = "";
		        			InputStream in = null;
		        			try {
		        				in = OpenHttpConnection(url);
		        				Log.d("AREAGO","Abriendo conexión..");
		        			} catch (Exception e) {
		        				Log.d("AREAGO",e.getLocalizedMessage());
		        				dialog.dismiss();
		        				return str;
		        			}
		        			
		        			InputStreamReader isr = new InputStreamReader(in);
		        			int charRead;
		        			
		        			char[] inputBuffer = new char[BUFFER_SIZE];
		        			try {
		        				while ((charRead = isr.read(inputBuffer))>0) {
		        					String readString = String.copyValueOf(inputBuffer,0,charRead);
		        					str += readString;
		        					inputBuffer = new char[BUFFER_SIZE];
		        					
		        				}
		        				in.close();
		        			} catch (IOException e) {
		        				Log.d("AREAGO",e.getLocalizedMessage());
		        				str = "";
		        				dialog.dismiss();
		        				return str;
		        			}   			
		        			
		        			dialog.dismiss();
							return str;
						}
    				}).start();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
	    switch (item.getItemId()) {
	    	case R.id.exit:
	    		System.exit(0);
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
