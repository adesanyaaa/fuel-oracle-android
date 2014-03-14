package org.biu.ufo.car.obd.commands.fuel;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.enums.FuelTrim;

import android.annotation.SuppressLint;

public class FuelTrimObdCommand extends BaseObdQueryCommand {
	public static String CMD = "01 2F";

	private float fuelTrimValue = 0.0f;
	private final FuelTrim bank;

	public FuelTrimObdCommand(final FuelTrim bank) {
		this.bank = bank;
	}

	@Override
	public String getCommand() {
		return bank.buildObdCommand();
	}

	public float getValue() {
		return fuelTrimValue;
	}
	
	/**
	 * @return the name of the bank in string representation.
	 */
	public final String getBank() {
		return bank.getBank();
	}

	@Override
	@SuppressLint("DefaultLocale")
	public String getFormattedResult() {
		return String.format("%.2f%s", fuelTrimValue, "%");
	}

	@Override
	protected void performCalculations() {
		// ignore first two bytes [hh hh] of the response
		if(data.length >= 3) {
			fuelTrimValue = prepareTempValue(data[2]);
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return bank.getBank();
	}
	
	private float prepareTempValue(final int value) {
		return new Double((value - 128) * (100.0 / 128)).floatValue();
	}

}
