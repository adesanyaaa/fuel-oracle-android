package org.biu.ufo.activities;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.events.EngineSpeedMessage;
import org.biu.ufo.events.FuelConsumedMessage;
import org.biu.ufo.events.FuelLevelMessage;
import org.biu.ufo.events.LatitudeMessage;
import org.biu.ufo.events.LongitudeMessage;
import org.biu.ufo.events.VehicleSpeedMessage;

import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.fragment_car_data)
public class FragmentCarData extends Fragment {
	@Bean
	OttoBus bus;

	@ViewById(R.id.main_message_table)
	LinearLayout fuelLevelLayout;

	@ViewById(R.id.main_msg)
	TextView fuelLevelMainMessage;

	@ViewById(R.id.sub_msg1)
	TextView fuelLevelsubMessage1;

	@ViewById(R.id.sub_msg2)
	TextView fuelLevelsubMessage2;

	@ViewById(R.id.speed)
	TextView speedCaption;

	@ViewById(R.id.fuel_consumed)
	TextView fuelConsumedCaption;

	@ViewById(R.id.drive_cost)
	TextView driveCostCaption;

	@ViewById(R.id.latitude)
	TextView latitudeCaption;

	@ViewById(R.id.longitude)
	TextView longtidueCaption;

	@ViewById(R.id.engine_speed)
	TextView engineSpeedCaption;

	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		bus.unregister(this);
	}
	
	@UiThread
	@Subscribe
	public void onFuelLevelUpdate(FuelLevelMessage message){
		fuelLevelLayout.setBackgroundResource(message.background);
		fuelLevelMainMessage.setText(message.mainMessage);
		fuelLevelsubMessage1.setText(message.subMessage_1);
		fuelLevelsubMessage2.setText(message.subMessage_2);
	}

	@UiThread
	@Subscribe
	public void onLongitudeUpdate(LongitudeMessage message){
		longtidueCaption.setText(message.longitude);
	}

	@UiThread
	@Subscribe
	public void onLatitudeUpdate(LatitudeMessage message){
		latitudeCaption.setText(message.latitude);
	}

	@UiThread
	@Subscribe
	public void onEngineSpeedUpdate(EngineSpeedMessage message) {
		engineSpeedCaption.setText(message.engineSpeed);
	}

	@UiThread
	@Subscribe
	public void onFuelConsumptionUpdate(FuelConsumedMessage message) {
		fuelConsumedCaption.setText(message.fuelConsumed);
		driveCostCaption.setText(message.driveCost);
	}

	@UiThread
	@Subscribe
	public void onVehicleSpeedUpdate(VehicleSpeedMessage message) {
		speedCaption.setText(message.vehicleSpeed);
	}

}
