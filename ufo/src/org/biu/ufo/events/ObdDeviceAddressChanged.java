package org.biu.ufo.events;

public class ObdDeviceAddressChanged {
	String address;
	
	public ObdDeviceAddressChanged(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
}
