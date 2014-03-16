package org.biu.ufo.control.events.user;

import org.biu.ufo.model.Place;

public class DestinationSelectedMessage {
	private Place place;
	
	public DestinationSelectedMessage(Place place) {
		this.place = place;
	}
	
	public Place getPlace() {
		return place;
	}

}
