package org.biu.ufo.events.control;

import org.biu.ufo.control.utils.AverageValue;

public class TripCompleted {

	private AverageValue avgSpeed;
	private AverageValue avgRPM;
	
	public TripCompleted(AverageValue avgSpeed, AverageValue avgRPM) {
		this.avgSpeed = avgSpeed;
		this.avgRPM = avgRPM;
	}

	public AverageValue getAvgSpeed() {
		return avgSpeed;
	}


	public AverageValue getAvgRPM() {
		return avgRPM;
	}

}
