package org.biu.ufo.control.monitors;

import java.util.LinkedList;
import java.util.List;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.components.RouteEstimator;
import org.biu.ufo.control.components.RouteEstimator.EstimatedRoute;
import org.biu.ufo.control.components.RouteEstimator.EstimatedRouteResult;
import org.biu.ufo.control.components.StationsFetcher;
import org.biu.ufo.control.components.StationsFetcher.StationsFetcherResultHandler;
import org.biu.ufo.control.utils.Calculator;
import org.biu.ufo.events.car.raw.FuelLevelMessage;
import org.biu.ufo.events.car.raw.LocationMessage;
import org.biu.ufo.events.control.FuelRecommendationMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

@EBean
public class Recommendator implements EstimatedRouteResult, StationsFetcherResultHandler {
	public static final long MAX_STATIONS_REQUESTS = 50;//10;
	public static final float MIN_DISTANCE_BETWEEN_STATIONS_REQUEST_POINTS = 1.5f;	// 1.5 KM
	public static final long MIN_DURATION_BETWEEN_RECOMMENDATIONS = 1*60*1000;	// 1 Minute
	public static final float MIN_DISTANCE_BETWEEN_RECOMMENDATIONS = 0.2f;	// 1 KM
	
	@Bean
	OttoBus bus;
			
	@Bean
	StationsFetcher stationsFetcher;
	
	@Bean
	RouteEstimator routeEstimator;

	private Location currentLocation;
	private Double currentFuelLevel;	
	private FuelRecommendationMessage lastRecommendation;	
	private TripMonitor routeMonitor;
	private EstimatedRoute estimatedRoute;

	public void start(TripMonitor routeMonitor) {
		this.routeMonitor = routeMonitor;
		bus.register(this);
	}

	public void stop() {
		bus.unregister(this);		
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
		return distance > MIN_DISTANCE_BETWEEN_RECOMMENDATIONS;
	}

	private boolean isEnoughTimePassed() {
		if(lastRecommendation == null)
			return true;
		
		return System.currentTimeMillis() - lastRecommendation.getTime() > MIN_DURATION_BETWEEN_RECOMMENDATIONS;
	}

	private void recommendIfNeeded() {
		//if(routeMonitor.getDestinationLocation() == null)
		//	return;
		
		if(isLowFuelLevel()) {
			if(isEnoughTimePassed() && isEnoughDistanceTraveled()) {
				Log.e("TEST", "isEnoughTimePassed() && isEnoughDistanceTraveled");
				recommendNow();
			}
		} else {
			if(lastRecommendation != null) {
				lastRecommendation = null;
				// Empty recommendation means "all good"
				bus.post(new FuelRecommendationMessage());
			}
		}
	}
	
	public void recommendNow() {
		lastRecommendation = new FuelRecommendationMessage();
		lastRecommendation.setFuelLevel(currentFuelLevel.doubleValue());
		lastRecommendation.setLocation(new Location(currentLocation));
		
		/*if(routeMonitor.getDestinationLocation() == null) {
			List<LatLng> positions = new LinkedList<LatLng>();
			positions.add(currentLocation.getLatLng());
			stationsFetcher.requestStations(positions, this);
		} else */if(estimatedRoute == null || !routeEstimator.isOnRoute(estimatedRoute, currentLocation)) {
			Log.e("TEST", "getNewRouteEstimation");
			routeEstimator.getNewRouteEstimation(currentLocation, routeMonitor.getDestinationLocation(), this);			
		} else {
			onEstimatedRouteResult(estimatedRoute);
		}
	}

	@Override
	public void onEstimatedRouteResult(EstimatedRoute route) {
		Log.e("TEST", "onEstimatedRouteResult");
		estimatedRoute = route;
	
		List<LatLng> positions = new LinkedList<LatLng>();
		
		int numberOfRequests = 0;
		LatLng lastPoint = new LatLng(0, 0);
		long maxRequests = MAX_STATIONS_REQUESTS;
		for(LatLng point : route.points) {
			if(Calculator.distance(lastPoint, point) > MIN_DISTANCE_BETWEEN_STATIONS_REQUEST_POINTS) {
				positions.add(point);
				++numberOfRequests;
				lastPoint = point;
			}
			
			if(numberOfRequests >= maxRequests) {
				break;
			}
		}
		Log.e("TEST", "requestStations");
		stationsFetcher.requestStations(positions, this);
	}

	@Override
	public void onStationsResult(List<Station> stations) {
		Log.e("TEST", "onStationsResult");
		lastRecommendation.addStations(stations);

		// Fix distance
		for(Station station : lastRecommendation.getStations()) {
			Location stationLocation = new Location(station.getLat(), station.getLng());
			station.setDistanceFromRoute(routeEstimator.getMinDistanceFromRoute(estimatedRoute, stationLocation));
		}
		lastRecommendation.sortStations();

		// TODO: check duration using Route
		bus.post(lastRecommendation);
	}
	
	@Produce
	public FuelRecommendationMessage produceFuelNextRecommendation() {
		if(lastRecommendation != null) {
			return lastRecommendation;
		}
		return new FuelRecommendationMessage();
	}

}
