package org.biu.ufo;

import com.openxc.VehicleManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receive the BOOT_COMPLETED signal and start the VehicleManager.
 *
 * The reason to do this in a central location is to centralize USB permissions
 * management.
 */
public class BootupReceiver extends BroadcastReceiver {
    private final static String TAG = "BootupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.i(TAG, "Starting vehicle service on boot");
//        context.startService(new Intent(context, VehicleManager.class));
    }
}
