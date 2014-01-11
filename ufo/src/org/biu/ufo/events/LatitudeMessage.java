package org.biu.ufo.events;

import org.biu.ufo.Formats;

import com.openxc.measurements.Latitude;

public class LatitudeMessage {

	public String latitude;
	
	public LatitudeMessage(Latitude latitude){
		this.latitude = Formats.cordsformatter.format(latitude.getValue().doubleValue());
	}
}
