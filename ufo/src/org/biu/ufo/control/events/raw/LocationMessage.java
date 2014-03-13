package org.biu.ufo.control.events.raw;

import org.biu.ufo.model.Location;


public class LocationMessage {

	public Location location;
	public boolean hasLat = false;
	public boolean hasLong = false;
	
	public LocationMessage(){
		location = new Location();
	}
	
	public void setLatitude(double latitude){
		location.setLatitude(latitude);
		hasLat = true;
	}

	public void setLongitude(double longitude){
		location.setLongitude(longitude);
		hasLong = true;
	}
	
	public boolean properLocation(){
		return hasLat && hasLong;
	}
	
	public Location getLocation() {
		if(properLocation()) {
			return location;			
		}
		return null;
	}
}
