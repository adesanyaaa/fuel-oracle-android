package org.biu.ufo.control.rest.internal.mygasfeed;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class StationsResponse {
	public List<Station> stations;
}
