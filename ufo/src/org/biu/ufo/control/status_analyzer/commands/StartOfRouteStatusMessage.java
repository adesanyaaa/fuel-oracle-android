package org.biu.ufo.control.status_analyzer.commands;

import org.biu.ufo.control.rest.internal.ufoserver.Location;


public class StartOfRouteStatusMessage extends StatusMessage{
	
	public Location location;
	public StartOfRouteStatusMessage(Location location){
		this.time = System.currentTimeMillis();
		this.message = "START OF ROUTE";
		this.location = location;
	}
}
