package org.biu.ufo.car.obd.commands.engine;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

public class EngineRuntimeObdCommand extends BaseObdQueryCommand {
	public static String CMD = "01 1F";

	private int value = 0;

	public int getValue() {
		return value;
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return CMD;
	}

	@Override
	protected void performCalculations() {
		if (data.length >= 4) {
			// ignore first two bytes [01 0C] of the response
			value = data[2] * 256 + data[3];
		}
	}

	@Override
	public String getFormattedResult() {
	    final String hh = String.format("%02d", value / 3600);
	    final String mm = String.format("%02d", (value % 3600) / 60);
	    final String ss = String.format("%02d", value % 60);
	    return String.format("%s:%s:%s", hh, mm, ss);
	}

	@Override
	public String getName() {
		return AvailableCommandNames.ENGINE_RUNTIME.getValue();
	}

}
