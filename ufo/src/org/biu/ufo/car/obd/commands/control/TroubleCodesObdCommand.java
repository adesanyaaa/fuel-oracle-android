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
import org.biu.ufo.car.obd.enums.AvailableCommandNames;

/**
 * In order to get ECU Trouble Codes, one must first send a DtcNumberObdCommand
 * and by so, determining the number of error codes available by means of
 * getTotalAvailableCodes().
 * <p>
 * If none are available (totalCodes < 1), don't instantiate this command.
 */
public class TroubleCodesObdCommand extends BaseObdQueryCommand {
	public static String CMD = "03";

	protected final static char[] dtcLetters = { 'P', 'C', 'B', 'U' };

	private StringBuffer codes = null;
	private int howManyTroubleCodes = 0;

	/**
	 * Default ctor.
	 */
	public TroubleCodesObdCommand(int howManyTroubleCodes) {
		codes = new StringBuffer();
		this.howManyTroubleCodes = howManyTroubleCodes;
	}

	@Override
	protected void performCalculations() {
//		if (!NODATA.equals(result)) {
//			/*
//			 * Ignore first byte [43] of the response and then read each two bytes.
//			 */
//			int begin = 2; // start at 2nd byte
//			int end = 6; // end at 4th byte
//
//			for (int i = 0; i < howManyTroubleCodes * 2; i++) {
//				// read and jump 2 bytes
//				byte b1 = Byte.parseByte(result.substring(begin, end));
//				begin += 2;
//				end += 2;
//
//				// read and jump 2 bytes
//				byte b2 = Byte.parseByte(result.substring(begin, end));
//				begin += 2;
//				end += 2;
//
//				int tempValue = b1 << 8 | b2;
//			}
//		}

		String[] ress = result.split("\r");
		for (String r : ress) {
			String k = r.replace("\r", "");
			codes.append(k);
			codes.append("\n");
		}
	}

	/**
	 * @return the formatted result of this command in string representation.
	 */
	public String formatResult() {
		return codes.toString();
	}

	@Override
	public String getFormattedResult() {
		return codes.toString();
	}

	@Override
	public String getName() {
		return AvailableCommandNames.TROUBLE_CODES.getValue();
	}

	@Override
	public String getCommand() {
		// TODO Auto-generated method stub
		return CMD;
	}

}