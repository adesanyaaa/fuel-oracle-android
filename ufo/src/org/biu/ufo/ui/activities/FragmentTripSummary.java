package org.biu.ufo.ui.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.model.DrivePoint;
import org.biu.ufo.model.DriveRoute;
import org.biu.ufo.model.Location;
import org.biu.ufo.storage.RouteDataStore;

import android.annotation.SuppressLint;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("ValidFragment")
@EFragment(R.layout.trip_summary) 
class FragmentTripSummary extends Fragment {

	@Bean
	OttoBus bus;
		
	@ViewById
	TextView trip_duration;
	
	@ViewById
	TextView trip_start;
	
	@ViewById
	TextView trip_end;	
	
	@ViewById	
	TextView trip_source;

	@ViewById	
	TextView trip_destination;

	@ViewById	
	TextView trip_cost;

	@ViewById	
	TextView trip_currency;
	
	@ViewById	
	TextView trip_fueling_destination;
	
	@ViewById	
	TextView trip_saved;
	
	@ViewById
	LinearLayout fuel_layout;
	
	boolean updateNeeded = true;
	
	@Bean
	RouteDataStore routeDataStore;
	
	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
		updateNeeded = true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@AfterViews
	protected void setupContent() {
		/*
		routeDataStore.open();
		
		//TODO: make sure it is the latest route!
		DriveRoute route = routeDataStore.getRoutesHistory(1).getRouteByIndex(0);
		*/
		
		DriveRoute route = getTestRoute();
		trip_destination.setText(route.getDestination().getAddress().getAddressLine(0));
		trip_source.setText(route.getSource().getAddress().getAddressLine(0));
		
		
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		trip_start.setText(formatter.format(new Date(route.getStartTime())));
		trip_end.setText(formatter.format(new Date(route.getEndTime())));
		
		formatter = new SimpleDateFormat("HH:mm:ss");
		trip_duration.setText(formatter.format(new Date(route.getDuration())));
		
		//TODO project drive cost data
		double fuel_rate = 0.13;
		double alter_fuel_rate = 0.16;  // alternative
		double gas_tank_before = 60;
		double gas_tank_after = 20;
		double total_cost = fuel_rate*(gas_tank_before-gas_tank_after);
		double alter_cost = alter_fuel_rate*(gas_tank_before-gas_tank_after);
		trip_cost.setText(String.valueOf(fuel_rate*(gas_tank_before-gas_tank_after)));
		trip_fueling_destination.setText("Somewhere");
		trip_saved.setText(String.valueOf(alter_cost-total_cost));
		
	}

	private DriveRoute getTestRoute() {
		DriveRoute driveRoute = new DriveRoute();
		DrivePoint point = new DrivePoint();
		point.setLocation(new Location());
		point.setLabel("The Source of all evil");
		driveRoute.add(point);
		
		point = new DrivePoint();
		point.setLocation(new Location());
		point.setLabel("The Destination of all evil");
		driveRoute.add(point);
		driveRoute.setStartTime(0);
		int extra = new Random().nextInt(100000);
		driveRoute.setEndTime(60000 + extra);
		return driveRoute;
	}
	
	
	
}