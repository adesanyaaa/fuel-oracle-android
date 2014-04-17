package org.biu.ufo.control.events.analyzer.fueling;

public class FuelProcessMessage {

	private double startLevel;
	private double endLevel;
	public long startTime;
	public long endTime;

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

}
