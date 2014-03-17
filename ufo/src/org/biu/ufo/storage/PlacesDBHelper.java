package org.biu.ufo.storage;

import org.androidannotations.annotations.EBean;

import android.database.sqlite.SQLiteDatabase;

@EBean
public class PlacesDBHelper extends ComponentDBHelper {
	
	public static final String COLUMN_ADDRESS = "address";
	public static final String COLUMN_LATITUDE = "latitude";
	public static final String COLUMN_LONGITUDE = "longitude";
	public static final String COLUMN_LABEL = "label";
	
	public PlacesDBHelper() {
		super("places_history");
	}

	@Override
	public void createTable(SQLiteDatabase database) {
		
		String CREATE_HISTORY = "create table if not exists "
				+ TABLE_NAME + "(" 
				+ COLUMN_ID + " INTEGER primary key autoincrement, " 
				+ COLUMN_ADDRESS + " TEXT not null unique on conflict ignore,"
				+ COLUMN_LABEL + " TEXT not null,"
				+ COLUMN_LATITUDE + " REAL not null,"
				+ COLUMN_LONGITUDE + " REAL not null"
				+ ");";

		database.execSQL(CREATE_HISTORY);

	}

}