package org.biu.ufo.car.obd.commands.engine;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

public class MassAirFlowObdCommand extends BaseObdQueryCommand {
	public static String CMD = "01 10";

	private float maf = -1.0f;

	/**
	 * @return MAF value for further calculus.
	 */
	public double getMAF() {
		return maf;
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return CMD;
	}

	@Override
	protected void performCalculations() {
		if (data.length >= 4) {
			// ignore first two bytes [hh hh] of the response
			maf = (data[2] * 256 + data[3]) / 100.0f;
		}
	}

	@Override
	public String getFormattedResult() {
		return String.format("%.2f%s", maf, "g/s");
	}

	@Override
	public String getName() {
		return AvailableCommandNames.MAF.getValue();
	}

}
