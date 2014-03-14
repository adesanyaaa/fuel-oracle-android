package org.biu.ufo.control.analyzers;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.PlaceResolver;
import org.biu.ufo.control.PlaceResolver.OnPlaceResolved;
import org.biu.ufo.control.events.analyzer.routemonitor.EndOfRouteStatusMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedDestination;
import org.biu.ufo.control.events.analyzer.routemonitor.StartOfRouteStatusMessage;
import org.biu.ufo.control.events.route.DestinationSelected;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;

import android.content.Context;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * RouteEstimator
 * Higher level route analyzer (uses RouteAnalyzer)
 * 
 * TODO: estimate destination if not selected
 */
@EBean
public class RouteEstimator {
	private static final float CLOSE_ENOUGH_DISTANCE = 500;
	
	@RootContext
	Context context;

	@Bean
	OttoBus bus;
	
	@Bean
	PlaceResolver placeResolver;
	
	Location sourceLocation;
	Place sourcePlace;
	
	Location stopLocation;
	Place stopPlace;

	Location destLocation;
	Place destPlace;

	boolean isDestLocationEstimated = true;
	
	@Subscribe
	public void onRouteStarted(StartOfRouteStatusMessage message) {
		// Only resolve if needed (TODO: once resolve cache is implemented this won't be needed)
		boolean shouldResolve = true;
		if(sourceLocation != null && sourcePlace != null 
				&& Calculator.distance(sourceLocation, message.getLocation()) < CLOSE_ENOUGH_DISTANCE) {
			shouldResolve = false;
		}
		
		sourceLocation = message.getLocation();
		if(shouldResolve) {
			placeResolver.resolvePlace(sourceLocation, new OnPlaceResolved() {
				@Override
				public void onResult(Location location, Place place) {
					sourcePlace = place;
				}

				@Override
				public void onFailure(Location location) {
					// TODO Auto-generated method stub
				}
			});			
		}
	}
	
	@Subscribe
	public void onRouteEnded(EndOfRouteStatusMessage message) {
		stopLocation = message.getLocation();
		
		// Done!
		if(Calculator.distance(destLocation, stopLocation) < CLOSE_ENOUGH_DISTANCE) {
			stopPlace = destPlace;
			tripCompleted();
		}
		
		// Partial stop?
		else {
			placeResolver.resolvePlace(stopLocation, new OnPlaceResolved() {
				@Override
				public void onResult(Location location, Place place) {
					stopPlace = place;
				}
				
				@Override
				public void onFailure(Location location) {
					// TODO Auto-generated method stub
				}
			});
		}
	}

	private void tripCompleted() {
		// Change source
		sourceLocation = stopLocation;
		sourcePlace = stopPlace;
		
		// Clear stop and destination
		stopLocation = null;
		stopPlace = null;
		destLocation = null;
		destPlace = null;
		
		//
	}

	@Subscribe
	public void onDestinationSelected(DestinationSelected message) {
		double latitude = message.getPlace().getAddress().getLatitude();
		double longitude = message.getPlace().getAddress().getLongitude();
		destLocation = new Location(latitude, longitude);
		destPlace = message.getPlace();
		isDestLocationEstimated = false;
		bus.post(new EstimatedDestination(destPlace, isDestLocationEstimated));
	}
	
	@Produce
	public EstimatedDestination produceEstimatedDestination() {
		if(destPlace != null) {
			return new EstimatedDestination(destPlace, isDestLocationEstimated);			
		}
		return null;
	}

}