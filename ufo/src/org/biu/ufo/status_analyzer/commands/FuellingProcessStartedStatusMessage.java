package org.biu.ufo.status_analyzer.commands;

public class FuellingProcessStartedStatusMessage extends FuellingProcessStatusMessage{

	public FuellingProcessStartedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process NOW";
		
	}
}
