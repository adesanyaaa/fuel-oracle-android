package org.biu.ufo.messages;

import com.openxc.measurements.EngineSpeed;

public class EngineSpeedMessage {

	public String engineSpeed;
	
	public EngineSpeedMessage(EngineSpeed engineSpeed){
		this.engineSpeed = String.valueOf(engineSpeed.getValue().intValue());
	}
}
