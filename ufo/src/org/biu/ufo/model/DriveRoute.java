package org.biu.ufo.model;

import java.util.ArrayList;
import org.androidannotations.annotations.EBean;

@EBean
public class DriveRoute{
	// Members
	private ArrayList<DrivePoint> route;
	private long startTime;
	private long endTime;
	
	
	public DriveRoute(){
		this.route = new ArrayList<DrivePoint>();

	}
		
	public ArrayList<DrivePoint> getRoute(){
		return route;
	}

	
	
	
	public Location getStartLocation() {
		return route.get(0).getLocation();
	}

	public Location getEndLocation() {
		return getRoute().get(getRoute().size() - 1).getLocation();
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime() {
		this.startTime = route.get(0).getLocation().getTimestamp();
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime() {
		this.endTime = getRoute().get(getRoute().size() - 1).getLocation().getTimestamp();
	}

	public long getDuration() {
		return this.endTime - this.startTime;
	}
	
	public boolean isEnded() {
		return getEndTime() != 0;
	}

}
