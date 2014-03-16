package org.biu.ufo.control.events.analyzer.routemonitor;

import java.util.List;

import org.biu.ufo.model.Place;

import com.google.android.gms.maps.model.LatLng;

public class EstimatedRouteMessage {
	Place destPlace;
	List<LatLng> estimatedRoute;
	
	public EstimatedRouteMessage(Place destPlace, List<LatLng> estimatedRoute) {
		this.destPlace = destPlace;
		this.estimatedRoute = estimatedRoute;
	}

	
}
