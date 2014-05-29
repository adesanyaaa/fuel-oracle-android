package org.biu.ufo.control.utils;

public class AverageValue {
	double sum;
	int count;
	
	public void add(double value) {
		sum += value;
		++count;
	}
	
	public double getAverage() {
		return sum/count;
	}
}
