package org.biu.ufo.control;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.analyzers.FuelAnalyzer;
import org.biu.ufo.control.analyzers.FuelRecommendator;
import org.biu.ufo.control.analyzers.RouteAnalyzer;

@EBean(scope=Scope.Singleton)
public class Controller {
	
	@Bean
	OttoBus bus;
	
	@Bean
	FuelAnalyzer fuelAnalyzer;
	
	@Bean
	RouteAnalyzer routeAnalyzer;
	
	@Bean
	FuelRecommendator fuelingRecommendator;
		
	public void init(){
		bus.register(fuelAnalyzer);
		bus.register(routeAnalyzer);
		bus.register(fuelingRecommendator);
	}
	
	public void close(){
		bus.unregister(routeAnalyzer);
		bus.unregister(fuelAnalyzer);
		bus.unregister(fuelingRecommendator);
	}
	
}
