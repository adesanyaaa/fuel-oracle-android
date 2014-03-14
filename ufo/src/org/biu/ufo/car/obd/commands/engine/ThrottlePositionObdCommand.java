package org.biu.ufo.car.obd.commands.engine;

import org.biu.ufo.car.obd.commands.PrecentageObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

public class ThrottlePositionObdCommand extends PrecentageObdQueryCommand {
	public static String CMD = "01 11";

	@Override
	public String getCommand() {
		return CMD;
	}

	@Override
	public String getName() {
		return AvailableCommandNames.THROTTLE_POS.getValue();
	}

}
