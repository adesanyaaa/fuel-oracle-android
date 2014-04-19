package org.biu.ufo.control.ml;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.model.DriveHistory;
import org.biu.ufo.model.DriveRoute;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;
import org.biu.ufo.storage.RouteDataStore;

import com.google.common.annotations.Beta;

import android.location.Address;

/**
 * Estimates the destination using KNN algorithm 
 * based on routes history (Source location and hour)
 */
@EBean
public class KNNRouteEstimator extends KNN{
	
	@Bean
	RouteDataStore routeDataStore;

	public KNNRouteEstimator(){
		super();
		setTrainingSet(getHistoryTrainingSet());
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
	
	public ArrayList<DataInstance> getHistoryTrainingSet(){
		ArrayList<DataInstance> trainningSet = new ArrayList<DataInstance>();
		ArrayList<Double> attributes;
		
		///routeDataStore NULL EXCEPTION!!
		routeDataStore.open();
		DriveHistory history = routeDataStore.getRoutesHistory(-1);
		for  (int i = 0; i < history.getTotalRoutes(); ++i){
			DriveRoute route = history.getRouteByIndex(i);
			Location startLocation = route.getStartLocation();
			
			Calendar startRoute = Calendar.getInstance();
			startRoute.setTimeInMillis(route.getStartTime());	
			
			attributes = convertToAttributes(startLocation.getLatitude(), startLocation.getLongitude(), startRoute.get(Calendar.HOUR_OF_DAY));			
			trainningSet.add(new DataInstance(attributes, route.getDestination()));
			
		}
	
		routeDataStore.close();
		return trainningSet;
	}
	
	private static ArrayList<Double> convertToAttributes(double latitude, double longitude, int hour){
		ArrayList<Double> attributes = new ArrayList<Double>();
		attributes.add(Double.valueOf(hour));
		attributes.add(latitude);
		attributes.add(longitude);
		
		return attributes;
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
