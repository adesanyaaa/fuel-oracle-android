package org.biu.ufo.control.events.raw;

import org.biu.ufo.model.Location;

public class PickLocationMessage {
	private Location refLocation;
	
	public PickLocationMessage(Location refLocation) {
		this.refLocation = refLocation;
	}

	public Location getRefLocation() {
		return refLocation;
	}

}
