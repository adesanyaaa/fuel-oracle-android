package org.biu.ufo.control.events.raw;

import com.openxc.measurements.FuelConsumed;

public class FuelConsumedMessage {
	public static double GAS_LITER_RATE = 3.21;

	public double fuelConsumed;	// in liters
	
	public FuelConsumedMessage(FuelConsumed fuelConsumed){
		this.fuelConsumed = fuelConsumed.getValue().doubleValue();
	}
}
