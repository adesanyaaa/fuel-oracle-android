package org.biu.ufo.openxc.sources;

import com.openxc.VehicleManager;
import com.openxc.remote.RawMeasurement;
import com.openxc.sources.BaseVehicleDataSource;

/**
 * A single {@link ObdDataSource} should be added to {@link VehicleManager}
 * 
 * {@link ObdDataSource#notifyMeasurement(RawMeasurement)} is called on each measurement we want to pass to OpenXC.
 * 
 * @author Roee Shlomo
 *
 */
public class ObdDataSource extends BaseVehicleDataSource {

	public void notifyMeasurement(RawMeasurement measurement) {
		handleMessage(measurement);
	}

}
