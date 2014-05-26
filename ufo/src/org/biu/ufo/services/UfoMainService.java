package org.biu.ufo.services;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.car.openxc.VehicleManagerConnector;
import org.biu.ufo.car.openxc.VehicleManagerConnector.VehicleManagerConnectorCallback;
import org.biu.ufo.control.Controller;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.connection.ObdConnectionLostMessage;
import org.biu.ufo.control.events.connection.ObdDeviceAddressChangedMessage;
import org.biu.ufo.control.events.raw.DistanceTraveled;
import org.biu.ufo.control.events.raw.EngineSpeedMessage;
import org.biu.ufo.control.events.raw.FuelConsumedMessage;
import org.biu.ufo.control.events.raw.FuelLevelMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.services.CarGatewayService.CarGatewayServiceBinder;
import org.biu.ufo.settings.PreferenceManagerService_;
import org.biu.ufo.ui.activities.MainActivity;
import org.biu.ufo.ui.activities.MainActivity_;
import org.biu.ufo.ui.popups.FuelNextContentView;
import org.biu.ufo.ui.popups.FuelNextContentView_;
import org.biu.ufo.ui.popups.PopupNotificationManager;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelConsumed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Latitude;
import com.openxc.measurements.Longitude;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.Odometer;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.remote.VehicleServiceException;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * UfoMainService
 * 
 * A Foreground service with "running" notification.
 * Should run as long as we are connected to some data source (typically the car)
 * Should monitor source availability.
 *  
 * @author Roee Shlomo
 *
 */
@EService
public class UfoMainService extends StandOutWindow implements VehicleManagerConnectorCallback {
	private final static String TAG = "UfoMainService";

	public final static int SERVICE_FUEL_NEXT_ID = 1541;

	@Bean
	VehicleManagerConnector mVMmConnector;

	@Bean
	OttoBus bus;

	@Bean
	Controller controller;
	
	@Bean
	PopupNotificationManager popupNotificationManager;

	private CarGatewayServiceBinder mCarGateway;

	private LocationMessage locationMessage = new LocationMessage();
	private Location lastKnownLocation;

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Bind to VM
		mVMmConnector.bindToVehicleManager(this);

		// Bind to CarGateway
		bindService(new Intent(this, CarGatewayService_.class), mCarGatewayConnection, Context.BIND_AUTO_CREATE);

		// Bind to preferences manager
		bindService(new Intent(this, PreferenceManagerService_.class), mPreferencesManagerConnection, Context.BIND_AUTO_CREATE);
		
		// Initialize controller
		controller.init();

		// Start popup notification manager
		popupNotificationManager.start();
		
		// Register on bus
		bus.register(this);
		
