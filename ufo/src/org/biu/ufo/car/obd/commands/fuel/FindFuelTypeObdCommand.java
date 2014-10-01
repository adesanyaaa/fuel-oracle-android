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
package org.biu.ufo.car.obd.commands.fuel;

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;
import org.biu.ufo.car.obd.enums.FuelType;

/**
 * This command is intended to determine the vehicle fuel type.
 */
public class FindFuelTypeObdCommand extends BaseObdQueryCommand {
	public static String CMD = "01 51";

	private int fuelType = 0;
	
	public FuelType getValue() {
		return FuelType.fromValue(fuelType);
	}

	@Override
	protected void performCalculations() {
		if(data.length >= 3) {
			// ignore first two bytes [hh hh] of the response
			fuelType = data[2];
		}
	}

	@Override
	public String getFormattedResult() {
		return FuelType.fromValue(fuelType).getDescription();
	}
	
	@Override
	public String getCommand() {
		return CMD;
	}
	
	@Override
	public String getName() {
		return AvailableCommandNames.FUEL_TYPE.getValue();
	}

}