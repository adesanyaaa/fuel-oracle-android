package org.biu.ufo.openxc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.openxc.VehicleManager;

public class VehicleManagerConnector {
	public static final String TAG = "VehicleManagerConnector";

	public interface VehicleManagerConnectorCallback {
		void onVMConnected();
		void onVMDisconnected();		
	}

	private VehicleManagerConnectorCallback callback;
	private Context context;
	private VehicleManager mVehicleManager;

	public VehicleManagerConnector(Context context, VehicleManagerConnectorCallback callback) {
		this.context = context;
		this.callback = callback;
	}

	public VehicleManager getVehicleManager() {
		return mVehicleManager;
	}

	public void bindToVehicleManager() {
		context.bindService(new Intent(context, VehicleManager.class),
				mConnection, Context.BIND_AUTO_CREATE);    	
	}

	public void unbindToVehicleManager() {
		context.unbindService(mConnection);    	
	}

	public void cleanup() {
		context = null;
		callback = null;
		mVehicleManager = null;
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i(TAG, "Bound to VehicleManager");
			mVehicleManager = ((VehicleManager.VehicleBinder)service).getService();
			new Thread(new Runnable() {
				public void run() {
					mVehicleManager.waitUntilBound();
					callback.onVMConnected();                
				}
			}).start();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.w(TAG, "VehicleService disconnected unexpectedly");
			mVehicleManager = null;
			callback.onVMDisconnected();
		}
	};

}
