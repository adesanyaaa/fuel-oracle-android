package org.biu.ufo.control.events.route;

import org.biu.ufo.model.Place;

public class DestinationSelected {
	private Place place;
	
	public DestinationSelected(Place place) {
		this.place = place;
	}
	
	public Place getPlace() {
		return place;
	}

}
