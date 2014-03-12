package org.biu.ufo.events;

import org.biu.ufo.Formats;
import org.biu.ufo.rest.internal.ufoserver.Location;

import com.openxc.measurements.Latitude;

public class LocationMessage {

	public Location location;
	public boolean hasLat = false;
	public boolean hasLong = false;
	
	public LocationMessage(){
		location = new Location();
	}
	
	public void setLatitude(double latitude){
		location.latitude = latitude;
		hasLat = true;
	}

	public void setLongitude(double longitude){
		location.longitude = longitude;
		hasLong = true;
	}
	
	public boolean properLocation(){
		return hasLat&&hasLong;
	}
}
