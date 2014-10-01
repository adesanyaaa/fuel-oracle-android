package org.biu.ufo.control.monitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.components.PlaceResolver;
import org.biu.ufo.control.components.PlaceResolver.OnPlaceResolved;
import org.biu.ufo.control.components.StationsFetcher;
import org.biu.ufo.control.components.StationsFetcher.StationsFetcherResultHandler;
import org.biu.ufo.control.utils.AverageValue;
import org.biu.ufo.control.utils.Calculator;
import org.biu.ufo.events.car.raw.EngineSpeedMessage;
import org.biu.ufo.events.car.raw.FuelLevelMessage;
import org.biu.ufo.events.car.raw.LocationMessage;
import org.biu.ufo.events.car.raw.VehicleSpeedMessage;
import org.biu.ufo.events.control.EstimatedDestinationMessage;
import org.biu.ufo.events.control.FuelProcessMessage;
import org.biu.ufo.events.control.TripCompleted;
import org.biu.ufo.events.control.TripStart;
import org.biu.ufo.events.control.TripStop;
import org.biu.ufo.events.user.DestinationSelectedMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;
import org.biu.ufo.rest.Station;
import org.biu.ufo.storage.RouteDataStore;
import org.biu.ufo.storage.RouteDataStore.Record;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * TripMonitor
 *  
 * - Records route 
 * - informs on estimated destination change
 * - informs on destination reach (with statistics)
 */
@EBean
public class TripMonitor {
	public static final float CLOSE_ENOUGH_DISTANCE = 0.2f;
	
	static class WayPoint {
		long time = System.currentTimeMillis();
		Location location;
		Place place;
	}
		
	@Bean
	OttoBus bus;
	
	@Bean
	PlaceResolver placeResolver;
	
	@Bean 
	StationsFetcher stationsFetcher;
	
	@Bean
	RouteDataStore routeDataStore;
	
	WayPoint destPoint;		// Final destination point : as selected by user or estimated
	boolean isDestLocationEstimated;	// False if destination selected by user

	Location prevLocation;
	Location currentLocation;
	Record record;
	AverageValue avgSpeed;
	AverageValue avgRPM;
	double fuelLevel;
	
	private int latestToken;
	
	public void start() {
		initializeRoute();
		bus.register(this);
	}

	public void stop() {
		bus.unregister(this);
	}

	/**
	 * New trip
	 */
	private void initializeRoute() {
		// Clear destination
		destPoint = null;
		isDestLocationEstimated = true;
		avgSpeed = null;
		avgRPM = null;
	}
	
	@Subscribe
	public void onFuelLevel(FuelLevelMessage msg) {
		double newFuelLevel =  msg.getFuelLevelValue();
		if(fuelLevel != newFuelLevel) {
			fuelLevel = newFuelLevel;
			if(record != null) {
				record.addFuelLevel(fuelLevel);				
			}
		}
	}

	/**
	 * User started driving
	 * @param message
	 */
	@Subscribe
	public void onRouteStarted(TripStart message) {
		latestToken = new Random().nextInt();
		final int token = latestToken;
		
		// TODO: what if currentLocation is null
		placeResolver.resolvePlace(currentLocation, new OnPlaceResolved() {
			
			@Override
			public void onResult(Location location, Place place) {
				if(token == latestToken) {
					record = routeDataStore.initRecord(location, place.toString());
					if(fuelLevel > 0) {
						record.addFuelLevel(fuelLevel);						
					}
				}
			}
			
			@Override
			public void onFailure(Location location) {
				if(token == latestToken) {
					record = routeDataStore.initRecord(location, "");
					if(fuelLevel > 0) {
						record.addFuelLevel(fuelLevel);						
					}

				}
			}
		});
	}
	
	/**
	 * User stopped driving
	 * @param message
	 */
	@Subscribe
	public void onRouteEnded(TripStop message) {
		final Record recordToClose = this.record;
		placeResolver.resolvePlace(currentLocation, new OnPlaceResolved() {
			
			@Override
			public void onResult(Location location, Place place) {
				recordToClose.addLocation(location, place.toString(), true);

				recordToClose.close();
				// TODO: notify
				
				if(record == recordToClose) {
					record = null;
				}
			}
			
			@Override
			public void onFailure(Location location) {
				if(recordToClose != null) {
					recordToClose.close();
				}
				if(record == recordToClose) {
					record = null;
				}
			}
		});
	}
	
	/**
	 * User manually selected destination
	 * @param message
	 */
	@Subscribe
	public void onDestinationSelected(DestinationSelectedMessage message) {		
		double latitude = message.getPlace().getAddress().getLatitude();
		double longitude = message.getPlace().getAddress().getLongitude();

		destPoint = new WayPoint();
		destPoint.location = new Location(latitude, longitude);
		destPoint.place = message.getPlace();
		isDestLocationEstimated = false;

		bus.post(new EstimatedDestinationMessage(destPoint.place, isDestLocationEstimated));		
	}
	
	/**
	 * Current location changed
	 * @param message
	 */
	@Subscribe
	public void onLocationUpdate(LocationMessage message) {
		currentLocation = message.getLocation();
		if(prevLocation == null) {
			prevLocation = currentLocation;
		}
		if(record != null && record.isInTrip()) {
			if(Calculator.distance(prevLocation, currentLocation) > CLOSE_ENOUGH_DISTANCE) {
				record.addLocation(currentLocation, "", false);
				prevLocation = currentLocation;
			}
			
			if(destPoint != null && Calculator.distance(destPoint.location, currentLocation) < CLOSE_ENOUGH_DISTANCE) {
				bus.post(new TripCompleted(avgSpeed, avgRPM));
				initializeRoute();
			}
		}
	}

	@Subscribe
	public void onVehicleSpeedUpdate(VehicleSpeedMessage message){
		if(avgSpeed == null) {
			avgSpeed = new AverageValue();
		}
		avgSpeed.add(message.getSpeed());
	}

	@Subscribe
	public void onEngineSpeedUpdate(EngineSpeedMessage message){
		if(avgRPM == null) {
			avgRPM = new AverageValue();
		}
		avgRPM.add(message.getSpeed());
	}

	@Subscribe
	public void onFuelProcessMessage(final FuelProcessMessage message) {
		List<LatLng> posList = new ArrayList<LatLng>();
		posList.add(message.getLocation().getLatLng());
		stationsFetcher.requestStations(posList, new StationsFetcherResultHandler() {
			@Override
			public void onStationsResult(List<Station> stations) {
				Collections.sort(stations, new Comparator<Station>() {
					@Override
					public int compare(Station lhs, Station rhs) {
						return Double.compare(lhs.getDistance(currentLocation), rhs.getDistance(currentLocation));
					}
				});
				
				if(stations.size() > 0) {
					if(record != null) {
						record.addFuelingRecord(message, stations.get(0));
					} else {
						Record recordToWriteTo =  routeDataStore.initRecord(currentLocation, null);
						recordToWriteTo.addFuelingRecord(message, stations.get(0));
						recordToWriteTo.close();
					}
				}
			}
		});		
	}
	
	@Produce
	public EstimatedDestinationMessage produceEstimatedDestination() {
		if(destPoint != null && destPoint.place != null) {
			return new EstimatedDestinationMessage(destPoint.place, isDestLocationEstimated);			
		}
		return null;
	}	
	
	public Location getDestinationLocation() {
		if(destPoint != null)
			return destPoint.location;
		return null;
	}

	
}