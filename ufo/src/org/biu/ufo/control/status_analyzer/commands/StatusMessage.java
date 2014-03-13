package org.biu.ufo.control.status_analyzer.commands;

public abstract class StatusMessage {
	protected long time;
	protected String message;
	
	
	public String getMessage(){
		return message;
	}
	
	public long getTime(){
		return time;
	}
}
