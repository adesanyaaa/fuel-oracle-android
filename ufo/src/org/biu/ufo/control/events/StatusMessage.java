package org.biu.ufo.control.events;

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
