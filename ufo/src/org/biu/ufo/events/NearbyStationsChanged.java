package org.biu.ufo.events;

import java.util.Collection;

import org.biu.ufo.rest.Station;

public class NearbyStationsChanged {
	private Collection<Station> stations;
	
	public NearbyStationsChanged(Collection<Station> stations) {
		this.stations = stations;
	}
	
	public Collection<Station> getStations() {
		return stations;
	}
}
