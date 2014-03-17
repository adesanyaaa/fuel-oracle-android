package org.biu.ufo.storage;

import org.androidannotations.annotations.EBean;

import android.database.sqlite.SQLiteDatabase;

@EBean
public class RouteDBHelper extends ComponentDBHelper {

	public static final String COLUMN_START_LATITUDE = "start_latitude";
	public static final String COLUMN_START_LONGITUDE = "start_longitude";
	public static final String COLUMN_END_LATITUDE = "end_latitude";
	public static final String COLUMN_END_LONGITUDE = "end_longitude";
	public static final String COLUMN_FILE = "file";
	
	public RouteDBHelper() {
		super("route_history");
	}


	@Override
	public void createTable(SQLiteDatabase database) {
		String CREATE_HISTORY = "create table if not exists "
				+ TABLE_NAME + "(" 
				+ COLUMN_ID + " INTEGER primary key autoincrement, " 
				+ COLUMN_START_LATITUDE + " REAL not null,"
				+ COLUMN_START_LONGITUDE + " REAL not null,"
				+ COLUMN_END_LATITUDE + " REAL not null,"
				+ COLUMN_END_LONGITUDE + " REAL not null,"
				+ COLUMN_FILE + " TEXT not null unique on conflict ignore"
				+ ");";

		database.execSQL(CREATE_HISTORY);		
	}

}
