package com.audiolab.areago;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


public class ListActivityPaseos extends ListActivity implements View.OnClickListener {
	
	ArrayList<Paseo> walks = new ArrayList<Paseo>();
	ArrayList<String> listINFO = new ArrayList<String>();
	private FileWriter fWriter;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scroll_walks);
		setTitle("AREAGO : Paseos");
		
		String str = getIntent().getStringExtra("json");
		
		
		LinearLayout l = (LinearLayout)findViewById(R.id.layout_general);
		
		try {
			JSONArray jArray = new JSONArray(str);
			for (int i = 0; i<jArray.length();i++) {
				JSONObject jObject = jArray.getJSONObject(i);
				listINFO.add(jObject.getString("name"));
				Paseo walk = new Paseo();
				walk.id = jObject.getInt("id");
				walk.titulo = jObject.getString("name");
				walk.descripcion = jObject.getString("description");
				walks.add(walk);
				
				ScrollView sv = new ScrollView(this);
				
				LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				LinearLayout layout = new LinearLayout(this);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setId(jObject.getInt("id"));
				
				ImageView img = new ImageView(this);
				img.setLayoutParams(params);
				img.setImageResource(R.drawable.audifonos);
				layout.addView(img);
				img.setClickable(true);
				img.setOnClickListener(this);
				img.setId(jObject.getInt("id"));
				
				//Titulo
				TextView tv = new TextView(this);
				tv.setText("Nombre:");
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setText(jObject.getString("name"));
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				// Descripcion
				
				tv = new TextView(this);
				tv.setText("Descripcion:");
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setText(jObject.getString("description"));
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				
				LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);

				sv.addView(layout,layoutParam);
				
				l.addView(sv);
				
			}
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d("ERROR","Json Error");
		} catch (Exception ex) {
			Log.d("ERROR","Exception..");
		}
		
	}


	public void onClick(View v) {
		// TODO Auto-generated method stub
		// Si no existe en memoria lo descargamos
		if (!isExternalStorageWritable()) {
			Toast.makeText(this,"No se puede escribir en la tarjeta",Toast.LENGTH_LONG).show();
		}
		File or = Environment.getExternalStorageDirectory();
        File fold = new File(or.getAbsolutePath() + "/areago");
        if (!fold.isDirectory()) fold.mkdir();
		
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
