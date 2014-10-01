package org.biu.ufo.control.monitors;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.events.car.raw.FuelLevelMessage;
import org.biu.ufo.events.car.raw.LocationMessage;
import org.biu.ufo.events.control.FuelProcessMessage;
import org.biu.ufo.model.Location;

import android.os.Handler;

import com.squareup.otto.Subscribe;

/**
 * FuelMonitor
 * Notifies on fueling process start and completion.
 */
@EBean
public class FuelMonitor {
	public static float MIN_PERCENTAGE_CHANGE = 5;	// Minimum change in fuel status
	public static long PROCESS_COMPLETED_CHECK_TIME = 1*30*1000;
	
	@Bean
	OttoBus bus;
    private Handler handler = new Handler();
	private double currentFuelLevel;
	private long lastChangeTime;
	private FuelProcessMessage detailsMessage;
	private Location currentLocation;

	public void start() {
		lastChangeTime = 0;
		detailsMessage = null;
		bus.register(this);
	}

	public void stop() {
		handler.removeCallbacks(processCompletedCheck);
		bus.unregister(this);		
	}
	
	@Subscribe 
	public void onLocationMessage(LocationMessage locationMessage) {
		currentLocation = locationMessage.getLocation();

		if(detailsMessage != null && detailsMessage.getLocation() == null) {
			detailsMessage.setLocation(currentLocation);
			if(detailsMessage.isCompleted()) {
				bus.post(detailsMessage);
				detailsMessage = null;
			}
		}
	}
	
	@Subscribe
	public void onFuelLevelUpdate(FuelLevelMessage message){
		double fuelLevel = message.getFuelLevelValue();
		
		if(fuelLevel > currentFuelLevel && currentFuelLevel != 0) {
			if(detailsMessage == null) {
				detailsMessage = new FuelProcessMessage(currentFuelLevel);
				//bus.post(detailsMessage);
				scheduleProcessCompletedCheck(PROCESS_COMPLETED_CHECK_TIME);
			}
			
			lastChangeTime = System.currentTimeMillis();
		}
		
		currentFuelLevel = fuelLevel;
	}

	protected void processCompleted() {
		detailsMessage.setEndFuelLevel(currentFuelLevel);
		
		if(detailsMessage.getLocation() != null) {
			bus.post(detailsMessage);
			detailsMessage = null;
		}
	}

	private void scheduleProcessCompletedCheck(long delay) {
        handler.postDelayed(processCompletedCheck, delay);
	}

	private Runnable processCompletedCheck = new Runnable() {
        @Override
        public void run() {
        	long passed = System.currentTimeMillis() - lastChangeTime;
        	if(passed > PROCESS_COMPLETED_CHECK_TIME) {
        		processCompleted();
        	} else {
            	scheduleProcessCompletedCheck(PROCESS_COMPLETED_CHECK_TIME - passed);        		
        	}
        }
    };

}
