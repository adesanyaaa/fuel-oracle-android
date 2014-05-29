package org.biu.ufo.control.monitors;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.events.car.raw.EngineSpeedMessage;
import org.biu.ufo.events.car.raw.LocationMessage;
import org.biu.ufo.events.car.raw.VehicleSpeedMessage;
import org.biu.ufo.events.control.TripStart;
import org.biu.ufo.events.control.TripStop;
import org.biu.ufo.model.Location;

import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * DrivingStatusMonitor
 * 
 * Notifies driving status
 * - Driving
 * - Waiting (e.g. on red light) 
 * - Off (engine not started)
 * - Offline (no messages received)
 */
@EBean
public class DrivingStatusMonitor {
	public static final int OFF_ENGINE_SPEED = 100;	// below this value assume car is off
	public static final int WAITING_VEHICLE_SPEED = 2;	// below this value assume car is standing
	public static final long TIME_UNTIL_CONSIDERED_OFFLINE = 1*60*1000;	// If no messages received for this long assume offline

	public enum DrivingStatus {DRIVING, WAITING, OFF, OFFLINE};
	
	@Bean
	OttoBus bus;
    private Handler handler = new Handler();
    private DrivingStatus currentDrivingStatus;
	private double currentSpeed;
	private int currentEngineRPM;
	private Location currentLocation;
	private long lastMessageTime;

	public void start() {
		currentDrivingStatus = DrivingStatus.OFFLINE;	// but we don't publish it at startup
		currentSpeed = 0;
		currentEngineRPM = 0;		
		currentLocation = null;
		scheduleOfflineCheck(TIME_UNTIL_CONSIDERED_OFFLINE);
		
		bus.register(this);
	}

	public void stop() {
		handler.removeCallbacks(offlineCheck);		
		bus.unregister(this);
	}
	
	@Subscribe
	public void onLocationMessage(LocationMessage message) {
		currentLocation = message.getLocation();
		checkDrivingStatus();
	}
	
	@Subscribe
	public void onVehicleSpeedUpdate(VehicleSpeedMessage message) {
		currentSpeed = message.getSpeed();
		checkDrivingStatus();
	}

	@Subscribe
	public void onEngineSpeedUpdate(EngineSpeedMessage message) {
		currentEngineRPM = message.getSpeed();
		lastMessageTime = System.currentTimeMillis();
		checkDrivingStatus();
	}
	
	@Produce
	public DrivingStatus produceDrivingStatus() {
		return currentDrivingStatus;
	}
	
	private void checkDrivingStatus() {
		DrivingStatus prev = currentDrivingStatus;
		DrivingStatus status = getDrivingStatus();
		if(status != prev) {
			currentDrivingStatus = status;

			if(prev == DrivingStatus.OFFLINE) {
				Log.e("TEST", "TripStart " + status.toString());
				bus.post(new TripStart());
			} else if(status == DrivingStatus.OFFLINE) {
				Log.e("TEST",  "TripStop " + status.toString());
				bus.post(new TripStop());
			}			
			bus.post(status);
		}
	}
	
	private DrivingStatus getDrivingStatus() {
		if(currentLocation == null)
			return DrivingStatus.OFFLINE;
		
    	long passed = System.currentTimeMillis() - lastMessageTime;
    	if(passed > TIME_UNTIL_CONSIDERED_OFFLINE) {
    		return DrivingStatus.OFFLINE;
    	}
    	
		if(currentEngineRPM <= OFF_ENGINE_SPEED && currentSpeed <= WAITING_VEHICLE_SPEED) {
			return DrivingStatus.OFF;
		}
		
		if(currentSpeed <= WAITING_VEHICLE_SPEED) {
			return DrivingStatus.WAITING;
		}
		
		return DrivingStatus.DRIVING;
	}

	private void scheduleOfflineCheck(long delay) {
        handler.postDelayed(offlineCheck, delay);
	}
	
	private Runnable offlineCheck = new Runnable() {
        @Override
        public void run() {
        	long passed = System.currentTimeMillis() - lastMessageTime;
        	if(passed > TIME_UNTIL_CONSIDERED_OFFLINE) {
            	checkDrivingStatus();
            	scheduleOfflineCheck(TIME_UNTIL_CONSIDERED_OFFLINE);
        	} else {
            	scheduleOfflineCheck(TIME_UNTIL_CONSIDERED_OFFLINE - passed);
        	}
        }
    };
}
