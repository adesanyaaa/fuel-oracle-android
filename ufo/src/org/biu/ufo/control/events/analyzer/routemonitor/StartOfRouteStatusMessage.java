package org.biu.ufo.control.events.analyzer.routemonitor;

import org.biu.ufo.control.events.StatusMessage;
import org.biu.ufo.model.Location;



public class StartOfRouteStatusMessage extends StatusMessage {
	
	private Location location;
	
	public StartOfRouteStatusMessage(Location location){
		this.time = System.currentTimeMillis();
		this.message = "START OF ROUTE";
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
}
