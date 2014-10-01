package org.biu.ufo.tracker;

import java.util.Map;

public class TrackerEvent {
	private Map<String, String> data;

	public TrackerEvent(Map<String, String> data) {
		this.data = data;
	}
	
	public Map<String, String> getData() {
		return data;
	}
}
