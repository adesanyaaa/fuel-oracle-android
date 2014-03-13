package org.biu.ufo.control.status_analyzer.commands;

import org.biu.ufo.control.rest.internal.ufoserver.Location;


public class EndOfRouteStatusMessage extends StatusMessage{
	
	public Location location;
	public EndOfRouteStatusMessage(Location location){
		this.time = System.currentTimeMillis();
		this.message = "END OF ROUTE";
		this.location = location;
	}
}
