package org.biu.ufo.control.events.analyzer.recommendation;

import java.util.List;

import org.biu.ufo.rest.Station;

public class FuelNextRecommendation {
	final List<Station> stations;
	
	public FuelNextRecommendation(List<Station> stations) {
		this.stations = stations;
	}
	
	public List<Station> getStations() {
		return stations;
	}
	
	public Station getTopStation() {
		if(!stations.isEmpty())
			return stations.get(0);
		return null;
	}

	public boolean shouldFuel() {
		return stations != null;
	}
}
