package org.biu.ufo.events.control;

import org.biu.ufo.model.Location;

public class FuelProcessMessage {

	private double startLevel;
	private double endLevel;
	private long startTime;
	private long endTime;
	private Location location;
	
	public FuelProcessMessage(double startLevel) {
		this.startLevel = startLevel;
		this.startTime = System.currentTimeMillis();
	}

	public long getStartTime() {
		return startTime;
	}


	public long getEndTime() {
		return endTime;
	}

	public double getStartFuelLevel() {
		return startLevel;
	}


	public double getEndFuelLevel() {
		return endLevel;
	}

	public void setEndFuelLevel(double endLevel) {
		this.endLevel = endLevel;
		this.endTime = System.currentTimeMillis();;
	}

	public boolean isCompleted() {
		return endTime != 0;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}
