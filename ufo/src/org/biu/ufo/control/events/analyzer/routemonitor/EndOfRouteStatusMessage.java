package org.biu.ufo.control.events.analyzer.routemonitor;

import org.biu.ufo.control.events.StatusMessage;
import org.biu.ufo.model.Location;


public class EndOfRouteStatusMessage extends StatusMessage{
	
	public Location location;
	public EndOfRouteStatusMessage(Location location){
		this.time = System.currentTimeMillis();
		this.message = "END OF ROUTE";
		this.location = location;
	}
}
