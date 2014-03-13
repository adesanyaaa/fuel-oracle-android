package org.biu.ufo.control.status_analyzer.commands;

public class FuelLossProcessStartedStatusMessage extends FuellingProcessStatusMessage{

	public FuelLossProcessStartedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process Loss NOW";
		
	}
}
