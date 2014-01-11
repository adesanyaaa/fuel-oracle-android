package org.biu.ufo.events;

import com.openxc.measurements.EngineSpeed;

public class EngineSpeedMessage {

	public String engineSpeed;
	
	public EngineSpeedMessage(EngineSpeed engineSpeed){
		this.engineSpeed = String.valueOf(engineSpeed.getValue().intValue());
	}
}
