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
package org.biu.ufo.car.obd.commands.pressure;

import org.biu.ufo.car.obd.enums.AvailableCommandNames;

public class FuelPressureObdCommand extends PressureObdCommand {

	// TODO: should it be 01 0A ?
	public FuelPressureObdCommand(boolean useImperialUnits) {
		super("010A", useImperialUnits);
	}

	/**
	 * TODO describe of why we multiply by 3
	 * 
	 * @param temp
	 * @return
	 */
	@Override
	protected final int preparePressureValue() {
		return data[2] * 3;
	}

	@Override
	public String getName() {
		return AvailableCommandNames.FUEL_PRESSURE.getValue();
	}

}