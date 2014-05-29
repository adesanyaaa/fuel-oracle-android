package org.biu.ufo.car.obd.commands;

import java.util.ArrayList;

public abstract class BaseObdQueryCommand implements IObdCommand {
	protected static final String NODATA = "NODATA";  
	protected Integer[] data;
	protected String result;

	@Override
	public boolean handleResult(String result) {
		this.result = result;

		ArrayList<Integer> raw = new ArrayList<Integer>();
		for(String singleByte : result.split(" ")) {
			try {
				raw.add(Integer.decode("0x" + singleByte));
			} catch(NumberFormatException e) {
			}
		}
		this.data = raw.toArray(new Integer[0]);
		performCalculations();
		return true;
	}

	protected abstract void performCalculations();

}
