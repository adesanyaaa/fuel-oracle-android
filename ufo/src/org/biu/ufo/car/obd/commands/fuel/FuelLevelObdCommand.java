package org.biu.ufo.car.obd.commands.fuel;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

import android.annotation.SuppressLint;

public class FuelLevelObdCommand extends BaseObdQueryCommand {
	public static String CMD = "01 2F";

	private float fuelLevel = 0f;

	@Override
	public String getCommand() {
		return CMD;
	}

	public float getValue() {
		return fuelLevel;
	}

	@Override
	@SuppressLint("DefaultLocale")
	public String getFormattedResult() {
		return String.format("%.1f%s", getValue(), "%");
	}

	@Override
	protected void performCalculations() {
		// ignore first two bytes [hh hh] of the response
		if(data.length >= 3) {
			fuelLevel = 100.0f * data[2] / 255.0f;
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return AvailableCommandNames.FUEL_LEVEL.getValue();
	}

}
