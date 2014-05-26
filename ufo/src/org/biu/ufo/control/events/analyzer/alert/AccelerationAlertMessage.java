package org.biu.ufo.control.events.analyzer.alert;

import org.biu.ufo.control.events.notification.INotification;

public class AccelerationAlertMessage implements INotification{
	
	int engineSpeed;
		
	public int getEngineSpeed() {
		return engineSpeed;
	}
	
	public AccelerationAlertMessage(int engineSpeed){
		this.engineSpeed = engineSpeed;
	}
	
}
