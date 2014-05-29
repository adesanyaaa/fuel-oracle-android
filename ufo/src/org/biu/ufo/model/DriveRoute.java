package org.biu.ufo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.androidannotations.annotations.EBean;

import android.location.Address;

@EBean
public class DriveRoute{
	// Members
	private List<FuelingData> fueling;
	private List<DrivePoint> route;
	private long startTime;
	private long endTime;
	
	
	public DriveRoute(){
		this.route = new ArrayList<DrivePoint>();
		this.fueling = new ArrayList<FuelingData>();
	}
		
	public List<DrivePoint> getRoute(){
		return route;
	}	
	
	public List<FuelingData> getFuelingData() {
		return fueling;
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

	public Place getDestination(){
		
		DrivePoint finalPoint = route.get(route.size()-1);
		Address address = new Address(Locale.getDefault());
		address.setAddressLine(0, finalPoint.getLabel());
		address.setLatitude(finalPoint.getLocation().getLatitude());
		address.setLongitude(finalPoint.getLocation().getLongitude());
		Place destination = new Place(address);
		
		return destination;
	}
	
	public Place getSource(){
		DrivePoint startPoint = route.get(0);
		Address address = new Address(Locale.getDefault());
		address.setAddressLine(0, startPoint.getLabel());
		address.setLatitude(startPoint.getLocation().getLatitude());
		address.setLongitude(startPoint.getLocation().getLongitude());
		Place source = new Place(address);
		return source;
	}

}
