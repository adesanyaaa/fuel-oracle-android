package org.biu.ufo.events.car.raw;

import com.openxc.measurements.EngineSpeed;

public class EngineSpeedMessage {
	private int speed;
	
	public EngineSpeedMessage(EngineSpeed engineSpeed){
		this.setSpeed(engineSpeed.getValue().intValue());
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}
}
