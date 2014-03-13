package org.biu.ufo.settings;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.VehicleManager;

@EService
public class PreferenceManagerService extends Service {
    private static String TAG = "PreferenceManagerService";

    private IBinder mBinder = new PreferenceBinder();
    private VehicleManager mVehicleManager;

    private List<VehiclePreferenceManager> mPreferenceManagers =
            new ArrayList<VehiclePreferenceManager>();

    public class PreferenceBinder extends Binder {
        public PreferenceManagerService getService() {
            return PreferenceManagerService.this;
        }
    }

    @Bean
    BluetoothPreferenceManager bluetoothPreferenceManager;
    @Bean
    FileRecordingPreferenceManager fileRecordingPreferenceManager;
//    @Bean
//    GpsOverwritePreferenceManager gpsOverwritePreferenceManager;
    @Bean
    NativeGpsPreferenceManager nativeGpsPreferenceManager;
    @Bean
    UploadingPreferenceManager uploadingPreferenceManager;
    @Bean
    NetworkPreferenceManager networkPreferenceManager;
    @Bean
    TraceSourcePreferenceManager traceSourcePreferenceManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service starting");

        bindService(new Intent(this, VehicleManager.class),
                mConnection, Context.BIND_AUTO_CREATE);

        mPreferenceManagers = new ArrayList<VehiclePreferenceManager>();
        mPreferenceManagers.add(bluetoothPreferenceManager);
        mPreferenceManagers.add(fileRecordingPreferenceManager);
//        mPreferenceManagers.add(gpsOverwritePreferenceManager);
        mPreferenceManagers.add(nativeGpsPreferenceManager);
        mPreferenceManagers.add(uploadingPreferenceManager);
        mPreferenceManagers.add(networkPreferenceManager);
        mPreferenceManagers.add(traceSourcePreferenceManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        for(VehiclePreferenceManager manager : mPreferenceManagers) {
            manager.close();
        }

        unbindService(mConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service binding in response to " + intent);
        return mBinder;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).toString();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder)service
                    ).getService();

            new Thread(new Runnable() {
                public void run() {
                    mVehicleManager.waitUntilBound();
                    for(VehiclePreferenceManager manager : mPreferenceManagers) {
                        manager.setVehicleManager(mVehicleManager);
                    }
                }
            }).start();
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleService disconnected unexpectedly");
            mVehicleManager = null;
        }
    };
}
