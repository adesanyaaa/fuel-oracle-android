package org.biu.ufo.messages;

import org.biu.ufo.Formats;

import com.openxc.measurements.Longitude;

public class LongitudeMessage {
	public String longitude;
	
	public LongitudeMessage(Longitude longitude){
		this.longitude = Formats.cordsformatter.format(longitude.getValue().doubleValue());
	}
}
