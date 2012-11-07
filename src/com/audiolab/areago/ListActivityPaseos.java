package com.audiolab.areago;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class ListActivityPaseos extends ListActivity implements View.OnClickListener {
	
	ArrayList<Paseo> walks = new ArrayList<Paseo>();
	File or;
	File fold;
	FileInputStream fIn;
	
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scroll_walks);
		setTitle("AREAGO : Paseos");

		/// Miramos la disponibilidad de paseos en la máquina
		/// Añadimos a Walks los paseos descargados
		get_PaseosDescargados();
		
		/// Cargamos paseos online (hemos cargado el archivo en el activity anterior)
		String str = getIntent().getStringExtra("json");
		get_PaseosOnline(str);
		
		// Creamos la lista de paseos recorriendo walks
		
		LinearLayout l = (LinearLayout)findViewById(R.id.layout_general);
		
		try {
			for (int i = 0; i<walks.size();i++) {
				
				ScrollView sv = new ScrollView(this);
				
				LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				LinearLayout layout = new LinearLayout(this);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setId(walks.get(i).id);
				
				ImageView img = new ImageView(this);
				img.setLayoutParams(params);
				img.setImageResource(R.drawable.audifonos);
				layout.addView(img);
				img.setClickable(true);
				img.setOnClickListener(this);
				//img.setId(walks.get(i).id);
				img.setId(i);
				
				//Titulo
				TextView tv = new TextView(this);
				tv.setText("Nombre:");
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setText(walks.get(i).titulo);
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				// Descripcion
				
				tv = new TextView(this);
				tv.setText("Descripcion:");
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setText(walks.get(i).descripcion);
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				// Update
				tv = new TextView(this);
				tv.setText("Update:");
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setText("ACTUALIZADO");
				if (walks.get(i).update) { 
					tv.setText("NECESITA ACTUALIZACIÓN DE LA VERSION LOCAL");
					layout.setBackgroundColor(0x55FF0000);
				}
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				
				LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);

				sv.addView(layout,layoutParam);
				
				l.addView(sv);
				
			}
		
		} catch (Exception ex) {
			Log.d("ERROR","Exception..");
		}
		
	}

	private void get_PaseosOnline(String str) {

		JSONArray jArray;
		try {
			jArray = new JSONArray(str);
		
			for (int i = 0; i<jArray.length();i++) {

				JSONObject jObject = jArray.getJSONObject(i);

				Paseo walk = new Paseo(jObject.getInt("id"),jObject.getString("name"),jObject.getString("description"));
				//walk.hash = jObject.getInt("hash");

			if (!walk.exist(walks)) {
				walks.add(walk);
			}
		} 
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void get_PaseosDescargados() {
		// TODO Auto-generated method stub
		or = Environment.getExternalStorageDirectory();
        fold = new File(or.getAbsolutePath() + "/areago");
        if (!fold.isDirectory()) fold.mkdir();
        File[] fpaseos = fold.listFiles();
        
        String JSONString = "";
        
        if (fpaseos.length == 0) Toast.makeText(this,"No hay paseos en memoria",Toast.LENGTH_LONG).show();
        
        for (int i = 0; i<fpaseos.length; i++) {
        	Writer writer = new StringWriter();
        	try {
        		File jsondata = new File(fpaseos[i]+"/data.json");
        		Log.d("AREAGO","File paseo: "+fpaseos[i]);
        		StringBuilder text = new StringBuilder();
        		
        		
        		char[] buffer = new char[1024];
        		Reader reader = new BufferedReader(new FileReader(jsondata));
        		int n;
        		while ((n = reader.read(buffer)) != -1) {
        			writer.write(buffer,0,n);
        		}
        		
        	} catch (Exception e) {
        		Log.d("AREAGO","Error: "+e);
        	}

        	JSONString = "";
        	JSONString = writer.toString();

        	//Parseamos el fichero data.json (objetoJson con id/descripcion/..
   
       		try {
				JSONObject jObject = new JSONObject(JSONString);
				
				Paseo walk = new Paseo(jObject.getInt("id"),jObject.getString("name"),jObject.getString("description"));
				walk.hash = jObject.getInt("hash");
				walks.add(walk);
				
				Log.d("AREAGO","Cargando el paseo: "+walk.titulo);
				Log.d("AREAGO","String: "+JSONString);
				
//				listINFO.add(jObject.getString("name"));
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
  
        }
	}


	public void onClick(View v) {
		// TODO Auto-generated method stub
		// Si no existe en memoria lo descargamos
		if (!isExternalStorageWritable()) {
			Toast.makeText(this,"No se puede escribir en la tarjeta",Toast.LENGTH_LONG).show();
		}
		
		
		// Si ya está descargado comprobamos si hay que actualizar
        fold = new File(or.getAbsolutePath() + "/areago" + "/"+v.getId());
        Toast.makeText(this,or.getAbsolutePath() + "/areago" + "/"+v.getId(),Toast.LENGTH_LONG).show();

		final ProgressDialog dialog = ProgressDialog.show(this,"Actualizando","Espera...",true);
		new Thread(new Runnable(){
			public void run(){
			try {
					Thread.sleep(5000);
					dialog.dismiss();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		
		Toast.makeText(this,"ID: "+v.getId(),Toast.LENGTH_LONG).show();
		
		// Esperamos a que acabe el thread de descarga
		// Falta investigar como esperarlo.. 
		// Posibilidad de trabajar con Asynctask, doINBackground / onPostExecute
		
		// Si ya está actualizado, podemos arrancar el paseo
		
		Intent i = new Intent("com.audiolab.areago.PaseoPreview");
		i.putExtra("json", walks.get(v.getId()).JsonPoints);
		i.putExtra("lat", "333");
		i.putExtra("lon", "222"); 
		i.putExtra("descripcion", walks.get(v.getId()).descripcion);
		i.putExtra("titulo", walks.get(v.getId()).titulo);
		i.putExtra("id", walks.get(v.getId()).id);

		startActivity(i);
	}
	
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        Log.e("AREAGO", "Sistema correcto para escribir"+state);
	        return true;
	    }
	    return false;
	}
	
}
