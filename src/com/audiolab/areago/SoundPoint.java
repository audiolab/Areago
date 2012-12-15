package com.audiolab.areago;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.util.Log;

public class SoundPoint extends Location {
	
	private float radius;
	private String folder = "/mnt/sdcard/areago/test";
	private String soundFile = "test.wav";
	private int id;
	private Context mContext;
	private int type;
	private String SSID;
	
	// TIPO DE PUNTOS TRADICIONALES
	public static final int TYPE_PLAY_ONCE=0; // reproduce una vez el audio mientras esté en el radio
	public static final int TYPE_PLAY_LOOP=1; // reproduce en loop mientras esté en el radio
	public static final int TYPE_PLAY_UNTIL=2;
	// TIPOS DE PUNTOS DE ACCIÓN
	public static final int TYPE_TOGGLE=6; // Play/Stop segun el estado anterior del audio a que hace referencia 
	public static final int TYPE_PLAY_START=3; // Ejecuta un audio
	public static final int TYPE_PLAY_STOP=4; // Para un audio
	// TIPOS DE PUNTOS DE WIFI
	public static final int TYPE_WIFI_PLAY_LOOP=5;
	
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
	public boolean autofade = false;
	//private SoundPoint instance;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void setEssid(String SSID) {
		this.SSID = SSID;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public void setAutofade(boolean af) {
		this.autofade = af;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
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
	
	// PRE: Solo puntos de locacalización de sonido
	public boolean checkColision(Location l){
		float distance=this.distanceTo(l);
		Log.d("AREAGO","Lat: "+l.getLatitude()+" - Lon: "+l.getLongitude()+ "- distance: "+distance);
		if (distance<=this.radius){
			Log.d("AREAGO","Colisiona el punto"+this.id+" Estado reproducc: "+this.status);
			switch (this.status) {
				case SoundPoint.STATUS_STOPPED :
					switch (this.type){
						case SoundPoint.TYPE_PLAY_ONCE:
							if (!this.played) this.mediaPlay(l);
							break;
						case SoundPoint.TYPE_PLAY_UNTIL:
							if (!this.played) this.mediaPlay(l);
							break;
						case SoundPoint.TYPE_PLAY_LOOP:
							this.mediaPlay(l);
							break;
						// EL RESTO DE TYPE NO AFECTA ??
					}
					break;
				case SoundPoint.STATUS_PLAYING :
					// Seguimos reproduciendo
					// TODO: cambia el volumen si es necesario..
//						if(this.autofade) this.changeVolume(l);
						float vol = (float) this.distanceTo(l)/this.radius;
				    	if (vol > 1.0) vol = (float) 1;
				    	if (vol < 0) vol = (float) 0;
				    	this.changeVolume(vol);
					break;
				case SoundPoint.STATUS_PAUSED :
					// Volvemos a ejecutar el audio.
					// REVISAR LA RAZON DE ESTE CASO ... posibilidad de 
					//this.mediaPlay();
					break;
			}
			// HABRÍA QUE MARCAR QUE ESTÁ DENTRO DEL CIRCULO?
			//salido = false; // no ha salido del círculo
			return true;
		} else {
			//Log.d("AREAGO","NO Colisiona el punto"+this.id);
			switch (status) {
				case SoundPoint.STATUS_STOPPED :
					// SIGUE PARADO
					break;
				case SoundPoint.STATUS_PLAYING :
					// LO PARAMOS O LO DEJAMOS PAUSADO? No.. 
					if (this.status!=SoundPoint.TYPE_PLAY_UNTIL)this.mediaStop(); // El play_until lo dejamos hasta que se finalice el audio 
					break;
				case SoundPoint.STATUS_PAUSED :
					// LO SEGUIMOS DEJANDO PAUSADO?
					break;
			}
			this.played = false; // Si estamos fuera del radio ya podemos volver a darle para que suene al volver a entrar
			// HABRÍA QUE MARCAR QUE ESTA FUERA DEL CIRCULO?
			return false;
		}
	}
	
	// PRE Solo puntos de sonidos en localizaciones WIFI
	public void checkColision(List<ScanResult> results) {
		// Revismaos puntos y si son de type WIFI comprobamos si alguno tiene el mismo ESSID
		// Deberíamos ver si el wifi de este punto está o no..
		for (ScanResult wifi : results) {
			// Si está aquí dentro debermos ejecutar el audio o cambiar el volumen
			Log.d("AREAGO","Existe el punto: "+this.id+"con el ESSID: "+this.SSID+" - Con el Wifi: "+wifi.SSID);
			if (wifi.SSID.equals(this.SSID)) { // Estamos en el radio de acción del wifi
				Log.d("AREAGO","Hay colision en: "+this.id+"con el ESSID: "+this.SSID);
				switch (this.status) {
					case SoundPoint.STATUS_STOPPED :
						Log.d("AREAGO","Play");
						this.mediaPlay(wifi);
						return; // Salimos de la funcinón..
					case SoundPoint.STATUS_PLAYING :
//						float vol = (float) ((-1)*wifi.level)/100;
						float x = (float) (90+wifi.level);
						if (x<-1) x=0;
						if (x>90) x=90;
						float vol = x/90;
				    	if (vol > 1.0) vol = (float) 1;
				    	if (vol < 0) vol = (float) 0;
				    	Log.d("AREAGO","Change Volume : "+vol);
						this.changeVolume(vol);
						return; //Salimos de la función
				}
			} 
		}
		// No hemos encontrado el ESSID en la lista de los wifis disponibles
		// Por lo tanto paramos si está ejecutado o no hacemos nada
		switch(this.status){
			case SoundPoint.STATUS_PLAYING:
				Log.d("AREAGO","Stop Audio Wifi");
				this.mediaStop();
				break;
		}
	}
	
	public void stopSoundFile() {
		if (this.status == STATUS_PLAYING || this.status == STATUS_PAUSED ) this.mediaStop();
	}
	
	private void mediaStop(){
		this.mp.stop();
		this.mp.release();
		status=STATUS_STOPPED;
	}
	
	private void pauseSoundFile() {
		this.mp.pause();
		status=STATUS_PAUSED;
	}
	
	private void unpauseSoundFile() {
		this.mp.start();
		status=STATUS_PLAYING;
	}
	
	private void changeVolume(float v) {
		// escala de log10 de 1-10 -> volumen = 1-log([1-10])
		// PRE: distancia <= radio
		// volume = 1 -log(distancia*10/radio)
		// Problem: distancia*10/radio != 0 porque es un error en logaritmo!!
		// El resultado queda demasiado duro.. hay que buscar una curba más suave   	
		/*double d = this.distanceTo(l)*10.0/this.radius;
		if (d==0.0) d=1.0;
		float v = (float) (1.0 - Math.log(d));*/
		// Lo dejo en lineal
//		float v = (float) this.distanceTo(l)/this.radius;
		this.mp.setVolume(v,v);
		
	}
	
	private void mediaPlay(ScanResult wifi) {
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
	    
	    if (!this.autofade) {
	    	this.mp.setVolume(1.0f, 1.0f);
	    } else {	
	    	// autoFADE
	    	// basandome en esto: http://stackoverflow.com/questions/8704186/android-scanresult-level-value
	    	// level = -10dBM es el bueno
	    	// level = -100dBM malo
//	    	float vol = (float) ((-1)*wifi.level)/100;
	    	float x = (float) (90+wifi.level);
			if (x<-1) x=0;
			if (x>90) x=90;
			float vol = x/90;
	    	if (vol > 1.0) vol = (float) 1;
	    	if (vol < 0) vol = (float) 0;
	    	Log.d("AREAGO","Gestionamos el volumen a: " + vol);
	    	this.changeVolume(vol);
	    }
	    
	    this.mp.start();
		
		this.status=SoundPoint.STATUS_PLAYING;
		this.played=true;
		
		this.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer arg0) {
				switch (type) {
				case  SoundPoint.TYPE_WIFI_PLAY_LOOP:
					Log.d("AREAGO","Se finalizó la reproducción de UN AUDIO LOOP: " + arg0.getAudioSessionId());
					arg0.start();
					status = STATUS_PLAYING;
					break;
				}						
			}
		});
	}
	
	private void mediaPlay(Location l){
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
		
	    // Gestión de volumen!
	    // autoFADE disabled
	    if (!this.autofade) {
	    	this.mp.setVolume(1.0f, 1.0f);
	    } else {	
		// autoFADE
	    	float vol = (float) this.distanceTo(l)/this.radius;
	    	if (vol > 1.0) vol = (float) 1;
	    	if (vol < 0) vol = (float) 0;
	    	this.changeVolume(vol);
	    }	
	    	
		this.mp.start();
		
		this.status=STATUS_PLAYING;
		this.played=true;
		
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