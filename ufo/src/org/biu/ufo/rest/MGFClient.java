package org.biu.ufo.rest;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.biu.ufo.rest.internal.mygasfeed.RequestFuelType;
import org.biu.ufo.rest.internal.mygasfeed.RequestSortBy;
import org.biu.ufo.rest.internal.mygasfeed.StationsResponse;

@EBean
public class MGFClient implements Client {
	@RestService
	org.biu.ufo.rest.internal.mygasfeed.Client _client;

	@Override
	public List<Station> getStations(String latitude, String longidute, float distance) {
		float distanceInMiles = distance * 0.000621371f;
		StationsResponse response = _client.getStations(latitude, longidute, distanceInMiles,
				RequestFuelType.reg, RequestSortBy.price);
				
		List<Station> result = new ArrayList<Station>(response.stations.size());
		for(org.biu.ufo.rest.internal.mygasfeed.Station mgfStation : response.stations) {
			Station station = new Station(mgfStation);
			station.setAddress(mgfStation.address);
			station.setLat(mgfStation.lat);
			station.setLng(mgfStation.lng);
			station.setPrice(mgfStation.price);
			station.setCompany(mgfStation.station);
			result.add(station);
		}
		
		return result;
	}

}
