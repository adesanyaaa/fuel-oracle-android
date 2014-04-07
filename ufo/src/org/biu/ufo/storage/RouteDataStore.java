package org.biu.ufo.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.Trace;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.analyzers.TestMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteSummaryMessage;
import org.biu.ufo.model.DriveHistory;
import org.biu.ufo.model.DriveRoute;
import org.biu.ufo.model.Location;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;
import com.openxc.sources.DataSourceException;
import com.openxc.util.AndroidFileOpener;
import com.squareup.otto.Subscribe;


@EBean
public class RouteDataStore {
	private static final String DIRECTORY_NAME = "UfoRoutes";

	private Location startLocation;
	private Location endLocation;
	private FileRecorder recorder;
	private String fileName;
	
	//TODO 
	private boolean firstTime = true;
	
	// Database fields
	private SQLiteDatabase database = null;
	

	@Bean
	OttoBus bus;
	
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
		bus.register(this);
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		bus.unregister(this);
		dbHelper.close();
	}
	
	public boolean initRecord(Location startLocation){
		// close old file
		if(this.recorder != null) {
			this.recorder.stop();			
		}
		
		// open new file
		this.recorder = new FileRecorder(DIRECTORY_NAME,new AndroidFileOpener(DIRECTORY_NAME));
		try {
			this.fileName = this.recorder.openNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			this.recorder = null;
			this.fileName = null;
			return false;
		}
		this.startLocation = startLocation;
		return addLocation(startLocation,false);
	}
	
	public String formatLocation(Location location) {
		return String.valueOf(location.getLatitude()) + "," +String.valueOf(location.getLongitude());
	}
	
	public boolean addLocation(Location location,boolean isEndLocation){
		if(recorder != null)
			return recorder.writeRecord("location", formatLocation(location),isEndLocation);
		return false;
	}
	
	public boolean closeRecord(Location endlocation){
		if(this.recorder != null) {
			this.endLocation = endlocation;
			addLocation(this.endLocation,true);		
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
				//null,
				RouteDBHelper.COLUMN_ID + " = " + id,
				null);
	}
	
	
	/*@Subscribe 
	public void onTest(TestMessage message){
		if (firstTime){
			deleteRoute(0);
			firstTime = false;
			insertRoute(10);
			insertRoute(20);
			insertRoute(30);
			insertRoute(40);
			insertRoute(50);
		}
		
		getRoutesHistory(3);
		//convertDataToRoute("2014-03-30-23-05-07.json");
		//getRoutesData(0);
	}
	*/
	// TODO remove this function
	/*private void insertRoute(int sum){
		initRecord(new Location(new LatLng(sum,sum--)));
		addLocation(new Location(new LatLng(sum,sum--)),false);
		addLocation(new Location(new LatLng(sum,sum--)),false);
		addLocation(new Location(new LatLng(sum,sum--)),false);
		addLocation(new Location(new LatLng(sum,sum--)),false);
		addLocation(new Location(new LatLng(sum,sum--)),false);
		addLocation(new Location(new LatLng(sum,sum--)),false);
		closeRecord(new Location(new LatLng(sum,sum--)));
	}*/
	
	
	public DriveHistory getRoutesHistory(int qnty){
		DriveHistory driveHistory = new DriveHistory();
		String filename = "";
		
		Cursor c =database.query(dbHelper.mRouteDBHelper.TABLE_NAME, null, null, null,
		        null, null, null);

		//if we want all the history ( insert -1)
		/*if (qnty==-1){
			qnty = 5;
		}*/
		if (c != null ) {
		    if  (c.moveToFirst()) {
		        do {
		        	filename = c.getString(c.getColumnIndex(RouteDBHelper.COLUMN_FILE)) ;
		        	driveHistory.addRoute(convertDataToRoute(filename));
		        	--qnty;
		        }while (qnty>0 && c.moveToNext());
		    }
		    c.close();
		}
		return driveHistory;
	}

	
	//Get into database and return driveRoute.
	private DriveRoute convertDataToRoute(String filename){
		DriveRoute driveRoute = new DriveRoute();
		ArrayList<Location> trace = null ;
		
		// close old recorder
		if(this.recorder != null) {
			this.recorder.stop();			
		}
				
		
		
		// open file
		this.recorder = new FileRecorder(DIRECTORY_NAME,new AndroidFileOpener(DIRECTORY_NAME));
		try {
			trace = recorder.readTraceLocations(filename);
			driveRoute.getRoute().addAll(trace);
			driveRoute.setEndTime();
			driveRoute.setStartTime();
			
		} catch (DataSourceException e) {
			e.printStackTrace();
		}
		return driveRoute;
	}
	
	
/*

	public void start() {
		bus.register(this);
	}
	
	
	public void stop() {
		bus.unregister(this);
	}*/
	
}
