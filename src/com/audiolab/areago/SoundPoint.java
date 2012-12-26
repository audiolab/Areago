package com.audiolab.areago;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

public class SoundPoint extends Location {
	
	private float radius;
	private String folder = "/mnt/sdcard/areago/test";
	private String soundFile = "test.wav";
	private int id;
	private Context mContext;
	private int type;
	private String SSID;
	private int layer=0;
	private int destLayer=0;
	// Volumen 
	public float volume=1;
	public float vol;
	public float tVolume;
	public int increment=100;
	public int fadeTime=500; // medio segundo de fade
	
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
	private static final int STATUS_ACTIVATE=4;
	private static final int STATUS_DEACTIVATE=5;
	
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
		if (this.type == SoundPoint.TYPE_TOGGLE) this.status = SoundPoint.STATUS_DEACTIVATE; // lo iniciamos sin estar activado
		else if ( (this.type == SoundPoint.TYPE_PLAY_LOOP) || (this.type == SoundPoint.TYPE_PLAY_ONCE) || (this.type == SoundPoint.TYPE_PLAY_UNTIL) || (this.type == SoundPoint.TYPE_WIFI_PLAY_LOOP) ) { this.status = SoundPoint.STATUS_STOPPED; }
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
	
	public void setLayer(int layer) {
		this.layer = layer;
	}
	
	public void setChangeToLayer(int l) {
		this.destLayer = l;
	}
	
	public int getLayer() {
		return this.layer;
	}
	
	public int getLayerDestination() {
		return this.destLayer;
	}
	
