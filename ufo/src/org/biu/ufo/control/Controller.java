package org.biu.ufo.control;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.monitors.DrivingStatusMonitor;
import org.biu.ufo.control.monitors.FuelMonitor;
import org.biu.ufo.control.monitors.Recommendator;
import org.biu.ufo.control.monitors.TripMonitor;

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
	

	public void init(){
		drivingStatusMonitor.start();
		fuelMonitor.start();
		routeMonitor.start();
		recommendator.start(routeMonitor);
	}
	
	public void close(){		
		drivingStatusMonitor.stop();
		fuelMonitor.stop();
		routeMonitor.stop();
		recommendator.stop();
	}
}
