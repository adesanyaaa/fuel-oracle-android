package org.biu.ufo.events.control;

import org.biu.ufo.model.Place;

public class EstimatedDestinationMessage {
	private Place place;
	private boolean isDestLocationEstimated;
		
	public EstimatedDestinationMessage(Place place) {
		this(place, true);
	}
	
	public EstimatedDestinationMessage(Place place, boolean isDestLocationEstimated) {
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
