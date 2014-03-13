package org.biu.ufo.car.obd.commands;

public abstract class BaseObdProtocolCommand implements IObdCommand {
	protected String result;

	@Override
	public boolean handleResult(String rawData) {
		result = rawData;
		return true;
	}

	@Override
	public String getFormattedResult() {
		return result;
	}

}
