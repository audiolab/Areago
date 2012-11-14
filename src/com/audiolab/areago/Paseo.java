package com.audiolab.areago;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.content.Context;

public class Paseo {
	
	int id ;
	String titulo;
	String idioma;
	String descripcion;
	int num_grabaciones;
	int tamano;
	SoundPoint pref = new SoundPoint("paseo_reference"); // Punto de referencia de inicio / lat-lon
	int hash;
	boolean update;
	String JsonPoints; // Listado de puntos del mapa en Json Array
	ArrayList<SoundPoint> puntos = new ArrayList<SoundPoint>();
	
	public Paseo(int id) {
		this.id = id;
	}
	
	public void setTitle(String t) {
		this.titulo = t;
	}
	
	public void setDescription(String d) {
		this.descripcion = d;
	}
	
	public String getTitle() {
		return this.titulo; 
	}
	
	public void stop() {
		for (int i = 0; i<this.puntos.size(); i++) {
			this.puntos.get(i).stopSoundFile();
		}
	}
	
	public void create_points(String str) {
		// Crea los puntos en el ArrayList a partir de un JArray
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
				p.setType(jObject.getInt("type")); // TODO: Leerlo del JSON
				this.puntos.add(jObject.getInt("id"), p);
			} 
		
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d("AREAGO","Error al cargar los puntos del paseo: "+this.getTitle());
		}
	}
	
	public void addRefPoint(double lat, double lon) {
		this.pref.setLatitude(lat);
		this.pref.setLongitude(lon);
	}
	
	public void check_collisions(Location l) {
		// Recorre los puntos del mapa y revisa si estamos dentro del radio de uno de ellos
		for (int i = 0; i<this.puntos.size(); i++){
			this.puntos.get(i).checkColision(l);
		}
	}
	
	public boolean exist(ArrayList<Paseo> walks) {
		// TODO Auto-generated method stub
		
		for (int i=0; i<walks.size(); i++) {
			if (walks.get(i).id == this.id) {
				if (walks.get(i).hash != this.hash) {
					// se debe actualizar
					walks.get(i).update=true;
					return true;
				} else { return true; }
			}
		}
		
		return false;
	}
	
}
