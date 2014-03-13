package org.biu.ufo.rest;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;

@EBean
public class UFOClient implements Client {
	@RestService
	org.biu.ufo.rest.internal.ufoserver.Client _client;
	
	@Override
	public List<Station> getStations(String latitude, String longitude, float distance) {
		List<org.biu.ufo.rest.internal.ufoserver.Station> stations = _client.getStations(latitude, longitude, distance);
		
		List<Station> result = new ArrayList<Station>(stations.size());
		for(org.biu.ufo.rest.internal.ufoserver.Station ufoStation : stations) {
			Station station = new Station(ufoStation);
			station.setAddress(ufoStation.address);
			station.setDistance(String.format("%.2f", ufoStation.distance) + "km");
			station.setLat(ufoStation.location.latitude);
			station.setLng(ufoStation.location.longitude);
			station.setCompany(ufoStation.company);
			station.setPrice(String.format("%.2f", ufoStation.cost));
			station.setPriceCurrency("\u20AA");
			result.add(station);
		}
		
		return result;
	}

}
