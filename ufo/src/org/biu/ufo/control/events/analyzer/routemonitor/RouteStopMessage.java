package org.biu.ufo.control.events.analyzer.routemonitor;

import org.biu.ufo.control.events.StatusMessage;
import org.biu.ufo.model.Location;


public class RouteStopMessage extends StatusMessage{
	
	public Location location;
	
	public RouteStopMessage(Location location){
		this.time = System.currentTimeMillis();
		this.message = "END OF ROUTE";
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
}
