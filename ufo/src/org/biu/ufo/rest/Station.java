package org.biu.ufo.rest;

import org.biu.ufo.control.Calculator;
import org.biu.ufo.model.Location;

public class Station {
	public enum PriceCurrency {DOLLARS, CENTS, NIS};
	public enum DistanceUnit {KM, MILES};
	public enum CapacityUnit {US_GALONS, UK_GALONS, LITTERS};

	private Object original;
	
	private String address;
	private double lat;
	private double lng;
	private String company;
	
	private float price;
	private PriceCurrency priceCurrency;
	private CapacityUnit capacityUnit;
	
//	private double distance;
//	private DistanceUnit distanceUnit;
	
	private double distanceFromRoute;
	
	private int duration;
	
	public Station(Object original) {
		this.original = original;
	}
	
	public Object getOriginal() {
		return original;
	}

	public String getAddress() {
		return address;
	}

	
	public void setDistanceFromRoute(double distance){
		this.distanceFromRoute = distance;
	}
	
	public double getDistanceFromRoute(){
		return this.distanceFromRoute;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public PriceCurrency getPriceCurrency() {
		return priceCurrency;
	}

	public void setPriceCurrency(PriceCurrency priceCurrency) {
		this.priceCurrency = priceCurrency;
	}

	public CapacityUnit getCapacityUnit() {
		return capacityUnit;
	}

	public void setCapacityUnit(CapacityUnit capacityUnit) {
		this.capacityUnit = capacityUnit;
	}
	
	public double getDistance(Location reference) {
		return Calculator.distance(reference, getLocation());
	}


//	public double getDistance() {
//		return distance;
//	}

//	public void setDistance(double distance) {
//		this.distance = distance;
//	}
//
//	public DistanceUnit getDistanceUnit() {
//		return distanceUnit;
//	}
//
//	public void setDistanceUnit(DistanceUnit distanceUnit) {
//		this.distanceUnit = distanceUnit;
//	}
	
	public DistanceUnit getDistanceUnit() {
		return DistanceUnit.KM;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lng);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Station other = (Station) obj;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lng) != Double.doubleToLongBits(other.lng))
			return false;
		return true;
	}

	public Location getLocation() {
		// TODO Auto-generated method stub
		return new Location(lat, lng);
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

}
