package com.audiolab.areago;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AreagoPreferences  extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.areagopreferences);
	}
}
