package org.biu.ufo.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.biu.ufo.events.control.FuelProcessMessage;
import org.biu.ufo.model.DriveHistory;
import org.biu.ufo.model.DrivePoint;
import org.biu.ufo.model.DriveRoute;
import org.biu.ufo.model.FuelLevelData;
import org.biu.ufo.model.FuelingData;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.openxc.sources.DataSourceException;
import com.openxc.util.AndroidFileOpener;


@EBean(scope = Scope.Singleton)
public class RouteDataStore {
	private static final String DIRECTORY_NAME = "UfoRoutes";

	public class Record {
		private FileRecorder recorder = new FileRecorder(DIRECTORY_NAME, new AndroidFileOpener(DIRECTORY_NAME));
		private Location startLocation;
		private Location endLocation;
		private String fileName;
		
		public Record() throws IOException {
			fileName = recorder.openNewFile();
		}
		
		public boolean addLocation(Location location, String label, boolean isEndLocation) {
			if(startLocation == null) {
				startLocation = location;
			}
			if(isEndLocation) {
				endLocation = location;
			}
			return recorder.writeRecord("location", formatLocation(location), label, isEndLocation);
		}
		
		public boolean addFuelingRecord(FuelProcessMessage fuelProcessMessage, Station station) {
			return recorder.writeRecord("fueling", formatFuelingData(fuelProcessMessage, station), "", false);
		}

		public boolean addFuelLevel(double fuelLevel) {
			return recorder.writeRecord("fuellevel", String.valueOf(fuelLevel), "", false);
		}
		
		public boolean isInTrip() {
			return startLocation != null && endLocation == null;
		}
		
		public void close() {
			if(endLocation == null) {
				endLocation = startLocation;
			}
			storeRoute(this);
			this.recorder.stop();	
		}

	}
	

	private int counter = 0;
	private SQLiteDatabase database = null;
	
	@Bean
	DBHelper dbHelper;
	
	public static final String[] allColumns = { 
		RouteDBHelper.COLUMN_ID,
		RouteDBHelper.COLUMN_START_LATITUDE,
		RouteDBHelper.COLUMN_START_LONGITUDE,
		RouteDBHelper.COLUMN_END_LATITUDE,
		RouteDBHelper.COLUMN_END_LONGITUDE,
		RouteDBHelper.COLUMN_FILE,
	};

	
	public void open() throws SQLException {
//		if (++counter == 1){
			database = dbHelper.getWritableDatabase();
//		}
	}
	

	public void close() {
//		if (--counter == 0){
//			dbHelper.close();
//			database = null;
//		}
	}
	
	private Record initRecord() {
		Record record = null;
		try {
			record = new Record();
		} catch(IOException e) {
			
		}
		return record;
	}
	

	public Record initRecord(Location startLocation, String label) {
		Record record = initRecord();
		if(record != null) {
			record.addLocation(startLocation, label, false);
		}
		return record;
	}
	
	private String formatLocation(Location location) {
		return String.valueOf(location.getLatitude()) + "," +String.valueOf(location.getLongitude());
	}
	
	public Object formatFuelingData(FuelProcessMessage fuelProcessMessage, Station station) {
		return String.valueOf(fuelProcessMessage.getStartFuelLevel()) + "," + String.valueOf(fuelProcessMessage.getEndFuelLevel()) + ","
				+ station.getAddress() + "," + station.getCompany() + "," + String.valueOf(station.getPrice());
	}

	private long storeRoute(Record record) {
		ContentValues values = new ContentValues();
		values.put(RouteDBHelper.COLUMN_END_LATITUDE, record.endLocation.getLatitude());
		values.put(RouteDBHelper.COLUMN_END_LONGITUDE, record.endLocation.getLongitude());
		values.put(RouteDBHelper.COLUMN_START_LATITUDE, record.startLocation.getLatitude());
		values.put(RouteDBHelper.COLUMN_START_LONGITUDE, record.startLocation.getLongitude());
		values.put(RouteDBHelper.COLUMN_FILE, record.fileName);

		return database.insert(dbHelper.mRouteDBHelper.TABLE_NAME, null, values);
	}
	
	public void deleteRoute(long id) {
		database.delete(dbHelper.mRouteDBHelper.TABLE_NAME,
				//null,
				RouteDBHelper.COLUMN_ID + " = " + id,
				null);
	}
	
	public DriveHistory getRoutesHistory(int qnty){
		DriveHistory driveHistory = new DriveHistory();
		String filename = "";
		
		Cursor c =database.query(dbHelper.mRouteDBHelper.TABLE_NAME, null, null, null,
		        null, null, null);

		//if we want all the history ( insert -1)
		if (qnty==-1){
			qnty = c.getColumnCount();
		}
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
		// open file
		FileRecorder recorder = new FileRecorder(DIRECTORY_NAME,new AndroidFileOpener(DIRECTORY_NAME));
		try {
			List<DrivePoint> drivePoints = new ArrayList<DrivePoint>();
			List<FuelingData> fuelingData = new ArrayList<FuelingData>();
			List<FuelLevelData> fuelLevelData = new ArrayList<FuelLevelData>();
			recorder.readTraceLocations(filename, drivePoints, fuelingData, fuelLevelData);
			driveRoute.getRoute().addAll(drivePoints);
			driveRoute.getFuelingData().addAll(fuelingData);
			if(fuelLevelData.size() > 0) {
				driveRoute.setStartFuelLevel(fuelLevelData.get(0).fuelLevel);
				driveRoute.setEndFuelLevel(fuelLevelData.get(fuelLevelData.size()-1).fuelLevel);
			}
			driveRoute.setEndTime();
			driveRoute.setStartTime();
		} catch (DataSourceException e) {
			e.printStackTrace();
		}
		return driveRoute;
	}
		
}
