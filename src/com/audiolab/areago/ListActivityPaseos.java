package com.audiolab.areago;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
	
	//ArrayList<Paseo> walks = new ArrayList<Paseo>();
	HashMap<Integer,Paseo> walks = new HashMap<Integer,Paseo>();
	File or;
	File fold;
	FileInputStream fIn;
	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
	private ProgressDialog mProgressDialog;
	View vClicked;
	
	String PATH_PASEOS;
	
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scroll_walks);
		setTitle("AREAGO : Paseos");

		try {
			or = Environment.getExternalStorageDirectory();
		}
		catch (RuntimeException e) {
			Log.e("AREAGO","Error al cargar el sistema de ficheros externo");
			return;
		}
		
		PATH_PASEOS = or.getAbsolutePath() + "/Areago";
		
		/// Miramos la disponibilidad de paseos en la máquina
		/// Añadimos a Walks los paseos descargados
		get_PaseosDescargados();
		
		/// Cargamos paseos online (hemos cargado el archivo en el activity anterior)
		String str = getIntent().getStringExtra("json");
		get_PaseosOnline(str);
		
		// Creamos la lista de paseos recorriendo walks
		
		LinearLayout l = (LinearLayout)findViewById(R.id.layout_general);
		
		try {
			
			Set set = walks.entrySet();
			Iterator iter = set.iterator();
			
			//for (int i = 0; i<walks.size();i++) {
			while (iter.hasNext()) {
				
				//Paseo p = (Paseo) iter.next();
				Map.Entry me = (Map.Entry)iter.next();
				Paseo p = (Paseo) me.getValue();
				
				ScrollView sv = new ScrollView(this);
				
				LayoutParams params = new LinearLayout.LayoutParams(300, LayoutParams.WRAP_CONTENT);
				LinearLayout layout = new LinearLayout(this);
				layout.setOrientation(LinearLayout.VERTICAL);
				layout.setBackgroundResource(R.color.white);
				//layout.setId(p.getId());
				
				ImageView img = new ImageView(this);
				img.setLayoutParams(params);
				img.setImageResource(R.drawable.areago_48dp);
				layout.addView(img);
				img.setClickable(true);
				img.setOnClickListener(this);
				img.setId(p.getId());
				//img.setId(i);


				TextView tv;
				
				//Titulo
//				TextView tv = new TextView(this);
//				tv.setBackgroundResource(R.color.white);
//				tv.setTextColor(Color.BLACK);
//				tv.setTypeface(null,Typeface.BOLD);
//				tv.setText("Nombre:");
//				tv.setLayoutParams(params);
//				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setBackgroundResource(R.color.white);
				tv.setTextColor(Color.BLACK);
				tv.setTypeface(null,Typeface.BOLD);
				tv.setText(String.valueOf(p.getId()) + " - "+p.getTitle());
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				//ID
//				tv = new TextView(this);
//				tv.setBackgroundResource(R.color.white);
//				tv.setTextColor(Color.BLACK);
//				tv.setTypeface(null,Typeface.BOLD);
//				tv.setText("ID:");
//				tv.setLayoutParams(params);
//				layout.addView(tv);
//				
//				
//				tv = new TextView(this);
//				tv.setBackgroundResource(R.color.white);
//				tv.setTextColor(Color.BLACK);
//				tv.setText(String.valueOf(p.getId()));
//				tv.setLayoutParams(params);
//				layout.addView(tv);
//				tv.setTextColor(Color.BLACK);
				
				// Descripcion
//				tv = new TextView(this);
//				tv.setBackgroundResource(R.color.white);
//				tv.setTextColor(Color.BLACK);
//				tv.setText("Descripcion:");
//				tv.setTypeface(null,Typeface.BOLD);
//				tv.setLayoutParams(params);
//				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setBackgroundResource(R.color.white);
				tv.setTextColor(Color.BLACK);
				tv.setText(p.getDescription());
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				// Update
//				tv = new TextView(this);
//				tv.setBackgroundResource(R.color.white);
//				tv.setTextColor(Color.BLACK);
//				tv.setText("Update:");
//				tv.setTypeface(null,Typeface.BOLD);
//				tv.setLayoutParams(params);
//				layout.addView(tv);
				
				
				
				
				// Download
//				tv = new TextView(this);
//				tv.setBackgroundResource(R.color.white);
//				tv.setTextColor(Color.BLACK);
//				tv.setText("Descarga:");
//				tv.setTypeface(null,Typeface.BOLD);
//				tv.setLayoutParams(params);
//				layout.addView(tv);
				
				tv = new TextView(this);
				tv.setBackgroundResource(R.color.white);
//				tv.setText("\nDESCARGADO");
				
				if (!p.isDownload()) {
					tv.setTextColor(Color.RED);
					tv.setText("\nNECESITA SER DESCARGADO");
//					layout.setBackgroundColor(0xFF0000FF);
					layout.setBackgroundResource(R.color.red);
				} else {
					TextView tvu = new TextView(this);
					tvu.setBackgroundResource(R.color.white);
					tvu.setTextColor(Color.RED);
//					tvu.setText("\nACTUALIZADO");
					if (!p.isUpdate()) { 
						tvu.setText("\nNECESITA ACTUALIZACION");
//						layout.setBackgroundColor(0x55FF0000);
						layout.setBackgroundResource(R.color.blue);
					}
					tvu.setLayoutParams(params);
					layout.addView(tvu);
				}
				tv.setLayoutParams(params);
				layout.addView(tv);
				
				
				LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);

				sv.addView(layout,layoutParam);
				sv.setBackgroundResource(R.color.white);
				
				l.addView(sv);
				l.setBackgroundResource(R.color.white);
				
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

				Paseo walk = new Paseo(jObject.getInt("id"));
				walk.setTitle(jObject.getString("nombre"));
				walk.setDescription(jObject.getString("resumen"));
				walk.hash = jObject.getString("hash");

			if (!walk.exist(walks)) {
				walk.downlad=false; // no esta descargado
				walk.update=true; // actualizado por defecto
				walks.put(jObject.getInt("id"),walk);
			}
		} 
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void get_PaseosDescargados() {

		fold = new File(PATH_PASEOS);
        if (!fold.isDirectory()) fold.mkdir(); // La primera vez que se entra no hay directorio creado
        File[] fpaseos = fold.listFiles();
                
        if (fpaseos.length == 0) Toast.makeText(this,"No hay paseos en memoria",Toast.LENGTH_LONG).show();
        
        for (int i = 0; i<fpaseos.length; i++) {
        	Writer writer = new StringWriter();
        	if ( fpaseos[i].isDirectory() ) {
        	try {
//        		File jsondata = new File(fpaseos[i]+"/data.json");
        		File jsondata = new File(fpaseos[i]+"/info.json");
        		char[] buffer = new char[1024];
        		Reader reader = new BufferedReader(new FileReader(jsondata));
        		int n;
        		while ((n = reader.read(buffer)) != -1) {
        			writer.write(buffer,0,n);
        		}
        		reader.close();
        		
        	} catch (Exception e) {
        		Log.d("AREAGO","Error: "+e);
        	}

        	String JSONString = "";
        	JSONString = writer.toString();

        	//Parseamos el fichero data.json (objetoJson con id/descripcion/..
   
       		try {
				JSONObject jObject = new JSONObject(JSONString);
				
				Paseo walk = new Paseo(jObject.getInt("id"));
				walk.setTitle(jObject.getString("name"));
				// La descripción es muy larga.. trabajos con excerpt
				//walk.setDescription(jObject.getString("description"));
				walk.setDescription(jObject.getString("excerpt"));
				walk.hash = jObject.getString("hash");
				walk.JsonPoints = jObject.getString("points");
				//walks.add(walk);
				//walks.add(jObject.getInt("id"),walk);
				walk.downlad=true; // El paseo ya está descargado
				walk.update=true; // Esta actualizado por defecto
				walks.put(jObject.getInt("id"),walk);
				
				Log.d("AREAGO","Cargando el paseo: "+walk.titulo+" - ID:"+walk.id);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	}
  
        }
	}


	public void onClick(View v) {
		// TODO Auto-generated method stub
		vClicked = v;
		
		// Si no existe en memoria lo descargamos
		if (!isExternalStorageWritable()) {
			Toast.makeText(this,"No se puede escribir en la tarjeta",Toast.LENGTH_LONG).show();
			return;
		}
		
		// Comprobar si ya está descargado (existe un paseo con el mismo ID en la carpeta de AREAGO
		Paseo p = (Paseo) walks.get(v.getId());
		
		if (p.isDownload() && p.isUpdate()) {
			start_walk();
		} else if (p.isDownload() && !p.isUpdate()) {
			//TODO: Actualizar los ficheros
		} else if (!p.isDownload()) {
			Log.d("AREAGO","Descargamos el zip "+fold.getAbsolutePath());
			// descargamos zip del paseo con el directorio oportuno
			startDownload();
		}
	}
	
	public void start_walk() {
		
		Intent i = new Intent("com.audiolab.areago.PaseoPreview");
		Paseo p = (Paseo) walks.get(vClicked.getId());
		i.putExtra("json", p.JsonPoints);
		i.putExtra("lat", "333");
		i.putExtra("lon", "222"); 
		i.putExtra("descripcion", p.descripcion);
		i.putExtra("titulo", p.titulo);
		i.putExtra("id", p.id);
		Log.d("AREAGO","Arrancamos el paseo: "+p.id+p.JsonPoints);

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
	
	// Gestion del progress bar
	private void startDownload() {
        String url = "http://www.xavierbalderas.com/areago/areago/descarga/"+vClicked.getId();
        new DownloadFileAsync().execute(url);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
		case DIALOG_DOWNLOAD_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage("Descargando archivos paseo...");
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(true);
			mProgressDialog.show();
			return mProgressDialog;
		default:
			return null;
        }
    }
	
	
	// Clase para gestionar las descargas
	class DownloadFileAsync extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(DIALOG_DOWNLOAD_PROGRESS);
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			int count;
			try {
				URL url = new URL(arg0[0]);
				URLConnection conexion;
				try {
					conexion = url.openConnection();
					conexion.connect();
				} catch (IOException e) {
					Log.e("AREAGO","No encuentra el ZIP del paseo - "+e);
					return null;
				}
				
				InputStream input = new BufferedInputStream(url.openStream()); // ZIP file
				Log.d("AREAGO","Stream input"+input.toString());
				
				ZipInputStream zin = new ZipInputStream(input);
				ZipEntry ze = null;
				
				//Creamos el directorio con el ID donde se decargar el archivo
				String path_walk = PATH_PASEOS + "/"+vClicked.getId();
				fold = new File(path_walk);
			    if (!fold.isDirectory()) fold.mkdir();
				
				while ((ze = zin.getNextEntry()) != null) {
					 Log.v("Decompress", "Unzipping " + ze.getName());
					 long lenghtOfFile = ze.getSize();
					 if (ze.isDirectory()) {
						 // TODO: Que tengo que hacer? crear el directorio?
						 fold = new File(path_walk + "/"+ze.getName());
					     if (!fold.isDirectory()) fold.mkdir();
					 } else {
						 OutputStream output = new FileOutputStream(path_walk+"/"+ze.getName());
						 byte data[] = new byte[1024];
						 long total = 0;
						 while ((count = zin.read(data)) != -1 ) {
							 total += count;
							 publishProgress(""+(int)((total*100)/lenghtOfFile));
							 output.write(data, 0, count);
						 }
						
						output.flush();
						output.close();
					 }
					 zin.closeEntry();
				}
				zin.close();
				input.close();
				
			} catch (Exception e) {
				Log.e("AREAGO","ERROR al descargar paseo"+e);
			}
			return null;
		}
		
		protected void onProgressUpdate(String... progress) {
			 //Log.d("ANDRO_ASYNC",progress[0]);
			 mProgressDialog.setProgress(Integer.parseInt(progress[0]));
		}

		@Override
		protected void onPostExecute(String unused) {
			dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
			// reiniciamos actividad para cargar los nuevos datos
			Intent i = getIntent();
			finish();
			startActivity(i);
		}
		
	}
}
