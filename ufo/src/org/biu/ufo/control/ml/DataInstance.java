package org.biu.ufo.control.ml;

import java.util.ArrayList;

public class DataInstance {
	ArrayList<Double> attributes;
	Object target;
		
	public DataInstance(ArrayList<Double> attributes, Object targetValue){
		this.attributes = attributes;
		this.target = targetValue;
	}
	
}
