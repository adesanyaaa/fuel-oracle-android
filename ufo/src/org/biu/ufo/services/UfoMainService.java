package org.biu.ufo.services;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.BusProvider;
import org.biu.ufo.R;
import org.biu.ufo.configuration.PreferenceManagerService_;
import org.biu.ufo.events.LowFuelLevel;
import org.biu.ufo.events.ObdConnectionLost;
import org.biu.ufo.events.ObdDeviceAddressChanged;
import org.biu.ufo.openxc.VehicleManagerConnector;
import org.biu.ufo.openxc.VehicleManagerConnector.VehicleManagerConnectorCallback;
import org.biu.ufo.services.CarGatewayService.CarGatewayServiceBinder;

import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.common.eventbus.Subscribe;
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
	
	@Bean
	VehicleManagerConnector mVMmConnector;

	private CarGatewayServiceBinder mCarGateway;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Move to foreground
		moveToForeground();

		// Bind to VM
		mVMmConnector.bindToVehicleManager(this);

		// Bind to CarGateway
		bindService(new Intent(this, CarGatewayService_.class), mCarGatewayConnection, Context.BIND_AUTO_CREATE);

		// Bind to preferences manager
		bindService(new Intent(this, PreferenceManagerService_.class), mPreferencesManagerConnection, Context.BIND_AUTO_CREATE);

		// Register on bus
		BusProvider.getEventBus().register(this);

		
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// Unregister from bus
		BusProvider.getEventBus().unregister(this);

		// Unbind from preferences manager
		unbindService(mPreferencesManagerConnection);    
		
		// Unbind from CarGateway
		unbindService(mCarGatewayConnection);    	
		
		// Unbind from VM
		mVMmConnector.unbindFromVehicleManager();

		// Remove from foreground
		removeFromForeground();
	}

	private void moveToForeground() {
		Log.i(TAG, "Moving service to foreground.");

		try {
			Intent intent = new Intent(this,
					Class.forName("org.biu.ufo.activities.MainActivity_"));
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
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isEnabled = prefs.getBoolean(getString(R.string.bluetooth_checkbox_key), true);
		boolean isObd = prefs.getBoolean(getString(R.string.bluetooth_is_obd_key), true);
		String deviceAddress = prefs.getString(getString(R.string.bluetooth_mac_key), null);
		if(isEnabled && isObd && deviceAddress != null) {
			if(mCarGateway.start(deviceAddress)) {
				Log.d(TAG, "Car Gateway started");
			} else {
				Log.d(TAG, "Car Gateway could not be started");
			}
		} else {
			Log.d(TAG, "Car Gateway not needed to be started");
		}
	}

	@Subscribe 
	public void onObdDeviceAddressChanged(ObdDeviceAddressChanged event) {
		Log.e(TAG, "OBD device changed");
		if(mCarGateway != null) {
			mCarGateway.stop();
			
			if(event.getAddress() != null) {
				if(mCarGateway.start(event.getAddress())) {
					Log.d(TAG, "Car Gateway started");
				} else {
					Log.d(TAG, "Car Gateway could not be started");
				}				
			} else {
				Log.d(TAG, "Car Gateway stopped due to preferences change");
			}			
		}
		
	}

	@Subscribe 
	public void onObdConnectionLost(ObdConnectionLost event) {
		Log.e(TAG, "OBD connection lost");
	}
	
	protected void onCGDisonnected() {
		Log.w(TAG, "CarGateway disconnected unexpectedly");
		mCarGateway = null;
	}
	
	private ServiceConnection mCarGatewayConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			onCGConnected((CarGatewayServiceBinder)service);
		}

		public void onServiceDisconnected(ComponentName className) {
			onCGDisonnected();
		}
	};

	private ServiceConnection mPreferencesManagerConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
		}

		public void onServiceDisconnected(ComponentName className) {
		}
	};

	private FuelLevel.Listener fuelLevelListener = new FuelLevel.Listener() {
		@Override
		@UiThread
		public void receive(Measurement measurement) {
			final FuelLevel fuelLevel = (FuelLevel) measurement;
			if(fuelLevel.getValue().doubleValue() < 10) {
				BusProvider.getEventBus().post(new LowFuelLevel());			        	
			}
		}
	};

}
