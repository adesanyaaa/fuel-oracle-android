package org.biu.ufo.model;

import com.google.android.gms.maps.model.LatLng;

public class Location {
	private double latitude;
	private double longitude;
	
	public Location(Location other){
		this.latitude = other.latitude;
		this.longitude = other.longitude;
	}

	public Location(double latitude, double longitude){
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Location(){
		this.latitude = 0;
		this.longitude = 0;
	}
	
	public Location(LatLng latlng){
		this.latitude = latlng.latitude;
		this.longitude = latlng.longitude;
	}

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}
	
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
