package org.biu.ufo.control;

import org.biu.ufo.model.Location;

import com.google.android.gms.maps.model.LatLng;

public class Calculator {
	
	public static float estimateLitersPer100Km(float metric_speed, float litersPerHour) {
	    return (100 / metric_speed) * litersPerHour;
	}
	
	public static float getLitersFromUSGallons(float galons) {
		return 3.78541178f * galons;
	}	

	public static float estimateMilesPerUSGallon(float metric_speed, float litersPerHour) {
	    return 235.2f / estimateLitersPer100Km(metric_speed, litersPerHour);
	}

	// In KM
	public static double distance(Location loc1, Location loc2) {
		return distance(loc1.getLatitude(), loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude());
	}
	
	public static double distance(LatLng loc1, LatLng loc2) {
		return distance(loc1.latitude, loc1.longitude, loc2.latitude, loc2.longitude);
	}

	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		double theta = lon1 - lon2;
		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
		dist = Math.acos(dist);
		dist = rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return (dist);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts decimal degrees to radians             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	public static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	/*::  This function converts radians to decimal degrees             :*/
	/*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
	public static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

}
