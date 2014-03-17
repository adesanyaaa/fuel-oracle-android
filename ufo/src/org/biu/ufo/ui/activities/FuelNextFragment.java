package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.user.ShowRecommendationsMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.utils.NavigationIntent;
import org.biu.ufo.ui.utils.UnitConverter;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.popup_fuel_next_layout)
public class FuelNextFragment extends Fragment {
	
	@Bean
	OttoBus bus;

	@ViewById
	TextView popup_fuel_price;
	
	@ViewById
	TextView popup_fuel_price_currency;
	
	@ViewById
	TextView popup_distance;
	
	@ViewById
	TextView popup_distance_unit;

	@ViewById
	TextView popup_station_address;
	
	@ViewById
	ImageView popup_gas_station_logo;

	@ViewById
	ImageButton popup_more_button;
	
	private Location location;
	
	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}

	@AfterViews
	public void initialize() {
		getView().setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(location != null) {
					startActivity(NavigationIntent.getNavigationIntent(location));
				}
			}
		});
		
		popup_more_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				bus.post(new ShowRecommendationsMessage());
			}
		});
	}
	
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		if(!message.shouldFuel() || message.getTopStation() == null) {
			getActivity().finish();
			return;
		}
		
		Station station = message.getTopStation();
		
		popup_fuel_price.setText(String.format("%.2f", station.getPrice()));
		popup_fuel_price_currency.setText(UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency()));

		popup_distance.setText(String.format("%.2f", station.getDistance()));
		popup_distance_unit.setText(UnitConverter.getResourceForDistanceUnit(station.getDistanceUnit()));

		popup_station_address.setText(station.getAddress());
		
	}

}
