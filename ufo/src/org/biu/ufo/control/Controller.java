package org.biu.ufo.control;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.analyzers.FuelAnalyzer;
import org.biu.ufo.control.analyzers.FuelRecommendator;
import org.biu.ufo.control.analyzers.RouteAnalyzer;
import org.biu.ufo.control.analyzers.RouteEstimator;

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
		
	public void init(){
		routeAnalyzer.start();
		routeEstimator.start();
		fuelAnalyzer.start();
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
