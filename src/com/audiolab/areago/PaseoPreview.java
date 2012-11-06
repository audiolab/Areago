package com.audiolab.areago;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class PaseoPreview extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.paseo_preview); 
		String t = getIntent().getStringExtra("titulo");
		setTitle("AREAGO : "+t);
		
		((TextView)findViewById(R.id.titulo)).setText(t);
		((TextView)findViewById(R.id.descripcion)).setText(getIntent().getStringExtra("descripcion"));
		
	}
	
	 public void onClick(View view) {
		 
	 }
	
}
