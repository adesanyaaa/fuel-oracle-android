package org.biu.ufo.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlacesDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "places.db";
	private static final int DATABASE_VERSION = 4;

	public static final String TABLE_HISTORY = "hostory";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_LABEL = "label";
	
	public PlacesDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {

		
		String CREATE_HISTORY = "create table "
				+ TABLE_HISTORY + "(" 
				+ COLUMN_ID + " INTEGER primary key autoincrement, " 
				+ COLUMN_ADDRESS + " TEXT not null,"
				+ COLUMN_LABEL + " TEXT not null,"
				+ COLUMN_LATITUDE + " REAL not null,"
				+ COLUMN_LONGITUDE + " REAL not null"
				+ ");";

		database.execSQL(CREATE_HISTORY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
	    onCreate(db);		
	}

}