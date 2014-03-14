package org.biu.ufo.control.events.analyzer.recommendation;

import java.util.List;

import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;

public class FuelNextRecommendation {
	private long time;
	Double fuelLevelAtRecommendTime;
	Location locationAtRecommendTime;
	List<Station> stations;

	public FuelNextRecommendation() {
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
	
	public void setStations(List<Station> stations) {
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
