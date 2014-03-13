package org.biu.ufo.control.events.connection;

public class ObdDeviceAddressChanged {
	String address;
	
	public ObdDeviceAddressChanged(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
}
