package org.biu.ufo.model;

import android.location.Address;

public class Place {
	private Address address;
	private String label;
	boolean isFavorite;
	
	public Place(Address address) {
		this.address = address;
	}
	
	public Place(Address address, String label, boolean isFavorite) {
		this.address = address;
		this.label = label;
		this.isFavorite = isFavorite;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public String getLabel() {
		if(label == null)
			return "";
		return label;
	}
	
	public boolean isFavorite() {
		return isFavorite;
	}
	
	@Override
	public String toString() {
		String name = "";
		int max = address.getMaxAddressLineIndex();
		if(max == 0)
			max = 1;
		for (int i = 0; i < max; i++) {
			if (name.length() > 0) name += " ";
			name += address.getAddressLine(i);
		}
		return name;
	}

}