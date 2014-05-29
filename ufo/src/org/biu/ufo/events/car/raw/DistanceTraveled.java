package org.biu.ufo.events.car.raw;

import com.openxc.measurements.Odometer;

public class DistanceTraveled {
	private double value;
	
	public DistanceTraveled(Odometer odometer) {
		value = odometer.getValue().doubleValue();
	}
	
	public double getValue() {
		return value;
	}
}
