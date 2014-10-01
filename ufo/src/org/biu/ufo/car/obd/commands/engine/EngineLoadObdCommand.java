package org.biu.ufo.car.obd.commands.engine;

import org.biu.ufo.car.obd.commands.PrecentageObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

public class EngineLoadObdCommand extends PrecentageObdQueryCommand {
	public static String CMD = "01 04";

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return CMD;
	}

	@Override
	public String getName() {
	    return AvailableCommandNames.ENGINE_LOAD.getValue();
	}

}
