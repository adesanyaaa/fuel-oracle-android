package org.biu.ufo.control.events.analyzer.fueling;

public class FuelLossProcessStartedStatusMessage extends FuellingProcessStatusMessage{

	public FuelLossProcessStartedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process Loss NOW";
		
	}
}
