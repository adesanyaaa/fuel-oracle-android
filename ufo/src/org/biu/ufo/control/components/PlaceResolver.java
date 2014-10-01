package org.biu.ufo.control.components;

import java.io.IOException;
import java.util.List;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

/**
 * Get Place from Location
 * TODO: use local cache
 * @author Roee Shlomo
 *
 */
@EBean
public class PlaceResolver {
	private static final String TAG = "PlaceResolver";
	
	private static final int MAX_TRIES = 3;

	@RootContext
	Context context;

	class Retry {
		int value = 0;
		
		void increase() {
			++value;
		}
	}
	
	public interface OnPlaceResolved {
		void onResult(Location location, Place place);
		void onFailure(Location location);
	}
	
	public void resolvePlace(Location location, OnPlaceResolved onPlaceResolved) {
		if(location == null) {
			onPlaceResolved.onFailure(location);
			return;
		}
		resolvePlace(location, onPlaceResolved, new Retry());
	}
	
	@Background
	public void resolvePlace(Location location, OnPlaceResolved onPlaceResolved, Retry retry) {
		
		Geocoder geocoder = new Geocoder(context);
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if(!addresses.isEmpty()) {
				delieverResolvePlaceResult(location, new Place(addresses.get(0)), onPlaceResolved);
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());			
			retryResolve(location, onPlaceResolved, retry);
		}
	}
	
	@UiThread(delay=30000)
	void retryResolve(Location location, OnPlaceResolved onPlaceResolved, Retry retry) {
		retry.increase();
		if(retry.value < MAX_TRIES) {
			resolvePlace(location, onPlaceResolved, retry);
		} else {
			onPlaceResolved.onFailure(location);
		}
	}

	@UiThread
	void delieverResolvePlaceResult(Location location, Place place,
			OnPlaceResolved onPlaceResolved) {
		onPlaceResolved.onResult(location, place);
	}

}
