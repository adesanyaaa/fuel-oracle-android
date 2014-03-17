package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.CardExpand;

import org.biu.ufo.R;
import org.biu.ufo.model.Location;
import org.biu.ufo.ui.utils.NavigationIntent;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RecommendationCardExpandInside extends CardExpand {

	private double fuelAmount;
	private int fuelMeasurementResId;

	private double fuelTotalCost;
	private int fuelCostCurrencyResId;

	private String stationAddress;

	private double stationDistance;
	private int stationDistanceUnitResId;
	
	private int companyLogo;

	private Location location;

	public RecommendationCardExpandInside(Context context) {
		super(context, R.layout.recommendation_expand_layout);
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {

		TextView fuel_amount = (TextView)view.findViewById(R.id.rec_fuel_amount);
		fuel_amount.setText(String.format( "%.2f",fuelAmount));

		TextView fuel_measurement = (TextView)view.findViewById(R.id.rec_fuel_measurement);
		fuel_measurement.setText(fuelMeasurementResId);

		TextView fuel_total_cost = (TextView)view.findViewById(R.id.rec_fuel_total_cost);
		fuel_total_cost.setText(String.format( "%.2f",fuelTotalCost));

		TextView fuel_cost_currency = (TextView)view.findViewById(R.id.rec_fuel_cost_currency);
		fuel_cost_currency.setText(fuelCostCurrencyResId);

		TextView station_address = (TextView)view.findViewById(R.id.rec_station_address);
		station_address.setText(stationAddress);

		TextView station_distance = (TextView)view.findViewById(R.id.rec_station_distance);
		station_distance.setText(String.format("%.2f", stationDistance));

		TextView station_distance_measurement = (TextView)view.findViewById(R.id.rec_station_distance_measurement);
		station_distance_measurement.setText(stationDistanceUnitResId);

		ImageView company_logo = (ImageView)view.findViewById(R.id.rec_company_logo);
		company_logo.setImageResource(companyLogo);
		
		Button navigateButton = (Button)view.findViewById(R.id.rec_navigate_button);
		navigateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(location != null) {
					Intent intent = NavigationIntent.getNavigationIntent(location);
					getContext().startActivity(intent);            		
				}				
			}
		});
	}

	public void setFuelAmount(double fuelAmount) {
		this.fuelAmount = fuelAmount;
	}

	public void setFuelMeasurementResId(int fuelMeasurementResId) {
		this.fuelMeasurementResId = fuelMeasurementResId;
	}

	public void setFuelTotalCost(double fuelTotalCost) {
		this.fuelTotalCost = fuelTotalCost;
	}

	public void setFuelCostCurrencyResId(int fuelCostCurrencyResId) {
		this.fuelCostCurrencyResId = fuelCostCurrencyResId;
	}

	public void setStationAddress(String stationAddress) {
		this.stationAddress = stationAddress;
	}

	public void setStationDistance(double stationDistance) {
		this.stationDistance = stationDistance;
	}

	public void setStationDistanceUnitResId(int stationDistanceUnitResId) {
		this.stationDistanceUnitResId = stationDistanceUnitResId;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public void setCompanyLogo(int companyLogo){
		this.companyLogo = companyLogo;
	}
}
