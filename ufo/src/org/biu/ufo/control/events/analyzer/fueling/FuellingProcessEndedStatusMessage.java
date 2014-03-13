package org.biu.ufo.control.events.analyzer.fueling;

public class FuellingProcessEndedStatusMessage extends FuellingProcessStatusMessage{

	public FuellingProcessEndedStatusMessage(){
		this.time = System.currentTimeMillis();
		this.message = "Fuelling Process has ended";
		
	}
}
