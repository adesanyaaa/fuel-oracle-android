package org.biu.ufo.openxc;

import org.androidannotations.annotations.EBean;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.openxc.VehicleManager;

@EBean
public class VehicleManagerConnector {
	public static final String TAG = "VehicleManagerConnector";

	public interface VehicleManagerConnectorCallback {
		void onVMConnected();
		void onVMDisconnected();		
	}

	private VehicleManagerConnectorCallback callback;
	private Context context;
	private VehicleManager mVehicleManager;

	public VehicleManagerConnector(Context context) {
		this.context = context;
	}

	public VehicleManager getVehicleManager() {
		return mVehicleManager;
	}

	public void bindToVehicleManager(VehicleManagerConnectorCallback callback) {
		this.callback = callback;
		
		context.bindService(new Intent(context, VehicleManager.class),
				mConnection, Context.BIND_AUTO_CREATE);    	
	}

	public void unbindFromVehicleManager() {
		context.unbindService(mConnection);    	
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
