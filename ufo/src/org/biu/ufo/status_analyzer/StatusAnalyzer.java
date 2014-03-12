package org.biu.ufo.status_analyzer;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.biu.ufo.OttoBus;
import org.biu.ufo.status_analyzer.components.FuelAnalyzer;
import org.biu.ufo.status_analyzer.components.RouteAnalyzer;

@EBean
public class StatusAnalyzer {
	
	@Bean
	OttoBus bus;
	
	@Bean
	FuelAnalyzer fuelAnalyzer;
	
	@Bean
	RouteAnalyzer routeAnalyzer;
	
	@Pref
	StatusSharedPref_ statusPref;
	
	public void init(){
	
		bus.register(fuelAnalyzer);
		bus.register(routeAnalyzer);
	}
	
	public void close(){
		bus.unregister(routeAnalyzer);
		bus.register(fuelAnalyzer);
	}
	
	
	

}
