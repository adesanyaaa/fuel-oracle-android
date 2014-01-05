package org.biu.ufo.services;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.BusProvider;
import org.biu.ufo.R;
import org.biu.ufo.events.LowFuelLevel;
import org.biu.ufo.openxc.VehicleManagerConnector;
import org.biu.ufo.openxc.VehicleManagerConnector.VehicleManagerConnectorCallback;
import org.biu.ufo.services.CarGatewayService.CarGatewayServiceBinder;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.openxc.VehicleManager;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.remote.VehicleServiceException;

/**
 * UfoMainService
 * 
 * A Foreground service with "running" notification.
 * Should run as long as we are connected to some data source (typically the car)
 * Should monitor source availability.
 *  
 *  TODO: bind to CarGatewayService, bind to VM, 
 *  
 * @author Roee Shlomo
 *
 */
@EService
public class UfoMainService extends Service implements VehicleManagerConnectorCallback {
	private final static String TAG = "UfoMainService";
	private final static int SERVICE_NOTIFICATION_ID = 1541;

	private VehicleManagerConnector mVMmConnector;
	private CarGatewayServiceBinder mCarGateway;

	private ServiceConnection mCarGatewayConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			onCGConnected((CarGatewayServiceBinder)service);
		}

		public void onServiceDisconnected(ComponentName className) {
			onCGDisonnected();
		}
	};

	private FuelLevel.Listener fuelLevelListener = new FuelLevel.Listener() {
		@Override
		public void receive(Measurement measurement) {
			final FuelLevel fuelLevel = (FuelLevel) measurement;
			if(fuelLevel.getValue().doubleValue() < 10) {
				BusProvider.getInstance().post(new LowFuelLevel());			        	
			}
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		moveToForeground();

		// Bind to VM
		mVMmConnector = new VehicleManagerConnector(this, this);
		mVMmConnector.bindToVehicleManager();

		// Bind to CarGateway
		bindService(new Intent(this, VehicleManager.class),
				mCarGatewayConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Unbind from VM
		mVMmConnector.unbindToVehicleManager();
		mVMmConnector.cleanup();

		// Unbind from CarGateway
		unbindService(mCarGatewayConnection);    	

		// Remove from foreground
		removeFromForeground();
	}

	private void moveToForeground() {
		Log.i(TAG, "Moving service to foreground.");

		try {
			// TODO change class name
			Intent intent = new Intent(this,
					Class.forName("com.openxc.enabler.OpenXcEnablerActivity"));
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
					Intent.FLAG_ACTIVITY_SINGLE_TOP);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					this, 0, intent, 0);

			NotificationCompat.Builder notificationBuilder =
					new NotificationCompat.Builder(this);
			notificationBuilder.setContentTitle(getString(R.string.app_name))
			.setContentInfo(getString(R.string.notification_content))
			.setSmallIcon(R.drawable.openxc_notification_icon_small_white)
			.setContentIntent(pendingIntent);

			startForeground(SERVICE_NOTIFICATION_ID,
					notificationBuilder.build());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Could not find Main Activity class.", e);
		}
	}

	private void removeFromForeground(){
		Log.i(TAG, "Removing service from foreground.");
		stopForeground(true);
	}

	@Override
	@UiThread
	public void onVMConnected() {
		// TODO Auto-generated method stub

		try {
			mVMmConnector.getVehicleManager().addListener(FuelLevel.class, fuelLevelListener);
		} catch (VehicleServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecognizedMeasurementTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onVMDisconnected() {
		// TODO Auto-generated method stub

	}

	protected void onCGConnected(CarGatewayServiceBinder service) {
		Log.i(TAG, "Bound to CarGateway");
		mCarGateway = service;
	}

	protected void onCGDisonnected() {
		Log.w(TAG, "CarGateway disconnected unexpectedly");
		mCarGateway = null;
	}

}
