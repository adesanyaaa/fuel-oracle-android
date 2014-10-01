package org.biu.ufo.tracker;

public class DestinationSelectedEvent {
	private int pos;

	public DestinationSelectedEvent(int pos) {
		this.pos = pos;
	}
	
	public int getPos() {
		return pos;
	}
}