		// Make foreground (using StandOut!)
		show(StandOutWindow.DEFAULT_ID);
		Window hiddenWindow = getWindow(StandOutWindow.DEFAULT_ID);
		WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.removeView(hiddenWindow);
		hiddenWindow.visibility = Window.VISIBILITY_GONE;

	}
	
	@Produce
	public LocationMessage produceLatestKnownLocation() {
		if(lastKnownLocation != null) {
			LocationMessage msg = new LocationMessage();
			msg.setLatitude(lastKnownLocation.getLatitude());
			msg.setLongitude(lastKnownLocation.getLongitude());
			return msg;
		}
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Close controller
		controller.close();

		// Stop popup notification manager
		popupNotificationManager.stop();
		
		// Unregister from bus
		bus.unregister(this);
		
		// Unbind from preferences manager
		unbindService(mPreferencesManagerConnection);    

		// Unbind from CarGateway
		mCarGateway.stop();
		unbindService(mCarGatewayConnection);    	

		// Unbind from VM
		mVMmConnector.unbindFromVehicleManager();
	}

	
	@Override
	@UiThread
	public void onVMConnected() {
		try {
			mVMmConnector.getVehicleManager().addListener(FuelLevel.class, fuelLevelListener);
			mVMmConnector.getVehicleManager().addListener(VehicleSpeed.class, vehicleSpeedListener);
			mVMmConnector.getVehicleManager().addListener(FuelConsumed.class, fuelConsumedListener);
			mVMmConnector.getVehicleManager().addListener(Odometer.class, odometerListener);
			mVMmConnector.getVehicleManager().addListener(Latitude.class, latitudeListener);
			mVMmConnector.getVehicleManager().addListener(Longitude.class, longitudeListener);
			mVMmConnector.getVehicleManager().addListener(EngineSpeed.class, engineSpeedListener);
		} catch (VehicleServiceException e) {
			e.printStackTrace();
		} catch (UnrecognizedMeasurementTypeException e) {
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
	public void onObdDeviceAddressChanged(final ObdDeviceAddressChangedMessage event) {
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
	public void onObdConnectionLost(final ObdConnectionLostMessage event) {
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

	@UiThread
	void post(Object o) {
		bus.post(o);
	}

	private Latitude.Listener latitudeListener = new Latitude.Listener() {

		@Override
		@UiThread
		public void receive(Measurement measurement) {
			final Latitude latitude = (Latitude) measurement;			
			locationMessage.setLatitude(latitude.getValue().doubleValue());
			if (locationMessage.properLocation()){
				lastKnownLocation = locationMessage.getLocation();
				post(locationMessage);
				locationMessage = new LocationMessage();
			}
		}
	};
	
	private Longitude.Listener longitudeListener = new Longitude.Listener() {

		@Override
		public void receive(Measurement measurement) {
			final Longitude longitude = (Longitude) measurement;		
			locationMessage.setLongitude(longitude.getValue().doubleValue());
			if (locationMessage.properLocation()){
				lastKnownLocation = locationMessage.getLocation();
				post(locationMessage);
				locationMessage = new LocationMessage();
			}
		}

	};

	private EngineSpeed.Listener engineSpeedListener = new EngineSpeed.Listener() {

		@Override
		public void receive(Measurement measurement) {
			final EngineSpeed engineSpeed = (EngineSpeed) measurement;
			post(new EngineSpeedMessage(engineSpeed));
		}

	};

	private VehicleSpeed.Listener vehicleSpeedListener = new VehicleSpeed.Listener() {

		@Override
		public void receive(Measurement measurement) {
			final VehicleSpeed vehicleSpeed = (VehicleSpeed) measurement;
			post(new VehicleSpeedMessage(vehicleSpeed));

		}
	};

	private FuelConsumed.Listener fuelConsumedListener = new FuelConsumed.Listener() {
		@Override
		public void receive(Measurement measurement) {
			final FuelConsumed fuelConsumed = (FuelConsumed) measurement;
			post(new FuelConsumedMessage(fuelConsumed));
		}
	};

	private FuelLevel.Listener fuelLevelListener = new FuelLevel.Listener() {
		@Override
		public void receive(Measurement measurement) {
			final FuelLevel fuelLevel = (FuelLevel) measurement;
			post(new FuelLevelMessage(fuelLevel));
		}
	};
	
	private Odometer.Listener odometerListener = new Odometer.Listener() {

		@Override
		public void receive(Measurement measurement) {
			final Odometer odometer = (Odometer) measurement;
			post(new DistanceTraveled(odometer));		}
		
	};
	

	/* StandOut stuff */
	
	@Override
	public String getAppName() {
		return getString(R.string.app_name);
	}
	
	@Override
	public int getAppIcon() {
		return R.drawable.ic_launcher;
	}
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (ACTION_SHOW.equals(action) || ACTION_RESTORE.equals(action)) {
				int id = intent.getIntExtra("id", DEFAULT_ID);
				Window window = getWindow(id);
				if(window != null && window.visibility == Window.VISIBILITY_VISIBLE) {
					return START_NOT_STICKY;
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public Notification getPersistentNotification(int id) {
		if(id == StandOutWindow.DEFAULT_ID) {
			Intent intent = new Intent(this,MainActivity_.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
					Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra("screen", MainActivity.MAIN);
			PendingIntent pendingIntent = PendingIntent.getActivity(
					this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
			notificationBuilder.setContentTitle(getString(R.string.app_name))
			.setContentInfo(getString(R.string.notification_content))
			.setSmallIcon(R.drawable.gasstation)
			.setContentIntent(pendingIntent);
			
			return notificationBuilder.build();
		}
		return null;
	}
			
	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		if(id == SERVICE_FUEL_NEXT_ID) {
			final FuelNextContentView view = FuelNextContentView_.build(this);	
			FuelRecommendationMessage fuelRecommendation = (FuelRecommendationMessage)popupNotificationManager.getPopupRecommendation();
			view.fillContent(popupNotificationManager, fuelRecommendation);
			frame.addView(view);
		}
		//TODO:
//		else if(id == SOME_OTHER_ID) {
//			
//		}
		else {
			frame.addView(new FrameLayout(this));			
		}
	}

	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		int y = StandOutLayoutParams.TOP + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
		int x = StandOutLayoutParams.LEFT;
		int w = StandOutLayoutParams.WRAP_CONTENT;
		int h = StandOutLayoutParams.WRAP_CONTENT;
		StandOutLayoutParams params = new StandOutLayoutParams(id, w, h, x, y);
		params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
		return params;
	}
		
	@Override
	public boolean onTouchBody(int id, Window window, View view, MotionEvent event) {
		super.onTouchBody(id, window, view, event);
		if(id != StandOutWindow.DEFAULT_ID && event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			popupNotificationManager.closePopup();
		}
		return false;
	}
	
	@Override
	public boolean onKeyEvent(int id, Window window, KeyEvent event) {
    	if(event.getKeyCode() == KeyEvent.KEYCODE_HOME ||
    			event.getKeyCode() == KeyEvent.KEYCODE_MENU ||
    			event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
    		popupNotificationManager.closePopup();
    	}
		return super.onKeyEvent(id, window, event);
	}
	
	@Override
	public boolean onBringToFront(int id, Window window) {
		if(id != StandOutWindow.DEFAULT_ID) {
			popupNotificationManager.onPopupClick();
		}		
		return true;
	}
	
	@Override
	public boolean onFocusChange(int id, Window window, boolean focus) {
		if(id != StandOutWindow.DEFAULT_ID && focus == false) {
			popupNotificationManager.closePopup();
		}		
		return super.onFocusChange(id, window, focus);
	}
	
	@Override
	public int getFlags(int id) {
		if(id == SERVICE_FUEL_NEXT_ID) {
			return super.getFlags(id) |
					StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE |
					StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP |
					StandOutFlags.FLAG_WINDOW_FOCUS_INDICATOR_DISABLE ;
		}
//		else if(id == SOME_OTHER_ID) {
//		
//		}
		return super.getFlags(id) | StandOutFlags.FLAG_WINDOW_HIDE_ENABLE;
	}

	@Override
	public boolean onShow(int id, Window window) {
		if(id != StandOutWindow.DEFAULT_ID) {
			popupNotificationManager.onShown();			
			popupNotificationManager.automaticClosing();
		}
		return super.onShow(id, window);
	}
}
