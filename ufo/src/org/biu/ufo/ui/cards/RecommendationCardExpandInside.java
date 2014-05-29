package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.CardExpand;

import org.biu.ufo.R;
import org.biu.ufo.events.control.FuelRecommendationMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.utils.NavigationIntent;
import org.biu.ufo.ui.utils.UnitConverter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RecommendationCardExpandInside extends CardExpand {

//	private double fuelAmount;
//	private int fuelMeasurementResId;
//
//	private double fuelTotalCost;
//	private int fuelCostCurrencyResId;
//
//	private String stationAddress;

	private double stationDistance;
//	private int stationDistanceUnitResId;
	
//	private String company;
//	private int companyLogo;
//
//	private Location location;

	
	TextView station_distance;
	TextView fuel_amount;
	TextView fuel_measurement;
	TextView fuel_total_cost;
	TextView fuel_cost_currency;
	TextView station_company;
	TextView station_distance_measurement;
	ImageView company_logo;
	Button navigateButton;
	
	FuelRecommendationMessage recommendation;
	Station station;
	
	public RecommendationCardExpandInside(Context context,
			FuelRecommendationMessage recommendation,
			Station station,
			Location currentLocation) {
		
		super(context, R.layout.recommendation_expand_layout);
		
		this.recommendation = recommendation;
		this.station = station;
		if(currentLocation == null) {
			currentLocation = recommendation.getLocationAtRecommendTime();
		}
		this.stationDistance = station.getDistance(currentLocation);
		
//		this.location = new Location(station.getLat(), station.getLng());
//		this.stationAddress = station.getAddress();
//		this.stationDistance = station.getDistance();
//		this.stationDistanceUnitResId = UnitConverter.getResourceForDistanceUnit(station.getDistanceUnit());

//		this.company = station.getCompany();
//		this.companyLogo = UnitConverter.getResourceForStationLogo(station.getCompany());
//		this.fuelCostCurrencyResId = UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency());
//		this.fuelMeasurementResId = UnitConverter.getResourceForCapacityUnit(station.getCapacityUnit());
//		this.fuelAmount = recommendation.getFuelAmount(station.getCapacityUnit());
//		this.fuelTotalCost = this.fuelAmount * station.getPrice();
		
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		double fuelAmount = recommendation.getFuelAmount(station.getCapacityUnit());
		fuel_amount = (TextView)view.findViewById(R.id.rec_fuel_amount);
		fuel_amount.setText(String.format( "%.2f", recommendation.getFuelAmount(station.getCapacityUnit())));

		fuel_measurement = (TextView)view.findViewById(R.id.rec_fuel_measurement);
		fuel_measurement.setText(UnitConverter.getResourceForCapacityUnit(station.getCapacityUnit()));

		double fuelTotalCost = fuelAmount * station.getPrice();
		fuel_total_cost = (TextView)view.findViewById(R.id.rec_fuel_total_cost);
		fuel_total_cost.setText(String.format( "%.2f",fuelTotalCost));

		fuel_cost_currency = (TextView)view.findViewById(R.id.rec_fuel_cost_currency);
		fuel_cost_currency.setText(UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency()));

		station_company = (TextView)view.findViewById(R.id.rec_station_company);
		if(!TextUtils.isEmpty(station.getCompany())) {
			station_company.setText(station.getCompany());			
		} else {
			station_company.setText(station.getAddress());
		}

		station_distance = (TextView)view.findViewById(R.id.rec_station_distance);
		station_distance.setText(String.format("%.2f", stationDistance));

		station_distance_measurement = (TextView)view.findViewById(R.id.rec_station_distance_measurement);
		station_distance_measurement.setText(UnitConverter.getResourceForDistanceUnit(station.getDistanceUnit()));

		company_logo = (ImageView)view.findViewById(R.id.rec_company_logo);
		company_logo.setImageResource(UnitConverter.getResourceForStationLogo(station.getCompanyEnglish()));
		
		navigateButton = (Button)view.findViewById(R.id.rec_navigate_button);
		navigateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(station.getLocation() != null) {
					Intent intent = NavigationIntent.getNavigationIntent(station.getLocation());
					getContext().startActivity(intent);            		
				}				
			}
		});
	}

	public void setCurrentLocation(Location location) {
		this.stationDistance = station.getDistance(location);		
	}

	public double getStationDistance() {
		return stationDistance;
	}
	
	public Station getStation() {
		return station;
	}

	
//	public void setFuelAmount(double fuelAmount) {
//		this.fuelAmount = fuelAmount;
//		fuel_amount.setText(String.format("%.2f",fuelAmount));
//	}
//
//	public void setFuelMeasurementResId(int fuelMeasurementResId) {
//		this.fuelMeasurementResId = fuelMeasurementResId;
//		fuel_measurement.setText(fuelMeasurementResId);
//	}
//
//	public void setFuelTotalCost(double fuelTotalCost) {
//		this.fuelTotalCost = fuelTotalCost;
//		fuel_total_cost.setText(String.format("%.2f",fuelTotalCost));
//	}
//
//	public void setFuelCostCurrencyResId(int fuelCostCurrencyResId) {
//		this.fuelCostCurrencyResId = fuelCostCurrencyResId;
//		fuel_cost_currency.setText(fuelCostCurrencyResId);
//	}

//	public void setStationAddress(String stationAddress) {
//		this.stationAddress = stationAddress;
//		if(!TextUtils.isEmpty(company)) {
//			station_company.setText(company);			
//		} else {
//			station_company.setText(this.stationAddress);
//		}
//
//	}

//	public void setStationDistance(double stationDistance) {
//		this.stationDistance = stationDistance;
//		station_distance.setText(String.format("%.2f", stationDistance));
//
//	}

//	public void setStationDistanceUnitResId(int stationDistanceUnitResId) {
//		this.stationDistanceUnitResId = stationDistanceUnitResId;
//		station_distance_measurement.setText(stationDistanceUnitResId);
//
//	}

//	public void setLocation(Location location) {
//		this.location = location;
//	}
	
//	public void setCompanyLogo(int companyLogo){
//		this.companyLogo = companyLogo;
//		company_logo.setImageResource(companyLogo);
//	}
//
//	public String getStationCompany() {
//		return company;
//	}
//	
//	public void setStationCompany(String company) {
//		this.company = company;
//	}


//	public double getStationDistance(){
//		return stationDistance;
//	}
	
//	public Location getLocation(){
//		return location;
//	}
}
