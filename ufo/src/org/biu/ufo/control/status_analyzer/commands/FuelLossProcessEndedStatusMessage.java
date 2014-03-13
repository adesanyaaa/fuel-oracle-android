package org.biu.ufo.control.status_analyzer.commands;

public class FuelLossProcessEndedStatusMessage extends FuellingProcessStatusMessage{

	public FuelLossProcessEndedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process has ended";
		
	}
}
