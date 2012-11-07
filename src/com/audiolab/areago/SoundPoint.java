package com.audiolab.areago;

import java.io.IOException;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

public class SoundPoint extends Location {
	
	private float radius;
	private String folder;
	private String soundFile;
	private int id;
	private Context mContext;
	
	private static final int STATUS_PLAYING=0;
	//private static final int STATUS_STOPPING=1;
	private static final int STATUS_STOPPED=2;
	private static final int STATUS_PAUSED=3;
	
	private MediaPlayer mp;
	private int status=STATUS_STOPPED;
	
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
	
	// Deberíamos hacer aquí el trabajo con los diversos tipos de TAGS?
	public boolean checkColision(Location l){
		float distance=this.distanceTo(l);
		if (distance<=this.radius){
			if (status==STATUS_STOPPED){
				this.mediaPlay();
			}
			salido = false; // no ha salido del círculo
			return true;
		} else {
			if(status==STATUS_PLAYING){
				salido = true;		
			}
			return false;
		}
	}
	
	private void mediaStop(){
		mp.stop();
		mp.release();
		status=STATUS_STOPPED;
	}
	
	private void mediaPlay(){
		this.mp = new MediaPlayer();
		status=STATUS_PLAYING;
	    try {
			this.mp.setDataSource(this.folder + "/" + this.soundFile);
			//mp.prepare();
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
		//Log.v("vale","fjdsfhdskjfhsdkjfhsdkjhfsd");
		
		
		mp.setVolume(1.0f, 1.0f);
		mp.start();
		
		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		
			public void onCompletion(MediaPlayer arg0) {
				// TODO Auto-generated method stub
				if (salido){
					status = STATUS_STOPPED;						
				}

			}
		});
		Vibrator vb = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		vb.vibrate(2000);		
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