package com.audiolab.areago;

import java.util.ArrayList;
import java.util.HashMap;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.os.Vibrator;
import android.util.Log;

public class Paseo {
	
	int id ;
	String titulo;
	String idioma;
	String descripcion;
	int num_grabaciones;
	int tamano;
	SoundPoint pref = new SoundPoint("paseo_reference"); // Punto de referencia de inicio / lat-lon
	String hash = "";
	boolean update=false; // ya está descargado pero necesita ser actualizado porque el hash es diferente
	boolean downlad=false; // necesita ser descargado?
	String JsonPoints; // Listado de puntos del mapa en Json Array
	ArrayList<SoundPoint> puntos = new ArrayList<SoundPoint>();
	Vibrator vibrator;
	
	// Creadoras
	public Paseo(int id) {
		this.id = id;
	}
	
	// Modificadoras
	public void setTitle(String t) {
		this.titulo = t;
	}
	
	public void setDescription(String d) {
		this.descripcion = d;
	}
	
	public void setVibrator(Vibrator v) {
		this.vibrator = v;
	}
	
	// Consultoras
	public String getTitle() {
		return this.titulo; 
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getDescription() {
		return this.descripcion;
	}
	
	public boolean isUpdate() { //esta actualizado? false no está actualizado 
		return this.update;
	}
	
	public boolean isDownload() { // esta descargado?
		return this.downlad;
	}
	
	// Acciones
	public void stop() {
		for (int i = 0; i<this.puntos.size(); i++) {
			this.puntos.get(i).stopSoundFile();
		}
	}
	
	public void create_points(String str) {
		// Crear los puntos a partir del Objeto Points del GeoJSON
		//
		
		JSONObject points;
		try {
			
			points = new JSONObject(str);
			//String type = points.getString("type"); no lo uso
			JSONArray features = points.getJSONArray("features"); // aquí van los puntos

		
			for (int i = 0; i<features.length();i++) {
				// Cargamos cada uno de los obj con los puntos
				JSONObject jO = features.getJSONObject(i);
				JSONObject properties = jO.getJSONObject("properties");
				JSONObject geometry = jO.getJSONObject("geometry");
				JSONObject geo_prop = geometry.getJSONObject("properties");
				JSONArray geo_coord = geometry.getJSONArray("coordinates");

				//Paseo walk = new Paseo(jObject.getInt("id"),jObject.getString("name"),jObject.getString("description"));
				// Me esto inventado el provider...
				SoundPoint p = new SoundPoint(LocationManager.GPS_PROVIDER);
				p.setLatitude(geo_coord.getDouble(1)); // TODO: No estoy seguro si el primer valor es LAT o LON...
				p.setLongitude(geo_coord.getDouble(0));
				p.setRadius((float) geometry.getDouble("radius"));
				p.setSoundFile(properties.getString("file"));
				p.setType(properties.getInt("type")); // Dentro de properties
				p.setEssid(properties.getString("essid")); // TODO: Será obligatorio tener ESSID?? aunque sea ""
				p.setAutofade(true); // por defecto le dejo activado el autofade.. 
				// TODO: Hacer una variable global en sharedPreferences para guarda el lugar de descarga..
				p.setFolder("/mnt/sdcard/Areago/"+this.getId()); // En JSON no nos define el ID del paseo? si
				this.puntos.add(p);
			} 
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("AREAGO","Error al cargar los puntos del paseo: "+this.getTitle()+" @ "+str);
		}
	}
	
	public void create_points_OLD(String str) {
		// Crea los puntos en el ArrayList a partir de un JArray
		//
		JSONArray jArray;
		try {
			jArray = new JSONArray(str);
			
		
			for (int i = 0; i<jArray.length();i++) {
				// Cargamos cada uno de los obj con los puntos
				JSONObject jObject = jArray.getJSONObject(i);
				//Paseo walk = new Paseo(jObject.getInt("id"),jObject.getString("name"),jObject.getString("description"));
				// Me esto inventado el provider... que cosas..
				SoundPoint p = new SoundPoint(LocationManager.GPS_PROVIDER);
				p.setLatitude(jObject.getDouble("lat"));
				p.setLongitude(jObject.getDouble("lon"));
				p.setRadius((float) jObject.getDouble("radio"));
				p.setSoundFile(jObject.getString("file"));
				p.setId(jObject.getInt("id"));
				p.setType(jObject.getInt("type"));
				p.setAutofade(jObject.getBoolean("autofade"));
				// TODO: Hacer una variable global en sharedPreferences para guarda el lugar de descarga..
				p.setFolder("/mnt/sdcard/Areago/"+this.getId());
				this.puntos.add(p);
			} 
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("AREAGO","Error al cargar los puntos del paseo: "+this.getTitle()+" @ "+str);
		}
	}
	
	public void addRefPoint(double lat, double lon) {
		this.pref.setLatitude(lat);
		this.pref.setLongitude(lon);
	}
	
	public String check_collisions(Location l) {
		// Recorre los puntos del mapa y revisa si estamos dentro del radio de uno de ellos
		String p = "";
		for (int i = 0; i<this.puntos.size(); i++){
			this.puntos.get(i).checkColision(l);
			p = p + " | "+this.puntos.get(i).getFolder()+"/"+this.puntos.get(i).getSoundFile();
		}
		return p;
	}
	
	public void check_collisions(ScanResult wifi) {
		for (int i=0; i<this.puntos.size(); i++) {
			this.puntos.get(i).checkColision(wifi);
			Log.d("AREAGO","ID: "+this.puntos.get(i).getId()+"tipo:"+this.puntos.get(i).getType());
		}
	}
	
	//public boolean exist(ArrayList<Paseo> walks) {
	public boolean exist(HashMap walks) {
		if (walks.get(this.id) != null) { // ya está mapeado un paseo con ese ID
			Paseo p = (Paseo) walks.get(this.id);
				/*if (this.hash != -1) { // si tiene hash... 
					if (p.hash != this.hash) { // se debe actualizar
						p.update = false; // marcamos como no actualizado
						walks.put(this.id, p); // actualizamos el paseo en el hash
					}
					
				}*/
			return true;
		} else { return false; }
	}
	
}
