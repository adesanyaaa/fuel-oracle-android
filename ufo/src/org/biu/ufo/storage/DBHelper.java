package org.biu.ufo.storage;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

@EBean
public class DBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "ufo_history.db";
	private static final int DATABASE_VERSION = 6;
	
	@Bean
	RouteDBHelper mRouteDBHelper;
	
	@Bean
	PlacesDBHelper mPlacesDBHelper;
	
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		this.mPlacesDBHelper.createTable(database);
		this.mRouteDBHelper.createTable(database);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    db.execSQL("DROP TABLE IF EXISTS " + this.mPlacesDBHelper.TABLE_NAME);
	    db.execSQL("DROP TABLE IF EXISTS " + this.mRouteDBHelper.TABLE_NAME);
	    onCreate(db);
	}

}