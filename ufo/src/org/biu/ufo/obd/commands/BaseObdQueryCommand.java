package org.biu.ufo.obd.commands;

import java.util.ArrayList;

import com.google.common.primitives.Bytes;

public abstract class BaseObdQueryCommand implements IObdCommand {
	protected byte[] data;
	protected String result;
	
	@Override
	public boolean handleResult(String result) {
		this.result = result;
		
		try {
			ArrayList<Byte> raw = new ArrayList<Byte>();
			for(String singleByte : result.split(" ")) {
				raw.add(Byte.decode("0x" + singleByte));
			}
			this.data = Bytes.toArray(raw);
			performCalculations();
			return true;
		} catch(NumberFormatException e) {
			
		}
		return false;
	}
	
	protected abstract void performCalculations();

}
