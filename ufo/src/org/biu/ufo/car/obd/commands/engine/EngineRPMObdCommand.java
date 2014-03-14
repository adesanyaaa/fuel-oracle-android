package org.biu.ufo.car.obd.commands.engine;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

public class EngineRPMObdCommand extends BaseObdQueryCommand {
	public static String CMD = "01 0C";
	
	private int rpm = -1;
	
	public int getRPM() {
		return rpm;
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return CMD;
	}

	@Override
	protected void performCalculations() {
		if (data.length >= 4) {
			// ignore first two bytes [41 0C] of the response
			rpm = (data[2] * 256 + data[3]) / 4;
		}
	}

	/**
	 * @return the engine RPM per minute
	 */
	@Override
	public String getFormattedResult() {
		return String.format("%d%s", rpm, " RPM");
	}

	@Override
	public String getName() {
		return AvailableCommandNames.ENGINE_RPM.getValue();
	}

}
