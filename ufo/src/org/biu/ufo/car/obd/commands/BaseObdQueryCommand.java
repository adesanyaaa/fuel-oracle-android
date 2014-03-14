package org.biu.ufo.car.obd.commands;

import java.util.ArrayList;

import com.google.common.primitives.Bytes;

public abstract class BaseObdQueryCommand implements IObdCommand {
	protected static final String NODATA = "NODATA";  
	protected byte[] data;
	protected String result;

	@Override
	public boolean handleResult(String result) {
		this.result = result;

		ArrayList<Byte> raw = new ArrayList<Byte>();
		for(String singleByte : result.split(" ")) {
			try {
				raw.add(Byte.decode("0x" + singleByte));
			} catch(NumberFormatException e) {
			}
		}
		this.data = Bytes.toArray(raw);
		performCalculations();
		return true;
	}

	protected abstract void performCalculations();

}
