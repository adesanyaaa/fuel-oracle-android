package org.biu.ufo.model;

import java.util.ArrayList;

public class DriveRoute{
	// Members
	private ArrayList<Location> route;
	private long startTime;
	private long endTime;
	private String feedback = "";
	private boolean hasFeedback = false;
	
	
	public DriveRoute(){
		this.route = new ArrayList<Location>();

	}
		
	public ArrayList<Location> getRoute(){
		return route;
	}

	public Location getStartLocation() {
		return route.get(0);
	}

	public Location getEndLocation() {
		return getRoute().get(getRoute().size() - 1);
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime() {
		this.startTime = route.get(0).getTimestamp();
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime() {
		this.endTime = getRoute().get(getRoute().size() - 1).getTimestamp();
	}

	public long getDuration() {
		return this.endTime - this.startTime;
	}
	
	public boolean isEnded() {
		return getEndTime() != 0;
	}

}
