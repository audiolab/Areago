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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	ProgressDialog dialog;
	String url = "http://www.xavierbalderas.com/areago/areago/listado";
	
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
			httpConn.connect();
			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK){
					in = httpConn.getInputStream();
			}
		} catch (Exception ex) {
			Log.d("Networking", ex.getLocalizedMessage());
			throw new IOException("Error connecting");
		}
		
		return in;
	}
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("AREAGO : Inicio");
    }
    
    public void onClick(View view) {
    	
    				dialog = ProgressDialog.show(MainActivity.this, "Cargando datos", "Espera mientras cargamos los datos de las rutas...", true);

        			int BUFFER_SIZE = 2000;
        			InputStream in = null;
        			String str="";
        			try {
        				in = OpenHttpConnection(url);
        			} catch (Exception e) {
        				Log.d("Networking",e.getLocalizedMessage());
        				str = "";
        				Toast.makeText(this,"No se puede conectar con el servidor",Toast.LENGTH_LONG).show();
        				dialog.dismiss();
        				return;
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
        				Log.d("Networking",e.getLocalizedMessage());
        				str = "";
        				return;
        			}   			
        			
        			dialog.dismiss();
        		
        			Intent i = new Intent("com.audiolab.areago.ListActivityPaseos");
        			i.putExtra("json", str);
        	
        			Toast.makeText(this,"Datos cargados con éxito",Toast.LENGTH_LONG).show();
        		
   			 		startActivity(i);
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
	    	case R.id.preferences:
	    		Toast.makeText(this,"En construcción..",Toast.LENGTH_LONG).show();
	    		return true;
	    	case R.id.exit:
	    		System.exit(0);
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
    
    //Carga de los datos de preferencias y las rutas

}
