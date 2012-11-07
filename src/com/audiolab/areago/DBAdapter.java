package com.audiolab.areago;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBAdapter {
	static final String KEY_ROWID= "_id";
	static final String KEY_NAME="titulo";
	static final String KEY_DESC="descripcion";
	static final int KEY_HASH=0;
	
	static final String DATABASE_NAME = "Areago";
	static final String DATABASE_TABLE = "paseos";
	static final int DATABASE_VERSION = 1;
	
	static final String DATABASE_CREATE="create table paseos (_id integer primary key autoincrement, "+"titulo text not null, descripcion text not null);";
	
	final Context context;
	
	DatabaseHelper DBHelper;
	SQLiteDatabase db;
	
	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}
	
	}
}
