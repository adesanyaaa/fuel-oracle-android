package org.biu.ufo.control.events.analyzer.recommendation;

import java.util.LinkedList;
import java.util.List;

import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;

public class FuelRecommendationMessage {
	private long time;
	Double fuelLevelAtRecommendTime;
	Location locationAtRecommendTime;
	List<Station> stations = new LinkedList<Station>();

	public FuelRecommendationMessage() {
		this.time = System.currentTimeMillis();
	}
	
	public long getTime() {
		return time;
	}
	
	public void setFuelLevel(double fuelLevel) {
		this.fuelLevelAtRecommendTime = fuelLevel;
	}
	
	public Double getFuelLevelAtRecommendTime() {
		return fuelLevelAtRecommendTime;
	}
	
	public void setLocation(Location locationAtRecommendTime) {
		this.locationAtRecommendTime = locationAtRecommendTime;
	}
	
	public Location getLocationAtRecommendTime() {
		return locationAtRecommendTime;
	}
	
	public void addStations(List<Station> stations) {
		for(Station station : stations) {
			if(!this.stations.contains(station)) {
				this.stations.add(station);
			}
		}
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
		return locationAtRecommendTime != null; /*!stations.isEmpty();*/
	}	
	
}
