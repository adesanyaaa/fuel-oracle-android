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

import org.biu.ufo.car.obd.commands.BaseObdQueryCommand;
import org.biu.ufo.car.obd.commands.SystemOfUnits;

/**
 * Abstract pressure command.
 */
public abstract class PressureObdCommand extends BaseObdQueryCommand implements SystemOfUnits {

  protected int tempValue = 0;
  protected int pressure = 0;
  protected boolean useImperialUnits;
  
  protected String cmd;
  
  /**
   * Default ctor
   * 
   * @param cmd
   */
  public PressureObdCommand(String cmd, boolean useImperialUnits) {
    this.cmd = cmd;
    this.useImperialUnits = useImperialUnits;
  }

  /**
   * Some PressureObdCommand subclasses will need to implement this method in
   * order to determine the final kPa value.
   * 
   * *NEED* to read tempValue
   * 
   * @return
   */
  protected int preparePressureValue() {
    return data[2];
  }

  protected void performCalculations() {
    if (!NODATA.equals(result))
      // ignore first two bytes [hh hh] of the response
      pressure = preparePressureValue();
  }

  @Override
  public String getFormattedResult() {
    return useImperialUnits ? String.format("%.1f%s", getImperialUnit(), "psi")
        : String.format("%d%s", pressure, "kPa");
  }

  /**
   * @return the pressure in kPa
   */
  public int getMetricUnit() {
    return pressure;
  }

  /**
   * @return the pressure in psi
   */
  public float getImperialUnit() {
    return new Double(pressure * 0.145037738).floatValue();
  }
  
  @Override
  public String getCommand() {
  	return cmd;
  }


}