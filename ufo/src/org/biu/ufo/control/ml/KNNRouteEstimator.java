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
	
	KNN knn;
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
		
		ArrayList<DataInstance> trainningSet = new ArrayList<DataInstance>();
		ArrayList<Double> attributes;
		for (int i = 0; i < 10; ++ i){
			attributes = new ArrayList<Double>();
			int hour = (i+5)%24;
			double latitude = 42.291595 + (Math.random() * (3));
			double longitude = -83.237617 + (Math.random() * (3));
			
			attributes.add(Double.valueOf(hour));
			attributes.add(latitude);
			attributes.add(longitude);
			
			Address address = new Address(Locale.getDefault());
			address.setAddressLine(0, "Location" + i%4);
			address.setLatitude(31.03118 - (i/100));
			address.setLongitude(32.03118 + (i/100));

			trainningSet.add(new DataInstance(attributes, new Place(address)));
			
		}
		knn = new KNN(trainningSet);
	}
	
	public List<Place> getDestinationEstimation(){
	
		List<Place> places = new ArrayList<Place>();
//		HashMap<Place, Double> placesMap = new HashMap<Place, Double>();
//		ArrayList<Double> testData = new ArrayList<Double>();
//
//		if (currentLocation == null || trainningData.size() <= 0){
//			return null;
//		}
//		testData.add(currentLocation.getLatitude());
//		testData.add(currentLocation.getLongitude());
//		testData.add(Double.valueOf(hour));
//
//		for (TrainingInstance instance: trainningData){
//			placesMap.put(instance.destination, Calculator.distance(instance.attributes, testData));
//		}
//		places = Ordering.natural().onResultOf(Functions.forMap(placesMap))
//				.sortedCopy(placesMap.keySet());
		ArrayList<Double> testData = new ArrayList<Double>();
		if (currentLocation == null){
			return null;
		}
		testData.add(Double.valueOf(hour));
		testData.add(currentLocation.getLatitude());
		testData.add(currentLocation.getLongitude());
		
		
		
		knn.evaluate(testData);
		Place place= (Place) knn.getEstimation(5);
		List<Object> sorted = knn.getTrainingListSorted();
		for (Object item : sorted){
			places.add((Place) item);
		}
		return places;
		
	}
	
}
