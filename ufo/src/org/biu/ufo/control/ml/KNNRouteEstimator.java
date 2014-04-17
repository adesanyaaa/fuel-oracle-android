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
public class KNNRouteEstimator extends KNN{
	
	Location currentLocation = null;
	int hour;
	
	public KNNRouteEstimator(Location location, int hour){
		super(getTrainingSet());
		this.currentLocation = location;
		this.hour = hour;
		
	}

	public void setCurrentInstance(Location location, int hour){
		this.currentLocation = location;
		this.hour = hour;
	}
	
	private static ArrayList<DataInstance> getTrainingSet(){
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
		return trainningSet;
	}
	
	/**
	 * @return training instances sorted by similarity to test details.
	 */
	public List<Place> getInstancesSortedBySimilarity(){
	
		ArrayList<Double> testData = new ArrayList<Double>();
		if (currentLocation == null){
			return null;
		}
		testData.add(Double.valueOf(hour));
		testData.add(currentLocation.getLatitude());
		testData.add(currentLocation.getLongitude());
		
		evaluate(testData);
		//Place place= (Place) getEstimation(5);
		
		List<Place> places = new ArrayList<Place>();
		List<Object> sorted = getTrainingListSorted();
		for (Object item : sorted){
			places.add((Place) item);
		}
		return places;
		
	}
	
	public Place getEstimation(){
		return (Place) super.getEstimation();
	}
	
	public Place getEstimation(int k){
		return (Place) super.getEstimation(k);
	}
	
}
