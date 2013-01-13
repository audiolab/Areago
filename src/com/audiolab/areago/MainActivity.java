package com.audiolab.areago;



import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

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
        findViewById(R.id.imageView1).setClickable(false);
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        //Cargar datos de las preferencias par las globales
        SharedPreferences appPrefs = getSharedPreferences("com.audiolab.areago_preferences",MODE_PRIVATE);
        
        

        
        
        if (!appPrefs.contains("editUrlServer")) {
        	SharedPreferences.Editor prefEditor = appPrefs.edit();
        	prefEditor.putString("editUrlServer", "http://www.xavierbalderas.com/areago/areago/listado");
        	prefEditor.commit();
        	// TODO: No me gusta como se visualiza la actividad.. la inserto a mano de momento hasta mejorar esta opción.
//        	Toast.makeText(getBaseContext(),"No está configurado el servidor de descarga de paseos. Configuramos con el servidor por defecto.",Toast.LENGTH_LONG).show();
//        	Intent i = new Intent("com.audiolab.areago.AreagoPreferences");
//    		startActivity(i);
        	
        	
//    		i = getIntent();
//    		finish();
//    		startActivity(i);
        }
        url = appPrefs.getString("editUrlServer", "");
        
        setTitle("AREAGO : Inicio");

        //Wireless

        if (!init_wireless()) { 
        	Log.d("AREAGO","Wireless Inactivo. No podras descargar paseos nuevos.");
        	showDialog(1);
        }
        else { 
        	Log.d("AREAGO","Wireless Activado");
        	}
        findViewById(R.id.imageView1).setClickable(true);
        
        //GPS
        if (!init_gps()) {
        	Log.d("AREAGO","Esta apagado del GPS");
        	findViewById(R.id.imageView1).setClickable(false);
        	showDialog(0);
        } else {
//        	t.setText(t.getText()+"GPS Activado\n");
        }
        //dialog.dismiss();
        
        //Storage
        // TODO: Comprobar el estado de la SD
//        t.setText(t.getText()+"Comprobando tarjeta de memoria - ");
        if ( !android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED) ) {	
//        	t.setText(t.getText()+"SDCard no es accesible, comprobar antes de continuar\n");
        	showDialog(2);
        	findViewById(R.id.imageView1).setClickable(false);
        	return;
        }
        
//        t.setText(t.getText()+"Pulsa sobre la imagen para continuar\n");
    }

    
    private boolean init_gps() {
    	// TODO Auto-generated method stub
    	locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    	if ( !locManager.isProviderEnabled("gps") ) return false;
    	// Lo desactivo mientras no gestionemos aquí la carga el punto donde se encuentra el paseante.
    	/* 
    	locationListener = new AreagoLocationListener();
    	
    	Criteria crit = new Criteria();
    	crit.setAccuracy(Criteria.ACCURACY_FINE);
    	locManager.getLastKnownLocation(locManager.getBestProvider(crit, true));
    	*/
    	return true;
	}

    private class AreagoLocationListener implements LocationListener {
    	
    	public void onLocationChanged(Location location) {
    		// TODO Auto-generated method stub
    		if (location != null) {
    			//Toast.makeText(getBaseContext(),"Location changed",Toast.LENGTH_LONG).show();
    			((TextView)findViewById(R.id.gps)).setText("Posición: "+location.getLatitude()+"/"+location.getLongitude()+"/"+location.getAccuracy());
    			lat = location.getLatitude();
    			lon = location.getLongitude();
    		}
    		
    	}

    	public void onProviderDisabled(String provider) {
    		// TODO Auto-generated method stub
    		((TextView)findViewById(R.id.gps)).setText(provider+" desconectado");
    		
    		
    	}

    	public void onProviderEnabled(String provider) {
    		// TODO Auto-generated method stub
    		((TextView)findViewById(R.id.gps)).setText("GPS Conectado:"+provider);
    		

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
    	}

    }


	private boolean init_wireless() {
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	return mWifi.isAvailable();
	}
	
	private boolean isConnected() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		builder = new AlertDialog.Builder(this);
		switch(id) {
		case 0: // GPS
			builder.setMessage("Inicie el GPS para arrancar AREAGO")
	        .setPositiveButton("Configurar", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	            	Intent i = getIntent();
	    			finish();
	    			startActivity(i);
	            }
	        });
			break;
		case 1: // Wifi
			builder.setMessage("Inicie el Wifi para arrancar AREAGO")
	        .setPositiveButton("Configurar", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
	            	Intent i = getIntent();
	    			finish();
	    			startActivity(i);
	            }
	        });
			break;
		case 2: // Storage
			builder.setMessage("Comprueba el acceso a su tarjeta de memoria")
	        .setPositiveButton("Configurar", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	startActivity(new Intent(android.provider.Settings.ACTION_MEMORY_CARD_SETTINGS));
	            	Intent i = getIntent();
	    			finish();
	    			startActivity(i);
	            }
	        });
			break;
		}
		return builder.create();
	}

public void onResume() {

	super.onResume();
	//locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
}

public void onPause() {
	super.onPause();
	//locManager.removeUpdates(locationListener);
}

	public void onClick(View view) {
    	
    				final ProgressDialog dialog = ProgressDialog.show(this,"Cargando rutas","Espera...",true);
    				
    				new Thread(new Runnable(){
    					public void run(){
    					try {
    							// TODO: Activar cuando esté en funcionamiento la web
    						Intent i = new Intent("com.audiolab.areago.ListActivityPaseos");
    						if (isConnected()) i.putExtra("json", init_rutas());
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
	    // Handle item selection
	    switch (item.getItemId()) {
//	    	case R.id.preferences:
//	    		//Toast.makeText(this,"En construcción..",Toast.LENGTH_LONG).show();
//	    		Intent i = new Intent("com.audiolab.areago.AreagoPreferences");
//	    		startActivity(i);
//	    		return true;
	    	case R.id.exit:
	    		System.exit(0);
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
