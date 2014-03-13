package org.biu.ufo.control.rest.internal.mygasfeed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Station {
	public String country;
	public String price;
	public String address;
	public int diesel; // 1=true
	public long id;
	public double lat;
	public double lng;
	public String station;
	public String region;
	public String city;
	public String date;
	public String distance;
}
