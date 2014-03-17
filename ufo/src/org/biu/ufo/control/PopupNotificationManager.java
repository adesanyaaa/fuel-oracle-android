package org.biu.ufo.control;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.ui.activities.PopupActivity_;
import org.biu.ufo.ui.activities.PopupActivity_.IntentBuilder_;

import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Subscribe;

@EBean
public class PopupNotificationManager {
	private static final double START_SPEED = 100;
	
	double currentSpeed;
	FuelRecommendationMessage recommendation;
	
	@Bean
	OttoBus bus;

	@RootContext
	Context context;
	
	
	public void start() {
		currentSpeed = START_SPEED;
		recommendation = null;
		bus.register(this);
	}
	
	public void stop() {
		bus.unregister(this);
	}
	
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		recommendation = message;
		showPopupIfNeededAndPossible();
	}

	@Subscribe
	public void onVehicleSpeedMessage(VehicleSpeedMessage message) {
		currentSpeed = message.getSpeed();
		showPopupIfNeededAndPossible();			
	}
	
	private void showPopupIfNeededAndPossible() {
		if(recommendation != null && recommendation.shouldFuel() && recommendation.getTopStation() != null
				&& currentSpeed < 50.0) {
			recommendation = null;
			IntentBuilder_ builder = PopupActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_NEW_TASK);
			builder.get().putExtra("type", "fuel_next");
			builder.start();
		}
	}

}
