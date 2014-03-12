package org.biu.ufo.status_analyzer.commands;

public class FuelProcessDetailsMessage extends FuellingProcessStatusMessage{

	public long startTime;
	public long endTime;
	public long duration;
	
	public float startFuelLevel;
	public float endFuelLevel;
	public float amount;
	
	
	public FuelProcessDetailsMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Details";
		
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
		this.duration = endTime - startTime;
	}


	public long getDuration() {
		return duration;
	}


	public float getStartFuelLevel() {
		return startFuelLevel;
	}


	public void setStartFuelLevel(float startFuelLevel) {
		this.startFuelLevel = startFuelLevel;
	}


	public float getEndFuelLevel() {
		return endFuelLevel;
	}


	public void setEndFuelLevel(float endFuelLevel) {
		this.endFuelLevel = endFuelLevel;
		this.amount = endFuelLevel - startFuelLevel;
	}


	public float getAmount() {
		return amount;
	}
}
