package org.biu.ufo.model;


public class DrivePoint {
	// Members
	private Location location;
	private String label = null;


	public DrivePoint() {
		this.location = new Location();
	}

	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}

	public String getLabel(){
		return this.label;
	}

	
	
	public void setLabel(String label){
		this.label = label;
	}
	
	
	
}
