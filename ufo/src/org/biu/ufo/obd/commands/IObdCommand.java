package org.biu.ufo.obd.commands;

public interface IObdCommand {

	public String getCommand();

	public boolean handleResult(String result);

	public String getFormattedResult();

	public String getName();

}
