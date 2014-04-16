package org.biu.ufo.control.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.biu.ufo.control.Calculator;
import org.biu.ufo.model.Location;


import org.biu.ufo.model.Place;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

import android.location.Address;

/**
 * Estimates the destination using KNN algorithm 
 * based on routes history (Source location and hour)
 */
public class KNNRouteEstimator {
	
	public class TrainingInstance{
		ArrayList<Double> attributes;
		Place destination;
		
		public TrainingInstance(double longitude, double latitude, int hour){
			attributes = new ArrayList<Double>();
			attributes.add(longitude);
			attributes.add(latitude);
			attributes.add(Double.valueOf(hour));
			
		}
	
	}
	
	ArrayList<TrainingInstance> trainningData;
	Location currentLocation = null;
	int hour;
	
	
	public KNNRouteEstimator(Location location, int hour){
		this.currentLocation = location;
		this.hour = hour;
		setInstances();
	}

	public void setCurrentInstance(Location location, int hour){
		this.currentLocation = location;
		this.hour = hour;
	}
	
	private void setInstances(){
		//TODO:load route history - currently static
		Random random = new Random();
		trainningData = new ArrayList<TrainingInstance>();
		for (int i = 0; i < 10; ++ i){
			int hour = (i+5)%24;
			double latitude = 31.03118 + (random.nextInt()/100);
			double longitude = 32.03118 + (random.nextInt()/100);
			TrainingInstance instance = new TrainingInstance(latitude,longitude,hour);

			Address address = new Address(Locale.getDefault());
			address.setAddressLine(0, "Location" + i);
			address.setLatitude(31.03118 - (i/100));
			address.setLongitude(32.03118 + (i/100));
			instance.destination = new Place(address);

			trainningData.add(instance);
			
		}
	}
	
	public List<Place> getDestinationEstimation(){
	
		List<Place> places;
		HashMap<Place, Double> placesMap = new HashMap<Place, Double>();
		ArrayList<Double> testData = new ArrayList<Double>();
		
		if (currentLocation == null || trainningData.size() <= 0){
			return null;
		}
		testData.add(currentLocation.getLatitude());
		testData.add(currentLocation.getLongitude());
		testData.add(Double.valueOf(hour));
		
		for (TrainingInstance instance: trainningData){
			placesMap.put(instance.destination, Calculator.distance(instance.attributes, testData));
		}
		places = Ordering.natural().onResultOf(Functions.forMap(placesMap))
				   .sortedCopy(placesMap.keySet());
		
		return places;
		
	}
	
}
