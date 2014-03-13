package org.biu.ufo.control.events.analyzer.fueling;

public class FuellingProcessStartedStatusMessage extends FuellingProcessStatusMessage{

	public FuellingProcessStartedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process NOW";
		
	}
}
