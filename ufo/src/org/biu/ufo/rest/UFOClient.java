package org.biu.ufo.rest;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.biu.ufo.rest.Station.CapacityUnit;
import org.biu.ufo.rest.Station.PriceCurrency;

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
			
			station.setLat(ufoStation.latitude);
			station.setLng(ufoStation.longitude);

			station.setCompany(ufoStation.company);

			station.setPrice(ufoStation.cost);
			station.setPriceCurrency(PriceCurrency.NIS);
			station.setCapacityUnit(CapacityUnit.LITTERS);
			
//			station.setDistance((float)ufoStation.distance); - USE Calculator.distance
//			station.setDistanceUnit(DistanceUnit.KM);
			
			result.add(station);
		}
		
		return result;
	}

}
