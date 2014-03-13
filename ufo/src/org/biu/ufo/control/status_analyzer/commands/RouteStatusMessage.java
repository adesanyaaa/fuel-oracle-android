package org.biu.ufo.control.status_analyzer.commands;

import java.util.ArrayList;

import org.biu.ufo.control.rest.internal.ufoserver.Location;

public class RouteStatusMessage extends StatusMessage{
	
	public ArrayList<Location> route;
	public Location startLocation;
	public Location endLocation;
	public long startTime;
	public long endTime;
	public long duration;
	
	public RouteStatusMessage(){
		this.route = new ArrayList<Location>();
		this.time = System.currentTimeMillis();
		this.message = "ROUTE Details";
	}
	
		
	public ArrayList<Location> getRoute(){
		return route;
	}


	public Location getStartLocation() {
		return startLocation;
	}


	public void setStartLocation(Location startLocation) {
		this.startLocation = startLocation;
	}


	public Location getEndLocation() {
		return endLocation;
	}


	public void setEndLocation(Location endLocation) {
		this.endLocation = endLocation;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public long getEndTime() {
		return endTime;
	}


	public void setEndTime(long endTime) {
		this.endTime = endTime;
		this.time = endTime;
		this.duration = this.endTime - this.endTime;
	}


	public long getDuration() {
		return duration;
	}



	public void setRoute(ArrayList<Location> route) {
		this.route = route;
	}
}
