package org.biu.ufo.control.events;

import org.biu.ufo.control.rest.internal.ufoserver.Location;

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
