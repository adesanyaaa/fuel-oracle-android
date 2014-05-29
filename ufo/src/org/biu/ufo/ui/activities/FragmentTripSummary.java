package org.biu.ufo.ui.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.components.StationsFetcher;
import org.biu.ufo.control.components.RouteEstimator.EstimatedRoute;
import org.biu.ufo.control.components.StationsFetcher.StationsFetcherResultHandler;
import org.biu.ufo.control.utils.Calculator;
import org.biu.ufo.model.DriveHistory;
import org.biu.ufo.model.DrivePoint;
import org.biu.ufo.model.DriveRoute;
import org.biu.ufo.model.FuelingData;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.storage.RouteDataStore;

import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("ValidFragment")
@EFragment(R.layout.trip_summary) 
class FragmentTripSummary extends Fragment implements StationsFetcherResultHandler{

	
	private static final String NO_DATA = "no data";
	@Bean
	OttoBus bus;


	@ViewById
	TextView trip_duration;

	@ViewById
	TextView trip_start;

	@ViewById
	TextView trip_fueling_company;

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

	@Bean 
	StationsFetcher stationsFetcher;

	@Bean
	RouteDataStore routeDataStore;

	double gasWaste = 0;
	FuelingData fuelingDetails;

	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@AfterViews
	protected void setupContent() {

		routeDataStore.open();


		DriveHistory history = routeDataStore.getRoutesHistory(2);
		DriveRoute route = history.getRouteByIndex(0);
		//not empty
		if (route != null){
			List<FuelingData> fuelingData = route.getFuelingData();

			if (route.getRoute().size() == 0){

				route = history.getRouteByIndex(1);
			}

			trip_destination.setText(route.getDestination().getAddress().getAddressLine(0));
			trip_source.setText(route.getSource().getAddress().getAddressLine(0));


			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			trip_start.setText(formatter.format(new Date(route.getStartTime())));
			trip_end.setText(formatter.format(new Date(route.getEndTime())));

			formatter = new SimpleDateFormat("HH:mm:ss");
			trip_duration.setText(formatter.format(new Date(route.getDuration())));

			//TODO: set with start and end fuel level of the trip
			gasWaste = route.getStartFuelLevel() - route.getEndFuelLevel();
			ArrayList<LatLng> positions = new ArrayList<LatLng>();
			positions.add(new LatLng(route.getEndLocation().getLatitude(), route.getEndLocation().getLongitude()));
			stationsFetcher.requestStations(positions, this);		

			if (fuelingData.size() == 0){
				fuel_layout.setVisibility(View.GONE);
				fuelingDetails = null;
			}else{
				fuelingDetails = fuelingData.get(0);
				trip_fueling_destination.setText(fuelingDetails.address);
				trip_fueling_company.setText(fuelingDetails.company);
				trip_currency.setText(getString(R.string.currency_dollar));

				trip_cost.setText(String.valueOf(fuelingDetails.price*gasWaste));
			}	
		}else{
			trip_destination.setText(NO_DATA);
			trip_source.setText(NO_DATA);
			trip_cost.setText(NO_DATA);
			trip_fueling_company.setText(NO_DATA);
			trip_fueling_destination.setText(NO_DATA);
			trip_duration.setText(NO_DATA);
			trip_start.setText(NO_DATA);
			trip_end.setText(NO_DATA);
			trip_saved.setText(NO_DATA);
		}

	}


	@Override
	public void onStationsResult(List<Station> stations) {

		double avgFuelRate = getAverageFuelRate(stations);
		trip_cost.setText(String.valueOf(gasWaste*avgFuelRate));

		if (fuelingDetails != null){
			double fuelAmount = fuelingDetails.endLevel - fuelingDetails.startLevel;
			double fuelingCostRecommendation = fuelAmount*fuelingDetails.price;
			double fuelingCost = fuelAmount*avgFuelRate;
			trip_saved.setText(String.valueOf(fuelingCost-fuelingCostRecommendation));

		}
	}

	private double getAverageFuelRate(List<Station> stations) {
		double sum = 0;
		for (Station station: stations){
			sum+=station.getPrice();
		}
		return sum/stations.size();
	}



}