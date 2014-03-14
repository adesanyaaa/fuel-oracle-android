package org.biu.ufo.control.events.analyzer.routemonitor;

import org.biu.ufo.model.Place;

public class EstimatedDestination {
	private Place place;
	
	public EstimatedDestination(Place place) {
		this.place = place;
	}
	
	public Place getPlace() {
		return place;
	}

}
