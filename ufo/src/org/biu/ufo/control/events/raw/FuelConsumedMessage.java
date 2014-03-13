package org.biu.ufo.control.events.raw;

import org.biu.ufo.ui.Formats;

import com.openxc.measurements.FuelConsumed;

public class FuelConsumedMessage {
	public static double GAS_LITER_RATE = 3.21;

	public String fuelConsumed;
	public String driveCost;
	
	public FuelConsumedMessage(FuelConsumed fuelConsumed){
		this.fuelConsumed = Formats.formatter.format(fuelConsumed.getValue().doubleValue());
		this.driveCost = Formats.formatter.format(fuelConsumed.getValue().doubleValue()*GAS_LITER_RATE);
	}
}
