package org.biu.ufo.rest;

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
	
	private float distance;
	private DistanceUnit distanceUnit;
	
	public Station(Object original) {
		this.original = original;
	}
	
	public Object getOriginal() {
		return original;
	}

	public String getAddress() {
		return address;
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

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public DistanceUnit getDistanceUnit() {
		return distanceUnit;
	}

	public void setDistanceUnit(DistanceUnit distanceUnit) {
		this.distanceUnit = distanceUnit;
	}

}
