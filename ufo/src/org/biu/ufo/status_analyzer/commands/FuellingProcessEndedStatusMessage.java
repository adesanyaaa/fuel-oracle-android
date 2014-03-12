package org.biu.ufo.status_analyzer.commands;

public class FuellingProcessEndedStatusMessage extends FuellingProcessStatusMessage{

	public FuellingProcessEndedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process has ended";
		
	}
}
