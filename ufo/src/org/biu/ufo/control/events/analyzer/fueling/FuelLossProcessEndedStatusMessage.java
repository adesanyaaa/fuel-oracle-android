package org.biu.ufo.control.events.analyzer.fueling;

public class FuelLossProcessEndedStatusMessage extends FuellingProcessStatusMessage{

	public FuelLossProcessEndedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process has ended";
		
	}
}
