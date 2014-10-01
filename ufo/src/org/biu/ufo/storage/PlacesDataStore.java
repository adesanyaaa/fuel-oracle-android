package org.biu.ufo.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.model.Place;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.text.TextUtils;

@EBean
public class PlacesDataStore {
	
	@RootContext
	Context context;

	@Bean
	DBHelper dbHelper;

	// Database fields
	private SQLiteDatabase database;

	public static final String[] allColumns = { 
			PlacesDBHelper.COLUMN_ID,
			PlacesDBHelper.COLUMN_ADDRESS,
			PlacesDBHelper.COLUMN_LABEL,
			PlacesDBHelper.COLUMN_LATITUDE,
			PlacesDBHelper.COLUMN_LONGITUDE,
	};

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void deletePlace(long id) {
		database.delete(dbHelper.mPlacesDBHelper.TABLE_NAME,
				PlacesDBHelper.COLUMN_ID + " = " + id,
				null);
	}

	public long storePlace(Place place) {
		ContentValues values = new ContentValues();
		String label = place.getLabel();
		if(label == null)
			label = "";
		values.put(PlacesDBHelper.COLUMN_LABEL, label);
		values.put(PlacesDBHelper.COLUMN_LATITUDE, place.getAddress().getLatitude());
		values.put(PlacesDBHelper.COLUMN_LONGITUDE, place.getAddress().getLongitude());
		values.put(PlacesDBHelper.COLUMN_ADDRESS, place.toString());
		return database.insert(dbHelper.mPlacesDBHelper.TABLE_NAME, null, values);
	}
	
	public Cursor getAllPlacesCursor() {
		return database.query(dbHelper.mPlacesDBHelper.TABLE_NAME,
				allColumns, null, null, null, null, null);
	}

	public List<Place> getAllPlaces() {
		List<Place> places = new ArrayList<Place>();

		Cursor cursor = getAllPlacesCursor();
		
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Place place = cursorToPlace(cursor);
			places.add(place);
			cursor.moveToNext();
		}

		// make sure to close the cursor
		cursor.close();
		return places;
	}

	public static Place cursorToPlace(Cursor cursor) {		
		String addressStr = cursor.getString(cursor.getColumnIndex(PlacesDBHelper.COLUMN_ADDRESS));
		double latitude = cursor.getDouble(cursor.getColumnIndex(PlacesDBHelper.COLUMN_LATITUDE));
		double longitude = cursor.getDouble(cursor.getColumnIndex(PlacesDBHelper.COLUMN_LONGITUDE));

		Address address = new Address(Locale.getDefault());
		address.setAddressLine(0, addressStr);
		address.setLatitude(latitude);
		address.setLongitude(longitude);

		String label = cursor.getString(cursor.getColumnIndex(PlacesDBHelper.COLUMN_LABEL));

		boolean isFavorite = false;
		if(!TextUtils.isEmpty(label)) {
			isFavorite = true;
		}

		return new Place(address, label, isFavorite);
	}

}