	public boolean isExecuted() {
		if (this.status == SoundPoint.STATUS_DEACTIVATE) { return false; }
		else { return true; }
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
	// PRE: Los puntos de trigger asociacios a un radio geografico tb se gestionan por aquí
	// Devuelve la misma capa si no hay cambio o devuelve la capa de destino si hay un toogle de cambio de capa
	public int checkColision(Location l){
		float distance=this.distanceTo(l);
		Log.d("AREAGO","Colision["+this.getId()+"]: Lat: "+l.getLatitude()+" - Lon: "+l.getLongitude()+ "- distance: "+distance+"/ radius: "+this.radius);
		if (distance<=this.radius){
			Log.d("AREAGO","[****] Colisiona el punto"+this.id+" Estado reproducc: "+this.status);
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
							Log.d("AREAGO","Playing audio Loop");
							this.mediaPlay(l);
							break;
						// EL RESTO DE TYPE NO AFECTA ??
					}
					break;
				case SoundPoint.STATUS_PLAYING :
					// Seguimos reproduciendo
					// TODO: cambia el volumen si es necesario..
//						if(this.autofade) this.changeVolume(l);
						if (this.autofade) {
							float volume = (float) 1.0 - (float) this.distanceTo(l)/this.radius;
							if (volume > 1.0) volume = (float) 1.0;
							else if (volume < 0) volume = (float) 0.0;
					    	Log.d("AREAGO","[GPS]["+this.soundFile+"]Change Volume : "+volume);
					    	this.changeVolume(volume);
						} else {
							Log.d("AREAGO","[GPS] No es AutoFADE");
						}
					break;
				case SoundPoint.STATUS_PAUSED :
					// Volvemos a ejecutar el audio.
					// REVISAR LA RAZON DE ESTE CASO ... posibilidad de 
					//this.mediaPlay();
					break;
				case SoundPoint.STATUS_ACTIVATE :
					// La acción del trigger ya ha sido ejecutada
					// TODO: De momento se va a poder ejecutar tants veces como se entre.. se podría limitar..
					return this.layer;
				case SoundPoint.STATUS_DEACTIVATE:
					// La acción del trigger no ha sido ejecutada
					Log.d("AREAGO","GPS Cambio de capa"+this.destLayer);
					return this.destLayer;
					//TODO: Deberíamos marcarla para que no se volviera a ejecutar???
			}
			// HABRÍA QUE MARCAR QUE ESTÁ DENTRO DEL CIRCULO?
			//salido = false; // no ha salido del círculo
			//return this.layer;
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
			// TODO: HABRÍA QUE MARCAR QUE ESTA FUERA DEL CIRCULO?
			//return false;
		}
		return this.layer;
	}
	
	// PRE Solo puntos de sonidos en localizaciones WIFI
	public int checkColision(List<ScanResult> results) {
		// Revismaos puntos y si son de type WIFI comprobamos si alguno tiene el mismo ESSID
		// Deberíamos ver si el wifi de este punto está o no..
		for (ScanResult wifi : results) {
			// Si está aquí dentro debermos ejecutar el audio o cambiar el volumen
			//Log.d("AREAGO","Existe el punto: "+this.id+"con el ESSID: "+this.SSID+" - Con el Wifi: "+wifi.SSID);
			if (wifi.SSID.equals(this.SSID)) { // Estamos en el radio de acción del wifi
				//Log.d("AREAGO","Hay colision en: "+this.id+"con el ESSID: "+this.SSID);
				if (this.type == SoundPoint.TYPE_WIFI_PLAY_LOOP) {
					switch (this.status) {
						case SoundPoint.STATUS_STOPPED :
							//Log.d("AREAGO","Play");
							this.mediaPlay(wifi);
							break; 
						case SoundPoint.STATUS_PLAYING :
							float x = (float) (90+wifi.level);
							if (x<-1) x=0;
							if (x>90) x=90;
							float volume = x/90;
					    	if (volume > 1.0) volume = (float) 1;
					    	else if (volume < 0) volume = (float) 0;
					    	Log.d("AREAGO","Change Volume : "+volume);
							this.changeVolume(volume);
							break; 
					}
				} else if (this.type == SoundPoint.TYPE_TOGGLE) {
					if (this.status == SoundPoint.STATUS_DEACTIVATE) { 
						Log.d("AREAGO","wifi Cambio de capa"+this.destLayer);
						return this.destLayer; 
						}
				}
				return this.layer;
			} 
		}
		// No hemos encontrado el ESSID en la lista de los wifis disponibles
		// Por lo tanto paramos si está ejecutado lo paramos o no hacemos nada
		switch(this.status){
			case SoundPoint.STATUS_PLAYING:
				//Log.d("AREAGO","Stop Audio Wifi");
				this.mediaStop();
				break;
		}
		return this.layer;
	}
	
	public void stopSoundFile() {
		if (this.status == STATUS_PLAYING || this.status == STATUS_PAUSED ) this.mediaStop();
	}
	
	private void mediaStop(){
		this.mp.stop();
		this.mp.release();
		this.status=STATUS_STOPPED;
	}
	
	private void pauseSoundFile() {
		this.mp.pause();
		this.status=STATUS_PAUSED;
	}
	
	private void unpauseSoundFile() {
		this.mp.start();
		this.status=STATUS_PLAYING;
	}
	
	private void changeVolume(float dVolume) {
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
		//this.mp.setVolume(this.volume,this.volume);
		Log.d("AREAGO","[Volumen] Cambio de volumen de "+this.volume+" A "+dVolume);
		fadeVolume(fadeTime,dVolume);
		this.volume=dVolume;
		Log.d("AREAGO","[Audio] Cambiando volumen a :"+this.volume);
		
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
			volume = x/90;
	    	if (volume > 1.0) volume = (float) 1;
	    	if (volume < 0) volume = (float) 0;
	    	Log.d("AREAGO","Gestionamos el volumen a: " + volume);
	    	this.changeVolume(volume);
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
	    	float volume = (float) 1.0 - (float) this.distanceTo(l)/this.radius;
	    	if (volume > 1.0) volume = (float) 1.0;
	    	if (volume < 0) volume = (float) 0.0;
	    	this.changeVolume(volume);
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
				case TYPE_PLAY_UNTIL :
					Log.d("AREAGO","Se finalizó la reproducción de: " + arg0.getAudioSessionId());
					arg0.stop();
					arg0.release();
					status = STATUS_STOPPED;
					break;
				case TYPE_PLAY_LOOP:
					// TODO: DEBEMOS VOLVER A EJECUTAR O LO MARCAMOS ANTES COMO LOOP PARA QUE NO PARE?
					Log.d("AREAGO","Se finalizó la reproducción de UN AUDIO LOOP: " + arg0.getAudioSessionId());
					arg0.release();
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
	
	// De un valor V (el actual en this.volume) a un valor
	//TODO: testear distintos tiempos para ver la reacción del audio..
	//TODO: Gestionar la muerte del proceso en el stop del activity!!!
	public void fadeVolume(int duration,float dVolume)
	{
	    vol = dVolume; // volumen final
	    tVolume = this.volume; // volumen inicial
	    float rVolume = vol - tVolume;
	    float steps = (float) duration/increment; // numero de veces que hace el cambio en tantos milisegundos
	    final float vIncrement = rVolume/steps; //sera positivo si es FadeIN o negativo si FadeOut

	    new CountDownTimer(duration, increment)
	    {
	        public void onFinish() 
	        {
	        	try {
	            mp.setVolume(vol, vol);
	        	} catch (IllegalStateException e) {
	        		e.printStackTrace();
	        	}
	        }
	        public void onTick(long millisUntilFinished) 
	        {
	            tVolume += vIncrement;
	            try {
	            	mp.setVolume(tVolume, tVolume);
	            } catch (IllegalStateException e) {
	            	e.printStackTrace();
	            }
	        }
	    }.start();
	}

}