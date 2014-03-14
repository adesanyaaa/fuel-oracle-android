package org.biu.ufo.rest;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.biu.ufo.rest.Station.CapacityUnit;
import org.biu.ufo.rest.Station.DistanceUnit;
import org.biu.ufo.rest.Station.PriceCurrency;
import org.biu.ufo.rest.internal.mygasfeed.RequestFuelType;
import org.biu.ufo.rest.internal.mygasfeed.RequestSortBy;
import org.biu.ufo.rest.internal.mygasfeed.StationsResponse;

import android.util.Log;

@EBean
public class MGFClient implements Client {
	@RestService
	org.biu.ufo.rest.internal.mygasfeed.Client _client;

	@Override
	public List<Station> getStations(String latitude, String longidute, float distance) {
		float distanceInMiles = distance * 0.621371f;
		StationsResponse response = _client.getStations(latitude, longidute, distanceInMiles,
				RequestFuelType.reg, RequestSortBy.price);
				
		List<Station> result = new ArrayList<Station>(response.stations.size());
		for(org.biu.ufo.rest.internal.mygasfeed.Station mgfStation : response.stations) {
			Station station = new Station(mgfStation);
			
			station.setAddress(mgfStation.address);

			station.setLat(mgfStation.lat);
			station.setLng(mgfStation.lng);

			station.setCompany(mgfStation.station);

			station.setPrice(Float.valueOf(mgfStation.price));
			station.setPriceCurrency(PriceCurrency.DOLLARS);
			station.setCapacityUnit(CapacityUnit.US_GALONS);
			
			String[] distanceText = mgfStation.distance.split(" ");
			if(distanceText.length == 2) {
				station.setDistance(Float.valueOf(distanceText[0]));
				if(mgfStation.distance.toLowerCase().contains("miles")) {
					station.setDistanceUnit(DistanceUnit.MILES);
				} else if(mgfStation.distance.toLowerCase().contains("km")){
					station.setDistanceUnit(DistanceUnit.KM);
				} else {
					station.setDistanceUnit(DistanceUnit.MILES);	// TODO
				}
			} else {
				Log.e("MGFClient", mgfStation.distance + " is invalid distance text!");
			}
			
			result.add(station);
		}
		
		return result;
	}

}
