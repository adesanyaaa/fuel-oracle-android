package org.biu.ufo.control.analyzers;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.events.analyzer.routemonitor.EndOfRouteStatusMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStatusMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.StartOfRouteStatusMessage;
import org.biu.ufo.control.events.raw.EngineSpeedMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.model.Location;

import android.os.Handler;
import android.os.Message;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * RouteAnalyzer
 * according to messages received from the car\simulator determines:
 * 	1.the starting and ending coordinates 
 *  2.the entire route
 * notifies via bus
 * @author Danny Karmon
 *
 */
@EBean
public class RouteAnalyzer {

	public static final double MIN_DISTANCE_KM = 0.02;			//minimum distance (in km) to count as movement
	public static final long MAX_STATIC_DURATION_SECONDS = 5;	//maximum time (in sec) to be standing still in the same position
	public static final long MAX_STATIC_DURATION = MAX_STATIC_DURATION_SECONDS*1000;
	public static final int MIN_ENGINE_SPEED = 7;			//below car is off, maybe used for other stuff such as AC
	public static final double MIN_VEHICLE_SPEED = 2;		//below car is standing still otherwise, on the move

	@Bean
	OttoBus bus;

	Location refLocation;
	Location currentLocation;
	double vehicleSpeed = 0;
	int engineSpeed = 0;

	RouteStatusMessage driveRoute;

	boolean driveStarted = false;
	boolean firstTimeInit = true;

	Handler durationHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {

			//naive check that the car is off
			if (engineSpeed<MIN_ENGINE_SPEED && vehicleSpeed <MIN_VEHICLE_SPEED) {
				driveRoute.setEndTime(System.currentTimeMillis());
				bus.post(new EndOfRouteStatusMessage(driveRoute.getEndLocation()));
				bus.post(driveRoute);
				firstTimeInit = true;
				driveStarted = false;
				durationHandler.removeMessages(1);
			} else {
				restartTimer();
			}
		}

	};


	@Subscribe
	public void onLocationUpdate(LocationMessage message){
		currentLocation = new Location(message.location);
		
		if (firstTimeInit){
			firstTimeInit = false;
			
			refLocation = message.location;
			driveRoute = new RouteStatusMessage();
			driveRoute.setStartTime(System.currentTimeMillis());
			driveRoute.getRoute().add(new Location(refLocation));

		}else{
			distanceCheck();
		}
	}

	@Subscribe
	public void onVehicleSpeedUpdate(VehicleSpeedMessage message){
		if (!firstTimeInit){
			vehicleSpeed = message.speed;
			distanceCheck();
		}

	}

	@Subscribe
	public void onEngineSpeedUpdate(EngineSpeedMessage message){
		if (!firstTimeInit){
			engineSpeed = message.speed;
			distanceCheck();
		}

	}

	private void distanceCheck(){
		if (Calculator.distance(currentLocation, refLocation) > MIN_DISTANCE_KM) {
			
			if (!driveStarted){
				driveStarted = true;
				bus.post(new StartOfRouteStatusMessage(driveRoute.getStartLocation()));
			}

			refLocation = new Location(currentLocation);
			driveRoute.getRoute().add(refLocation);
			restartTimer();

		}
	}

	@Produce
	public StartOfRouteStatusMessage produceStartOfRouteStatusMessage() {
		if(driveStarted) {
			return new StartOfRouteStatusMessage(driveRoute.getStartLocation());
		}
		return null;
	}
	
	@Produce
	public EndOfRouteStatusMessage produceEndOfRouteStatusMessage() {
		if(!driveStarted && driveRoute != null && driveRoute.isEnded()) {
			return new EndOfRouteStatusMessage(driveRoute.getEndLocation());
		}
		return null;
	}

	/**
	 * for ending the route.
	 * if not called within MAX_STATIC_DURATION - the process ended
	 */
	private void restartTimer() {
		durationHandler.removeMessages(1);
		durationHandler.sendEmptyMessageDelayed(1, MAX_STATIC_DURATION);
	}

}
