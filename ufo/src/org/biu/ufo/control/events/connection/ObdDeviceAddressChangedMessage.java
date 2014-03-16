package org.biu.ufo.control.events.connection;

public class ObdDeviceAddressChangedMessage {
	String address;
	
	public ObdDeviceAddressChangedMessage(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
}
