package org.biu.ufo.control.analyzers;

import java.io.IOException;
import java.util.List;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedDestination;
import org.biu.ufo.control.events.analyzer.routemonitor.StartOfRouteStatusMessage;
import org.biu.ufo.control.events.route.DestinationSelected;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

@EBean
public class RouteEstimator {
	@Bean
	OttoBus bus;
	
	@RootContext
	Context context;
	
	Location sourceLocation;
	Place sourcePlace;
	Location destLocation;
	Place destPlace;
	
	interface OnPlaceResolved {
		void onResult(Location location, Place place);
	}

	@Subscribe
	public void onRouteStarted(StartOfRouteStatusMessage message) {
		sourceLocation = message.getLocation();
		resolvePlace(sourceLocation, new OnPlaceResolved() {
			@Override
			public void onResult(Location location, Place place) {
				sourcePlace = place;
			}			
		});
	}
	
	@Subscribe
	public void onDestinationSelected(DestinationSelected message) {
		double latitude = message.getPlace().getAddress().getLatitude();
		double longitude = message.getPlace().getAddress().getLongitude();
		destLocation = new Location(latitude, longitude);
		destPlace = message.getPlace();
		bus.post(new EstimatedDestination(destPlace));
	}

	@Background
	void resolvePlace(Location location, OnPlaceResolved onPlaceResolved) {
		Geocoder geocoder = new Geocoder(context);
		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if(!addresses.isEmpty()) {
				delieverResolvePlaceResult(location, new Place(addresses.get(0)), onPlaceResolved);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@UiThread
	void delieverResolvePlaceResult(Location location, Place place,
			OnPlaceResolved onPlaceResolved) {
		onPlaceResolved.onResult(location, place);
	}
	
	@Produce
	public EstimatedDestination produceEstimatedDestination() {
		return new EstimatedDestination(destPlace);
	}

}