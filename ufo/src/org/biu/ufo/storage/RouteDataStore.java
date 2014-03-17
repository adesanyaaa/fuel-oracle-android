package org.biu.ufo.storage;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.model.Location;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;


@EBean
public class RouteDataStore {
	
	private static final String FILE_NAME_FORMAT = "yyyy-MM-dd_hh-mm-ss";
	private static final String filepath = "RouteHistory";
	private File routeHistoryFile;
	private File directory = null;
	
	private Location startLocation;
	private Location endLocation;
	private String fileName;
	private boolean fileCreated;
	private BufferedWriter writer;
	
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
		//database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		//dbHelper.close();
	}
	
	public boolean initRecord(Location startLocation){
		if (database == null){
			open();
		}
		if (directory == null){
			ContextWrapper contextWrapper = new ContextWrapper(context);
			directory = contextWrapper.getDir(filepath, Context.MODE_PRIVATE);	
		}
		this.startLocation = startLocation;
		
		fileName = (String) DateFormat.format(FILE_NAME_FORMAT, new Date());
		routeHistoryFile = new File(directory, fileName);
		
		try {
			FileOutputStream fOut = new FileOutputStream(routeHistoryFile);
			BufferedOutputStream buf = new BufferedOutputStream(fOut);
            OutputStreamWriter osw = new OutputStreamWriter(buf);
            writer = new BufferedWriter(osw);
			fileCreated = true;
		} catch (FileNotFoundException e) {
			fileCreated = false;
			e.printStackTrace();
		}
		return fileCreated;
	}
	
	public boolean addLocation(Location location){
		if (fileCreated){
			String locationStatement = String.valueOf(location.getLongitude()) + "," +String.valueOf(location.getLatitude()+"\n");
			try {
				writer.write(locationStatement);
				writer.flush();
			} catch (IOException e) {
				return false;
			}
		}
		return fileCreated;
	}
	
	public boolean closeRecord(Location endlocation){
		if (fileCreated){
			try {
				this.endLocation = endlocation;
				writer.close();
				storeRoute();
			} catch (IOException e) {
				return false;
			}
		}
		return fileCreated;
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
