package org.biu.ufo.control.events.raw;

import org.biu.ufo.ui.Formats;

import com.openxc.measurements.Longitude;

public class LongitudeMessage {
	public String longitudeMessage;
	public double longitude;
	
	public LongitudeMessage(Longitude longitude){
		this.longitudeMessage = Formats.cordsformatter.format(longitude.getValue().doubleValue());
		this.longitude = longitude.getValue().doubleValue();
	}
}
