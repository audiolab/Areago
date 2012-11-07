package com.audiolab.areago;

import java.util.ArrayList;

import android.location.Location;

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
	
	public Paseo(int id, String t, String d) {
		this.id = id;
		this.titulo = t;
		this.descripcion = d;
	}
	
	public void create_points(String jarray) {
		// Crea los puntos en el ArrayList a partir de un JArray
	}
	
	public void addRefPoint(double lat, double lon) {
		this.pref.setLatitude(lat);
		this.pref.setLongitude(lon);
	}
	
	public void check_collisions(Location l) {
		// Recorre los puntos del mapa y revisa si estamos en uno/o más de ellos
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
