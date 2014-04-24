package org.biu.ufo.control.analyzers;

import java.util.List;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.Controller;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedDestinationMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedRouteMessage;
import org.biu.ufo.control.events.raw.FuelLevelMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Client;
import org.biu.ufo.rest.Station;
import org.biu.ufo.rest.UFOClient;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * FuelRecommendator
 * Recommendation generator
 * 
 * TODO: only while driving?
 * 
 * @author Roee Shlomo
 *
 */
@EBean
public class FuelRecommendator implements IAnalyzer {
	public static final String TAG = "FuelRecommendator";
	public static final boolean ONLY_NEARBY = false;	// Set to false on final release
	
	public static final long MAX_STATIONS_REQUESTS = 50;//10;
	public static final float MIN_DISTANCE_BETWEEN_STATIONS_REQUEST_POINTS = 1.5f;	// 1.5 KM
	public static final long MIN_DURATION_BETWEEN_RECOMMENDATIONS = 5*60*1000;	// 5 Minutes
	public static final long MIN_DISTANCE_BETWEEN_RECOMMENDATIONS = 1;	// 1 KM
	
	
	@RootContext
	Context context;
	
	@Bean
	OttoBus bus;
	
	@Bean(UFOClient.class)
	Client stationsClient;

	Controller controller;
	
	Handler handler = new Handler();
	private volatile long currentRequestId;
	private volatile int pendingRequests;
	
	private Location currentLocation;
	private Double currentFuelLevel;	
	private FuelRecommendationMessage lastRecommendation;	
	
	private void recommendIfNeeded() {
		if(isLowFuelLevel()) {
			if(isEnoughTimePassed() && isEnoughDistanceTraveled()) {
				recommendNow();
			}
		} else {
			controller.getRouteEstimator().setRouteEstimationNeeded(false);
			if(lastRecommendation != null) {
				lastRecommendation = null;
				// Empty recommendation means "all good"
				bus.post(new FuelRecommendationMessage());
			}
		}
	}
	
	@Subscribe 
	public void onEstimatedDestinationMessage(EstimatedDestinationMessage message) {
		if(lastRecommendation != null) {
			lastRecommendation = null;
			// Empty recommendation means "all good"
			bus.post(new FuelRecommendationMessage());
		}		
		recommendIfNeeded();
	}
	
	public void recommendNow() {
		if(controller.getRouteEstimator().getDestinationLocation() == null)
			return;
		
		lastRecommendation = new FuelRecommendationMessage();
		lastRecommendation.setFuelLevel(currentFuelLevel.doubleValue());
		lastRecommendation.setLocation(new Location(currentLocation));
		
		if(ONLY_NEARBY || controller.getRouteEstimator().getDestinationLocation() == null) {
			generateStationsRequestId();
			requestNearbyStations(currentLocation.getLatitude(), currentLocation.getLongitude());			
		} else {
			controller.getRouteEstimator().requestRouteEstimation();
		}
	}
	
	private void generateStationsRequestId() {
		currentRequestId = (currentRequestId + 1) % 500;
		pendingRequests = 0;
	}
	
	protected void requestNearbyStations(double lat, double lng) {
		Log.d(TAG, "requestNearbyStations");
		++pendingRequests;
		fetchStations(currentRequestId, lat, lng);
	}

	@Subscribe
	public void onEstimatedRouteMessage(EstimatedRouteMessage message) {
		if(message.getEstimatedRoute().size() == 0) {
			Log.e(TAG, "Empty route!!! Using current location only!");
			generateStationsRequestId();
			requestNearbyStations(currentLocation.getLatitude(), currentLocation.getLongitude());			
			return;
		}
		
		generateStationsRequestId();
		
		Log.d(TAG, "onEstimatedRouteMessage got " + message.getEstimatedRoute().size() + " points");

		int numberOfRequests = 0;
		LatLng lastPoint = new LatLng(0, 0);
		int testCounted = 0;
		for(LatLng point : message.getEstimatedRoute()) {
			++testCounted;
			if(Calculator.distance(lastPoint, point) > MIN_DISTANCE_BETWEEN_STATIONS_REQUEST_POINTS) {
				requestNearbyStations(point.latitude, point.longitude);
				++numberOfRequests;
				lastPoint = point;
			}
			
			if(numberOfRequests >= MAX_STATIONS_REQUESTS) {
				break;
			}
		}
		
		Log.d(TAG, "onEstimatedRouteMessage queried " + numberOfRequests + " points. Last at pos=" + testCounted);

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
		--pendingRequests;

		if(lastRecommendation == null)
			return;

		if(stations != null) {
			lastRecommendation.addStations(stations);			
		}
		

		if(pendingRequests == 0) {
			// Fix distance
			for(Station station : lastRecommendation.getStations()) {
//				station.setDistanceUnit(DistanceUnit.KM);
				Location stationLocation = new Location(station.getLat(), station.getLng());
//				station.setDistance(Calculator.distance(currentLocation, stationLocation));
				station.setDistanceFromRoute(controller.getRouteEstimator().getMinDistanceFromRoute(stationLocation));
			}
			bus.post(lastRecommendation);
			// TODO: this is a popup test! Should make sure MainActivity is not visible!!!
//			PopupActivity_.intent(context.getApplicationContext()).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();	
		}
	}
	
	private boolean isLowFuelLevel() {
		currentFuelLevel = 7.1;
		return currentFuelLevel != null && currentFuelLevel < 30;
	}
	
	private boolean isEnoughDistanceTraveled() {
		// Must know location!
		if(currentLocation == null)
			return false;
		
		if(lastRecommendation == null)
			return true;
		
		double distance = Calculator.distance(currentLocation, lastRecommendation.getLocationAtRecommendTime());
		return distance > MIN_DISTANCE_BETWEEN_RECOMMENDATIONS;
	}

	private boolean isEnoughTimePassed() {
		if(lastRecommendation == null)
			return true;
		
		return System.currentTimeMillis() - lastRecommendation.getTime() > MIN_DURATION_BETWEEN_RECOMMENDATIONS;
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
	
	@Produce
	public FuelRecommendationMessage produceFuelNextRecommendation() {
		if(lastRecommendation != null) {
			return lastRecommendation;
		}
		return new FuelRecommendationMessage();
	}
	
	@Override
	public void start() {
		bus.register(this);
	}

	@Override
	public void stop() {
		bus.unregister(this);		
	}

	@Override
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
}
