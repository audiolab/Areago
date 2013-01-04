package com.audiolab.areago;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AreagoPreferences  extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.areagopreferences);
		setTheme(R.style.android_Theme_WhiteTheme);
		setTitle("Preferencias Servidor Areago");
	}
	public void onStop() {
		super.onStop();
	}
}
