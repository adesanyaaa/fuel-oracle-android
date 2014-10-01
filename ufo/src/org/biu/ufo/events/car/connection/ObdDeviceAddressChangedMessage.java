package org.biu.ufo.events.car.connection;

public class ObdDeviceAddressChangedMessage {
	String address;
	
	public ObdDeviceAddressChangedMessage(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
}
