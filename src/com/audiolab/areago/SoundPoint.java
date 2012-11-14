package com.audiolab.areago;

import java.io.IOException;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

public class SoundPoint extends Location {
	
	private float radius;
	private String folder = "/mnt/sdcard/areago/test";
	private String soundFile = "test.wav";
	private int id;
	private Context mContext;
	private int type;
	
	// TIPO DE PUNTOS TRADICIONALES
	public static final int TYPE_PLAY_ONCE=0; // reproduce una vez el audio mientras esté en el radio
	public static final int TYPE_PLAY_LOOP=1; // reproduce en loop mientras esté en el radio
	// TIPOS DE PUNTOS DE ACCIÓN
	public static final int TYPE_TOGGLE=2; // Play/Stop segun el estado anterior del audio a que hace referencia 
	public static final int TYPE_PLAY_START=3; // Ejecuta un audio
	public static final int TYPE_PLAY_STOP=0; // Para un audio
	
	// ESTADOS DE REPRODUCCIÓN
	private static final int STATUS_PLAYING=0;
	private static final int STATUS_STOPPED=2;
	private static final int STATUS_PAUSED=3;
	
	// INFO DE MEDIA PLAYER
	private MediaPlayer mp;
	private int status=STATUS_STOPPED;
	private boolean played=false;	 // Cuando ya lo hemos reproducido y no queremos volver a reproducirlo 
	
	private boolean salido = false;
	private boolean completado = false;
	
	//private SoundPoint instance;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public void setType(int type) {
		this.type = type;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getSoundFile() {
		return soundFile;
	}

	public void setSoundFile(String soundFile) {
		this.soundFile = soundFile;
	}

	public SoundPoint(Location l) {
		super(l);
		// TODO Auto-generated constructor stub
	}

	public SoundPoint(String provider) {
		super(provider);
		// TODO Auto-generated constructor stub
	}
	
	public SoundPoint(Context mContext){
		this(LocationManager.GPS_PROVIDER);
		this.mContext=mContext;	
		
	}
	
	// Deberíamos hacer aquí el trabajo con los diversos TYPES?
	public boolean checkColision(Location l){
		float distance=this.distanceTo(l);
		Log.d("AREAGO","Lat: "+l.getLatitude()+" - Lon: "+l.getLongitude()+ "- distance: "+distance);
		if (distance<=this.radius){
			Log.d("AREAGO","Colisiona el punto"+this.id+" Estado reproducc: "+this.status);
			switch (this.status) {
				case STATUS_STOPPED :
					switch (this.type){
						case TYPE_PLAY_ONCE:
							if (!this.played) this.mediaPlay();
							break;
						case TYPE_PLAY_LOOP:
							this.mediaPlay();
							break;
						// EL RESTO DE TYPE NO AFECTA ??
					}
					break;
				case STATUS_PLAYING :
					// Seguimos reproduciendo
					break;
				case STATUS_PAUSED :
					// Volvemos a ejecutar el audio.
					// REVISAR LA RAZON DE ESTE CASO
					//this.mediaPlay();
					break;
			}
			// HABRÍA QUE MARCAR QUE ESTÁ DENTRO DEL CIRCULO?
			//salido = false; // no ha salido del círculo
			return true;
		} else {
			//Log.d("AREAGO","NO Colisiona el punto"+this.id);
			switch (status) {
				case STATUS_STOPPED :
					// SIGUE PARADO
					break;
				case STATUS_PLAYING :
					// LO PARAMOS O LO DEJAMOS PAUSADO?
					this.mediaStop(); 
					break;
				case STATUS_PAUSED :
					// LO SEGUIMOS DEJANDO PARADO?
					break;
			}
			// HABRÍA QUE MARCAR QUE ESTA FUERA DEL CIRCULO?
			return false;
		}
	}
	
	public void stopSoundFile() {
		if (this.status == STATUS_PLAYING || this.status == STATUS_PAUSED ) mediaStop();
	}
	
	private void mediaStop(){
		this.mp.stop();
		this.mp.release();
		status=STATUS_STOPPED;
	}
	
	private void mediaPlay(){
		this.mp = new MediaPlayer();
		Log.d("AREAGO","Playing: "+this.folder + "/" + this.soundFile);
		
	    try {
			this.mp.setDataSource(this.folder + "/" + this.soundFile);
			this.mp.prepare();
			this.mp.setLooping(false);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		this.mp.setVolume(1.0f, 1.0f);
		this.mp.start();
		
		status=STATUS_PLAYING;
		played=true;
		
		this.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer arg0) {
				// TODO CUANDO ACABA LO MARCAMOS ??
				// GESTIONAMOS EL STOP DEL AUDIO EN LOOP A PARTIR DE COLISION, AL ACABAR SI ES LOOP LO VOVLEMOS A REPRODUCIR, SI NO, LO MARCAMOS COMO STOP.
				switch (type) {
				case TYPE_PLAY_ONCE :
					Log.d("AREAGO","Se finalizó la reproducción de: " + arg0.getAudioSessionId());
					arg0.stop();
					arg0.release();
					status = STATUS_STOPPED;
					break;
				case TYPE_PLAY_LOOP:
					// TODO: DEBEMOS VOLVER A EJECUTAR O LO MARCAMOS ANTES COMO LOOP PARA QUE NO PARE?
					Log.d("AREAGO","Se finalizó la reproducción de UN AUDIO LOOP: " + arg0.getAudioSessionId());
					arg0.start();
					status = STATUS_PLAYING;
				}						
			}
		});
		//Vibrator vb = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		//vb.vibrate(2000);		
	}
	
	
	public void prepareSoundFile(){
		this.mp = new MediaPlayer();
		
	    try {
			this.mp.setDataSource(this.folder + "/" + this.soundFile);
			//mp.prepare();
			this.mp.prepareAsync();
			this.mp.setLooping(false);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	}
	
	public void pausePlaying(){
		if (status==STATUS_PLAYING){
			this.mp.pause();
			status=STATUS_PAUSED;
		}
	}

}