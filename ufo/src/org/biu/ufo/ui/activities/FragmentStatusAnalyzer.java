package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.fueling.FuelProcessDetailsMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuellingProcessEndedStatusMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuellingProcessStartedStatusMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuellingProcessStatusMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.EndOfRouteStatusMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStatusMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.StartOfRouteStatusMessage;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.fragment_car_status)
public class FragmentStatusAnalyzer extends Fragment {
	
	@Bean
	OttoBus bus;
	
//	@Bean
//	Controller statusAnalyzer;
//	
	
	@ViewById(R.id.status_main_value)
	TextView fuelingValue;
	
	@ViewById(R.id.status_driving_value)
	TextView drivingValue;
	
	@ViewById(R.id.amount)
	TextView amount;
	
	@ViewById(R.id.duration)
	TextView duration;
	
	@ViewById(R.id.start_fuel_level)
	TextView startFuelLevel;
	
	@ViewById(R.id.end_fuel_level)
	TextView endFuelLevel;
	
	@ViewById(R.id.start_latitude)
	TextView startLatitude;
	
	@ViewById(R.id.start_longitude)
	TextView startLongitude;
	
	@ViewById(R.id.end_latitude)
	TextView endLatitude;
	
	@ViewById(R.id.end_longitude)
	TextView endLongitude;
	
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
	
	@Override
	public void onStart() {
		super.onStart();
//		statusAnalyzer.init();
	}

	@Override
	public void onDestroy(){
		super.onStop();
//		statusAnalyzer.close();
	}
	
	@UiThread
	@Subscribe
	public void onFuellingProcess(FuellingProcessStatusMessage message){
		if (message instanceof FuellingProcessStartedStatusMessage){ 
			fuelingValue.setText(R.string.status_yes_msg);
		}
		if (message instanceof FuellingProcessEndedStatusMessage){ 
			fuelingValue.setText(R.string.status_no_msg);
		} 
	}
	
	
	@UiThread
	@Subscribe
	public void onStartRoute(StartOfRouteStatusMessage message){
		drivingValue.setText(R.string.status_yes_msg);
		startLatitude.setText(String.valueOf(message.getLocation().getLatitude()));
		startLongitude.setText(String.valueOf(message.getLocation().getLongitude()));
	}
	
	@UiThread
	@Subscribe
	public void onEndRoute(EndOfRouteStatusMessage message){
		drivingValue.setText(R.string.status_no_msg);
		endLatitude.setText(String.valueOf(message.location.getLatitude()));
		endLongitude.setText(String.valueOf(message.location.getLongitude()));
		
	}
	
	@UiThread
	@Subscribe
	public void onRouteDetails(RouteStatusMessage message){
		endLatitude.setText(String.valueOf(message.getEndLocation().getLatitude()));
		endLongitude.setText(String.valueOf(message.getEndLocation().getLongitude()));
		
		startLatitude.setText(String.valueOf(message.getStartLocation().getLatitude()));
		startLongitude.setText(String.valueOf(message.getStartLocation().getLongitude()));
		
		
		duration.setText(String.valueOf(message.getDuration()));
	}
	
	
	@UiThread
	@Subscribe
	public void onFuel(FuelProcessDetailsMessage message){
		startFuelLevel.setText(String.valueOf(message.startFuelLevel));
		endFuelLevel.setText(String.valueOf(message.endFuelLevel));
		
		amount.setText(String.valueOf(message.amount));
	}
}
