package org.biu.ufo.control.events;

import java.util.Collection;

import org.biu.ufo.control.rest.Station;

public class NearbyStationsChanged {
	private Collection<Station> stations;
	
	public NearbyStationsChanged(Collection<Station> stations) {
		this.stations = stations;
	}
	
	public Collection<Station> getStations() {
		return stations;
	}
}
