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

import org.biu.ufo.car.obd.commands.PrecentageObdQueryCommand;
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

/**
 * Timing Advance
 */
public class TimingAdvanceObdCommand extends PrecentageObdQueryCommand {
	public static String CMD = "01 0E";

	@Override
	public String getName() {
		return AvailableCommandNames.TIMING_ADVANCE.getValue();
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return CMD;
	}

}