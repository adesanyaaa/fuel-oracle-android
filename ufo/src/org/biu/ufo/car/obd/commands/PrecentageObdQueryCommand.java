package org.biu.ufo.car.obd.commands;


public abstract class PrecentageObdQueryCommand extends BaseObdQueryCommand {
	private float percentage = 0f;

	@Override
	protected void performCalculations() {
		if (data.length >= 3)
			// ignore first two bytes [hh hh] of the response
			percentage = (data[2] * 100.0f) / 255.0f;
	}

	@Override
	public String getFormattedResult() {
		return String.format("%.1f%s", percentage, "%");
	}
}
