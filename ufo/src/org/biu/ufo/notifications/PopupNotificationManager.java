package org.biu.ufo.notifications;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;
import org.biu.ufo.events.car.raw.LocationMessage;
import org.biu.ufo.events.car.raw.VehicleSpeedMessage;
import org.biu.ufo.events.control.FuelRecommendationMessage;
import org.biu.ufo.events.control.HighAccelerationAlertMessage;

import android.view.View;

import com.squareup.otto.Subscribe;

@EBean
public class PopupNotificationManager {
    @App
    MainApplication application;

	@Bean
	OttoBus bus;

	private HighAccelerationAlertNotification accelerationNotification;
	private FuelNextNotification fuelNextNotification;

	public void start() {
		fuelNextNotification = new FuelNextNotification(application, bus);
		accelerationNotification = new HighAccelerationAlertNotification(application);
		bus.register(this);
	}

	public void stop() {
		bus.unregister(this);
		
		accelerationNotification.closePopup();
		accelerationNotification = null;
		
		fuelNextNotification.closePopup();
		fuelNextNotification = null;
	}
	
	@Subscribe
	public void onHighAccelerationAlertMessage(HighAccelerationAlertMessage message) {
		if(!fuelNextNotification.isPopupShown() && !accelerationNotification.isPopupShown()) {
			accelerationNotification.showPopup();			
		}
	}

	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		fuelNextNotification.onFuelRecommendationMessage(message);
		fuelNextNotification.showPopupIfNeededAndPossible();
	}

	@Subscribe
	public void onLocationMessage(LocationMessage message) {
		fuelNextNotification.onLocationMessage(message);
		fuelNextNotification.showPopupIfNeededAndPossible();
	}

	@Subscribe
	public void onVehicleSpeedMessage(VehicleSpeedMessage message) {
		fuelNextNotification.onVehicleSpeedMessage(message);
		fuelNextNotification.showPopupIfNeededAndPossible();
	}
	
	public View createView(int id) {
		if(fuelNextNotification.getPopupId() == id) {
			return fuelNextNotification.createView();
		} else if(accelerationNotification.getPopupId() == id) {
			return accelerationNotification.createView();
		}
		return null;
	}
	
	public void onPopupClick(int id) {
		if(fuelNextNotification.getPopupId() == id) {
			fuelNextNotification.onPopupClick();
		} else if(accelerationNotification.getPopupId() == id) {
			accelerationNotification.onPopupClick();
		}
	}
	
	public void onPopupClose(int id) {
		if(fuelNextNotification.getPopupId() == id) {
			fuelNextNotification.closePopup();
		} else if(accelerationNotification.getPopupId() == id) {
			accelerationNotification.closePopup();
		}
	}

	public void onShown(int id) {
		if(fuelNextNotification.getPopupId() == id) {
			fuelNextNotification.onShown();
		} else if(accelerationNotification.getPopupId() == id) {
			accelerationNotification.onShown();
		}		
	}

}
