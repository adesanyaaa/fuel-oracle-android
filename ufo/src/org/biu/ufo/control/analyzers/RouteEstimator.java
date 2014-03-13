package org.biu.ufo.control.analyzers;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.events.analyzer.routemonitor.StartOfRouteStatusMessage;
import org.biu.ufo.control.events.route.DestinationSelected;
import org.biu.ufo.model.Location;

import com.squareup.otto.Subscribe;

@EBean
public class RouteEstimator {
	@Bean
	OttoBus bus;
	
	Location sourceLocation;
	Location destinationLocation;
	Location estimatedDestinationLocation;
	
	@Subscribe
	public void onRouteStarted(StartOfRouteStatusMessage message) {
		sourceLocation = message.getLocation();
	}

	@Subscribe
	public void onDestinationSelected(DestinationSelected message) {
		double latitude = message.getPlace().getAddress().getLatitude();
		double longitude = message.getPlace().getAddress().getLongitude();
		destinationLocation = new Location(latitude, longitude);
	}
		
	Location getSourceLocation() {
		return sourceLocation;
	}
	
	Location getDestinationLocation() {
		if(destinationLocation == null)
			return estimatedDestinationLocation;
		return destinationLocation;
	}

}