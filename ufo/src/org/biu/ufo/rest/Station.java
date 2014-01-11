package org.biu.ufo.rest;

public class Station {
	private Object original;
	private String address;
	private double lat;
	private double lng;
	private String company;
	private float price;

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

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
	
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

}
