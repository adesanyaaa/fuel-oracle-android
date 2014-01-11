package org.biu.ufo.events;

import org.biu.ufo.R;

import com.openxc.measurements.FuelLevel;

public class FuelLevelMessage {
	private static final int LOW_TANK_LIMIT = 15;
	private static final int FUEL_NEXT_LIMIT = 40;
	private static final String MAIN_MSG_FUEL_NEXT = "FUEL NEXT";
	private static final String MAIN_MSG_FUEL_NEXT_SUB_MSG = "fuel left";
	private static final String MAIN_MSG_LOW_TANK = "LOW TANK";
	private static final String MAIN_MSG_LOW_TANK_SUB_MSG = "FUEL NOW!";
	private static final String DEFAULT_MSG = "Fuel tank level"; 

	public int background;
	public int icon = R.drawable.gasstation;
	public String mainMessage;
	public String subMessage_1;
	public String subMessage_2;

	public FuelLevelMessage(FuelLevel fuelLevel){
		double fuelLevelValue = fuelLevel.getValue().doubleValue();

		if(fuelLevelValue < LOW_TANK_LIMIT) {
			this.background =R.color.danger;
			this.mainMessage = MAIN_MSG_LOW_TANK;
			this.subMessage_1 = String.valueOf(fuelLevel);
			this.subMessage_2 = MAIN_MSG_LOW_TANK_SUB_MSG;

		} else if(fuelLevelValue < FUEL_NEXT_LIMIT) {
			this.background =R.color.fuel_next;
			this.mainMessage = MAIN_MSG_FUEL_NEXT;
			this.subMessage_1 = String.valueOf(fuelLevel);
			this.subMessage_2 = MAIN_MSG_FUEL_NEXT_SUB_MSG;
		} else {
			this.background = R.color.default_tile;
			this.mainMessage = String.valueOf(fuelLevel);
			this.subMessage_1 = DEFAULT_MSG;
			this.subMessage_2 = "";

		}
	}
}
