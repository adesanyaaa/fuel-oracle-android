package org.biu.ufo.car.obd.commands.fuel;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

import android.annotation.SuppressLint;

public class FuelConsumptionRateObdCommand extends BaseObdQueryCommand {
	public static String CMD = "01 5E";

	private float fuelRate = -1.0f;	// liters per hour

	@Override
	public String getCommand() {
		return CMD;
	}

	public float getLitersPerHour() {
		return fuelRate;
	}

	@Override
	@SuppressLint("DefaultLocale")
	public String getFormattedResult() {
	    return String.format("%.1f%s", fuelRate, "");
	}

	@Override
	protected void performCalculations() {
		// ignore first two bytes [hh hh] of the response
		if(data.length >= 4) {
		      fuelRate = (data[2] * 256 + data[3]) * 0.05f;
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return AvailableCommandNames.FUEL_CONSUMPTION.getValue();
	}

}
