package org.biu.ufo.control;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.analyzers.FuelAnalyzer;
import org.biu.ufo.control.analyzers.FuelRecommendator;
import org.biu.ufo.control.analyzers.RouteAnalyzer;
import org.biu.ufo.control.analyzers.RouteEstimator;
import org.biu.ufo.storage.RouteDataStore;

@EBean
public class Controller {
	
	@Bean
	OttoBus bus;
	
	@Bean
	FuelAnalyzer fuelAnalyzer;
	
	@Bean
	RouteAnalyzer routeAnalyzer;
	
	@Bean
	FuelRecommendator fuelingRecommendator;
	
	@Bean
	RouteEstimator routeEstimator;
	
	@Bean
	RouteDataStore routeDataStore;

	public void init(){
		
		routeAnalyzer.setController(this);
		routeAnalyzer.start();
		
		routeEstimator.setController(this);
		routeEstimator.start();
		
		fuelAnalyzer.setController(this);
		fuelAnalyzer.start();
		
		fuelingRecommendator.setController(this);
		fuelingRecommendator.start();
	}
	
	public void close(){		
		fuelingRecommendator.stop();
		fuelAnalyzer.stop();
		routeEstimator.stop();
		routeAnalyzer.stop();
	}
	
	public RouteEstimator getRouteEstimator() {
		return routeEstimator;
	}
}
