package org.biu.ufo.control.analyzers;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.Controller;
import org.biu.ufo.control.events.analyzer.alert.AccelerationAlertMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStopMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteSummaryMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStartMessage;
import org.biu.ufo.control.events.raw.EngineSpeedMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.events.raw.PickLocationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.model.Feedback;
import org.biu.ufo.model.Location;
import org.biu.ufo.storage.RouteDataStore;

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
public class RouteAnalyzer implements IAnalyzer {

	public static final double MIN_DISTANCE_KM = 0.02;			//minimum distance (in km) to count as movement
	public static final long MAX_STATIC_DURATION_SECONDS = 5;	//maximum time (in sec) to be standing still in the same position
	public static final long MAX_STATIC_DURATION = MAX_STATIC_DURATION_SECONDS*1000;
	public static final int MIN_ENGINE_SPEED = 7;			//below car is off, maybe used for other stuff such as AC
	public static final double MIN_VEHICLE_SPEED = 2;		//below car is standing still otherwise, on the move
	
	public static final double MIN_FAST_VEHICLE_SPEED = 7; 
	public static final double MIN_MEDIUM_VEHICLE_SPEED = 5;
	public static final double MIN_SLOW_VEHICLE_SPEED = 3;
	
	public static final double ACCELERARION_ALERT_THRESHOLD = 1800; //in rpm
	
	
	@Bean
	OttoBus bus;
	Controller controller;
	
	//@Bean
	//RouteDataStore routeDataStore;
	
	Location refLocation;
	Location currentLocation;
	double vehicleSpeed = 0;
	int engineSpeed = 0;
	
	
	// baruch add
	VehicleSpeedCategory retSpeed;
	double engine_countSpeedChanges = 0;
	double engine_sumSpeed = 0;
	double vehicle_sumSpeed = 0;
	double vehicle_avgSpeed = 0;
	double vehicle_countSpeedChanges = 0;
	// baruch add	
	
	RouteSummaryMessage driveRoute;

	boolean driveStarted = false;
	boolean firstTimeInit = true;
	
	Handler durationHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
	
			//naive check that the car is off
			if (engineSpeed<MIN_ENGINE_SPEED && vehicleSpeed <MIN_VEHICLE_SPEED) {
				driveRoute.setEndTime(System.currentTimeMillis());
				//routeDataStore.addLocation(driveRoute.getEndLocation(), true);
				bus.post(new RouteStopMessage(driveRoute.getEndLocation()));
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
			driveRoute = new RouteSummaryMessage();
			driveRoute.setStartTime(System.currentTimeMillis());
			driveRoute.getRoute().add(new Location(refLocation));

		}else{
			distanceCheck();
			setSpeedBehavior();
		}
	}

	@Subscribe
	public void onVehicleSpeedUpdate(VehicleSpeedMessage message){
		
		if (!firstTimeInit){
			++vehicle_countSpeedChanges;
			vehicleSpeed = message.speed;
			
			vehicle_sumSpeed +=vehicleSpeed;
			vehicle_avgSpeed = vehicle_sumSpeed/vehicle_countSpeedChanges;
			driveRoute.addVehicleSpeedInfo(vehicleSpeed,1);
			distanceCheck();
			
		}else{bus.post(new TestMessage());}
	}

	@Subscribe
	public void onEngineSpeedUpdate(EngineSpeedMessage message){
		if (!firstTimeInit){
			++engine_countSpeedChanges;
			engineSpeed = message.speed;
			
			engine_sumSpeed += engineSpeed;
			driveRoute.setAvgEngineSpeed(engine_sumSpeed/engine_countSpeedChanges);
			
			if (engineSpeed>ACCELERARION_ALERT_THRESHOLD){
				bus.post(new AccelerationAlertMessage(engineSpeed));
			}
			distanceCheck();
		}
	}
	
	
	@Produce
	public RouteStartMessage produceStartOfRouteStatusMessage() {
		if(driveStarted) {
			return new RouteStartMessage(driveRoute.getStartLocation());
		}
		return null;
	}
	
	
	@Produce
	public RouteStopMessage produceEndOfRouteStatusMessage() {
		if(!driveStarted && driveRoute != null && driveRoute.isEnded()) {
			return new RouteStopMessage(driveRoute.getEndLocation());
		}
		return null;
	}
	
	
	private void distanceCheck(){
		if (Calculator.distance(currentLocation, refLocation) > MIN_DISTANCE_KM) {
			
			if (!driveStarted){
				driveStarted = true;
				bus.post(new RouteStartMessage(driveRoute.getStartLocation()));
			}
			refLocation = new Location(currentLocation);
			driveRoute.getRoute().add(refLocation);
			bus.post(new PickLocationMessage(refLocation));
			restartTimer();
		}
	}

	private void setSpeedBehavior(){
		retSpeed = VehicleSpeedCategory.NOTMOVE;
		
		if (vehicle_avgSpeed >= MIN_FAST_VEHICLE_SPEED){
			retSpeed = VehicleSpeedCategory.FAST;
		}else if (vehicle_avgSpeed >= MIN_MEDIUM_VEHICLE_SPEED){
			retSpeed = VehicleSpeedCategory.MEDIUM;
		}else if ( vehicle_avgSpeed >= MIN_SLOW_VEHICLE_SPEED){
			retSpeed = VehicleSpeedCategory.SLOW;
		}
		vehicle_avgSpeed=0;
		vehicle_sumSpeed=0;
	}
	
	
	/**
	 * for ending the route.
	 * if not called within MAX_STATIC_DURATION - the process ended
	 */
	private void restartTimer() {
		durationHandler.removeMessages(1);
		durationHandler.sendEmptyMessageDelayed(1, MAX_STATIC_DURATION);
	}

	
	@Override
	public void start(){
		//routeDataStore.open();
		bus.register(this);
	}

	
	@Override
	public void stop() {
		//routeDataStore.close();
		bus.unregister(this);		
	}

	
	@Override
	public void setController(Controller controller){
		this.controller = controller;
	}

}
