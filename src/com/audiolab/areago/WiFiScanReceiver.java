package com.audiolab.areago;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class WiFiScanReceiver extends BroadcastReceiver {
	  private static final String TAG = "WiFiScanReceiver";
	  PaseoPreview wifiDemo;

	  public WiFiScanReceiver(PaseoPreview wifiDemo) {
	    super();
	    this.wifiDemo = wifiDemo;
	  }

	  @Override
	  public void onReceive(Context c, Intent intent) {
		// List available networks
		  List<ScanResult> results = wifiDemo.wifi.getScanResults();
			String con_list = "";
			for (ScanResult result : results) {
				con_list=result.toString();
			}
		//((TextView)findViewById(R.id.wifi_con)).setText(con_list);
		
		Toast.makeText(wifiDemo, con_list, Toast.LENGTH_LONG).show();
		  
	    /*List<ScanResult> results = wifiDemo.wifi.getScanResults();
	    ScanResult bestSignal = null;
	    for (ScanResult result : results) {
	      if (bestSignal == null
	          || WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0)
	        bestSignal = result;
	    }

	    String message = String.format("%s networks found. %s is the strongest.",
	        results.size(), bestSignal.SSID);
	    Toast.makeText(wifiDemo, message, Toast.LENGTH_LONG).show();*/

	    //Log.d(TAG, "onReceive() message: " + message);
	  }

	}
