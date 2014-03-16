package org.biu.ufo.control.events.analyzer.routemonitor;

import java.util.ArrayList;

import org.biu.ufo.control.events.StatusMessage;
import org.biu.ufo.model.Location;

public class RouteSummaryMessage extends StatusMessage {
	
	private ArrayList<Location> route;
	private long startTime;
	private long endTime;
	
	public RouteSummaryMessage(){
		this.route = new ArrayList<Location>();
		this.message = "Route details";
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

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getDuration() {
		return this.endTime - this.startTime;
	}
	
	public boolean isEnded() {
		return getEndTime() != 0;
	}

	@Override
	public long getTime() {
		if(endTime != 0) {
			return endTime;
		}
		return startTime;
	}
}
