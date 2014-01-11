package org.biu.ufo.rest.internal.ufoserver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Station {
	@JsonProperty("m_id")
	public long id;
	
	@JsonProperty("AD_TEXT")
	public String adText;
	
	@JsonProperty("COMPANY")
	public String company;
	
	@JsonProperty("LOCATION")
	public Location location;

	@JsonProperty("DISTANCE")
	public double distance;
	
	@JsonProperty("ADDRESS")
	public String address;
	
	@JsonProperty("PETROL95")
	public float cost;
}
