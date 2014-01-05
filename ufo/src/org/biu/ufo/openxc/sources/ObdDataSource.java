package org.biu.ufo.openxc.sources;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;

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
@EBean(scope=Scope.Singleton)
public class ObdDataSource extends BaseVehicleDataSource {

	public void notifyMeasurement(RawMeasurement measurement) {
		handleMessage(measurement);
	}

}
