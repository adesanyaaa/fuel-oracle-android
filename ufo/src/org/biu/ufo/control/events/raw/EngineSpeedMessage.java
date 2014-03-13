package org.biu.ufo.control.events.raw;

import com.openxc.measurements.EngineSpeed;

public class EngineSpeedMessage {

	public String engineSpeed;
	public int speed;
	
	public EngineSpeedMessage(EngineSpeed engineSpeed){
		this.engineSpeed = String.valueOf(engineSpeed.getValue().intValue());
		this.speed = engineSpeed.getValue().intValue();
	}
}
