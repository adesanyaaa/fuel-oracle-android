package org.biu.ufo.control.events.analyzer.routemonitor;

import org.biu.ufo.model.Place;

public class EstimatedDestination {
	private Place place;
	private boolean isDestLocationEstimated;
		
	public EstimatedDestination(Place place) {
		this(place, true);
	}
	
	public EstimatedDestination(Place place, boolean isDestLocationEstimated) {
		this.isDestLocationEstimated = isDestLocationEstimated;
		this.place = place;
	}
	
	public boolean isDestLocationEstimated() {
		return isDestLocationEstimated;
	}

	public Place getPlace() {
		return place;
	}

}
