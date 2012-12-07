package com.audiolab.areago;

import java.util.List;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;



public class WiFiScanReceiver extends BroadcastReceiver {
	  private static final String TAG = "AREAGO";
	  PaseoPreview p;

	  public WiFiScanReceiver(PaseoPreview a) {
	    super();
	    this.p = a;
	  }

	  @Override
	  public void onReceive(Context c, Intent intent) {
		  List<ScanResult> results = p.wifi.getScanResults();
		  for (ScanResult result : results) {
				Log.d(TAG,"ESSID: "+result.SSID.toString()+" | LEVEL: "+result.level);
				p.walk.check_collisions(result);
		  }
	  }

	}
