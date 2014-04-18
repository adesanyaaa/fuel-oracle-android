package org.biu.ufo.control.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.biu.ufo.model.Place;

import android.location.Address;

/**
 * Estimates the destination using KNN algorithm 
 * based on routes history (Source location and hour)
 */
public class KNNRouteEstimator extends KNN{
	
	public KNNRouteEstimator(ArrayList<DataInstance> trainingSet){
		super(trainingSet);
	}

	public KNNRouteEstimator(){
		super(getStaticTrainingSet());
	}
	
	public static ArrayList<DataInstance> getStaticTrainingSet(){
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
			address.setAddressLine(0, "Location " + i);
			address.setLatitude(31.03118 - (i/100));
			address.setLongitude(32.03118 + (i/100));
			trainningSet.add(new DataInstance(attributes, new Place(address)));
			
		}
		return trainningSet;
	}
	
	/**
	 * @return training instances sorted by similarity to test details.
	 */
	@Override
	public List<Place> getTrainingListSorted(){
		return (List<Place>) super.getTrainingListSorted();
	}
	
	@Override
	public Place getEstimation(){
		return (Place) super.getEstimation();
	}
	
	@Override
	public Place getEstimation(int k){
		return (Place) super.getEstimation(k);
	}
	
}
