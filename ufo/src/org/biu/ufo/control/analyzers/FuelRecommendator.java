package org.biu.ufo.control.analyzers;

import java.util.List;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.raw.FuelLevelMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Client;
import org.biu.ufo.rest.MGFClient;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.activities.PopupActivity_;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * FuelRecommendator
 * Recommendation generator
 * 
 * TODO: only while driving?
 * TODO: use route estimator and query interest points on the way
 * 
 * @author Roee Shlomo
 *
 */
@EBean
public class FuelRecommendator implements IAnalyzer {
	public static final String TAG = "FuelRecommendator";
	public static final long MIN_DURATION_BETWEEN_RUNS = 5*60*1000;
	public static final long MIN_DISTANCE_BETWEEN_RUNS = 1000;
	
	@RootContext
	Context context;
	
	@Bean
	OttoBus bus;
	
	@Bean(MGFClient.class)
	Client stationsClient;

	Handler handler = new Handler();
	private volatile long currentRequestId;
	
	private Location currentLocation;
	private Double currentFuelLevel;	
	private FuelRecommendationMessage lastRecommendation;	
	
	private void recommendIfNeeded() {
		if(isLowFuelLevel()) {
			if(isEnoughTimePassed() && isEnoughDistanceTraveled()) {
				recommendNow();
			}
		} else {
			lastRecommendation = null;
		}
	}
	
	public void recommendNow() {
		lastRecommendation = new FuelRecommendationMessage();
		lastRecommendation.setFuelLevel(currentFuelLevel.doubleValue());
		lastRecommendation.setLocation(new Location(currentLocation));
		requestNearbyStations(currentLocation.getLatitude(), currentLocation.getLongitude());
	}
	
	protected void requestNearbyStations(double lat, double lng) {
		Log.d(TAG, "requestNearbyStations");
		currentRequestId = (currentRequestId + 1) % 500;
		fetchStations(currentRequestId, lat, lng);
	}

	@Background
	protected void fetchStations(final long requestId, double lat, double lng) {
		Log.d(TAG, "fetchStations");

		final float distance = 1; // in KM
		final List<Station> stations = stationsClient.getStations(String.valueOf(lat), String.valueOf(lng), distance);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(requestId == currentRequestId) {
					delieverStationsList(stations);
				}
			}
		});
	}

	void delieverStationsList(final List<Station> stations) {
		Log.d(TAG, "delieverStationsList");		
//		Collections.sort(stations, new Comparator<Station>() {
//
//			@Override
//			public int compare(Station a, Station b) {
//				return 0;
//			}
//			
//		});
		lastRecommendation.setStations(stations);
		bus.post(lastRecommendation);
		
		// TODO: this is a popup test! Should make sure MainActivity is not visible!!!
		PopupActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();	
	}
	
	private boolean isLowFuelLevel() {
		return currentFuelLevel != null && currentFuelLevel < 30;
	}
	
	private boolean isEnoughDistanceTraveled() {
		// Must know location!
		if(currentLocation == null)
			return false;
		
		if(lastRecommendation == null)
			return true;
		
		double distance = Calculator.distance(currentLocation, lastRecommendation.getLocationAtRecommendTime());
		return distance > MIN_DISTANCE_BETWEEN_RUNS;
	}

	private boolean isEnoughTimePassed() {
		if(lastRecommendation == null)
			return true;
		
		return System.currentTimeMillis() - lastRecommendation.getTime() > MIN_DURATION_BETWEEN_RUNS;
	}

	@Produce
	public FuelRecommendationMessage produceFuelNextRecommendation() {
		if(lastRecommendation != null && lastRecommendation.getStations() != null) {
			return lastRecommendation;
		}
		return null;
	}
	
	@Subscribe
	public void onLocationUpdate(LocationMessage message){
		currentLocation = message.getLocation();
		recommendIfNeeded();
	}
	
	@Subscribe
	public void onFuelLevel(FuelLevelMessage message){
		currentFuelLevel = message.getFuelLevelValue();
	}

	@Override
	public void start() {
		bus.register(this);
	}

	@Override
	public void stop() {
		bus.unregister(this);		
	}
	
}
