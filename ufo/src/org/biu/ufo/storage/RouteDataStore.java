package org.biu.ufo.storage;

import java.io.IOException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.model.Location;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.openxc.util.AndroidFileOpener;


@EBean
public class RouteDataStore {
	private static final String DIRECTORY_NAME = "UfoRoutes";

	private Location startLocation;
	private Location endLocation;
	private FileRecorder recorder;
	private String fileName;
	
	// Database fields
	private SQLiteDatabase database = null;
	
	@Bean
	DBHelper dbHelper;
	
	@RootContext
	Context context;
	
	public static final String[] allColumns = { 
		RouteDBHelper.COLUMN_ID,
		RouteDBHelper.COLUMN_START_LATITUDE,
		RouteDBHelper.COLUMN_START_LONGITUDE,
		RouteDBHelper.COLUMN_END_LATITUDE,
		RouteDBHelper.COLUMN_END_LONGITUDE,
		RouteDBHelper.COLUMN_FILE,
	};

	
	public void open() throws SQLException{
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public boolean initRecord(Location startLocation){
		// close old file
		if(this.recorder != null) {
			this.recorder.stop();			
		}
		
		// open new file
		this.recorder = new FileRecorder(new AndroidFileOpener(DIRECTORY_NAME));
		try {
			this.fileName = this.recorder.openNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			this.recorder = null;
			this.fileName = null;
			return false;
		}
		this.startLocation = startLocation;
		return addLocation(startLocation);
	}
	
	public String formatLocation(Location location) {
		return String.valueOf(location.getLongitude()) + "," +String.valueOf(location.getLatitude());
	}
	
	public boolean addLocation(Location location){
		if(recorder != null)
			return recorder.writeRecord(formatLocation(startLocation));
		return false;
	}
	
	public boolean closeRecord(Location endlocation){
		if(this.recorder != null) {
			this.endLocation = endlocation;
			addLocation(this.endLocation);		
			storeRoute();
		
			this.recorder.stop();	
			this.recorder = null;
			this.fileName = null;

			return true;
		}	
		return false;
	}
	
	private long storeRoute() {
		ContentValues values = new ContentValues();
		values.put(RouteDBHelper.COLUMN_END_LATITUDE, endLocation.getLatitude());
		values.put(RouteDBHelper.COLUMN_END_LONGITUDE, endLocation.getLongitude());
		values.put(RouteDBHelper.COLUMN_START_LATITUDE, startLocation.getLatitude());
		values.put(RouteDBHelper.COLUMN_START_LONGITUDE, startLocation.getLongitude());
		values.put(RouteDBHelper.COLUMN_FILE, fileName);

		return database.insert(dbHelper.mRouteDBHelper.TABLE_NAME, null, values);
	}
	
	public void deleteRoute(long id) {
		database.delete(dbHelper.mRouteDBHelper.TABLE_NAME,
				RouteDBHelper.COLUMN_ID + " = " + id,
				null);
	}
	
}
