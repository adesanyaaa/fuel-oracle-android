/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.biu.ufo.car.obd.commands.control;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.commands.SystemOfUnits;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

/**
 * Distance traveled since codes cleared-up.
 */
public class DistanceTraveledSinceCodesClearedObdCommand extends BaseObdQueryCommand implements SystemOfUnits {
	public static String CMD = "01 44";

	private int km = 0;

	@Override
	protected void performCalculations() {
		if (data.length >= 4)
			// ignore first two bytes [01 31] of the response
			km = data[2] * 256 + data[3];
	}

	@Override
	public String getFormattedResult() {
		return String.format("%.2f%s", km, "km");
	}

	@Override
	public float getImperialUnit() {
		return new Double(km * 0.621371192).floatValue();
	}

	public int getKm() {
		return km;
	}

	public void setKm(int km) {
		this.km = km;
	}

	@Override
	public String getName() {
		return AvailableCommandNames.DISTANCE_TRAVELED_AFTER_CODES_CLEARED
				.getValue();
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return CMD;
	}

}