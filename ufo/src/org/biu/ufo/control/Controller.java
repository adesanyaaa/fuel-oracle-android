package org.biu.ufo.control;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.monitors.DrivingStatusMonitor;
import org.biu.ufo.control.monitors.FuelMonitor;
import org.biu.ufo.control.monitors.Recommendator;
import org.biu.ufo.control.monitors.TripMonitor;
import org.biu.ufo.tracker.TrackerMonitor;

@EBean
public class Controller {
	
	@Bean
	OttoBus bus;
	
	@Bean
	DrivingStatusMonitor drivingStatusMonitor;
	
	@Bean
	FuelMonitor fuelMonitor; 
	
	@Bean
	TripMonitor routeMonitor;
	
	@Bean
	Recommendator recommendator;
	
	@Bean
	TrackerMonitor trackerMonitor;

	public void init(){
		trackerMonitor.start();
		drivingStatusMonitor.start();
		fuelMonitor.start();
		routeMonitor.start();
		recommendator.start(routeMonitor);
		
	}
	
	public void close(){
		trackerMonitor.stop();
		drivingStatusMonitor.stop();
		fuelMonitor.stop();
		routeMonitor.stop();
		recommendator.stop();
	}
}
