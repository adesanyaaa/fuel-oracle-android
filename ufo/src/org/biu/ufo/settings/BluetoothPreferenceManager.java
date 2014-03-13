package org.biu.ufo.settings;

import java.util.Set;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.events.ObdDeviceAddressChanged;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.openxc.R;
import com.openxc.interfaces.bluetooth.BluetoothVehicleInterface;

/**
 * Enable or disable receiving vehicle data from a Bluetooth CAN device.
 */
@EBean
public class BluetoothPreferenceManager extends VehiclePreferenceManager {
    private final static String TAG = "BluetoothPreferenceManager";
    public final static String AUTO_DEVICE_SELECTION_ENTRY = "Automatic";
   
    @Bean
    OttoBus bus;
    
    public BluetoothPreferenceManager(Context context) {
        super(context);
    }

    protected PreferenceListener createPreferenceListener() {
        return new PreferenceListener() {
            private int[] WATCHED_PREFERENCE_KEY_IDS = {
                R.string.bluetooth_checkbox_key,
                R.string.bluetooth_mac_key,
                org.biu.ufo.R.string.bluetooth_is_obd,
            };

            protected int[] getWatchedPreferenceKeyIds() {
                return WATCHED_PREFERENCE_KEY_IDS;
            }

            public void readStoredPreferences() {
                setBluetoothStatus(getPreferences().getBoolean(
                            getString(R.string.bluetooth_checkbox_key), false));
            }
        };
    }
    
    @UiThread
    void setBluetoothStatus(boolean enabled) {
        Log.i(TAG, "Setting bluetooth data source to " + enabled);
        
        if(enabled) {        	
        	String deviceAddress = getPreferenceString(R.string.bluetooth_mac_key);

        	boolean isOBD = getPreferences().getBoolean(getString(org.biu.ufo.R.string.bluetooth_is_obd_key), true);
        	if(isOBD) {
        		// Make sure not using BT VI 
                getVehicleManager().removeVehicleInterface(BluetoothVehicleInterface.class);
                
                // But use OBD connection
                bus.post(new ObdDeviceAddressChanged(deviceAddress));                	

        	} else {
        		// Stop the OBD connection
        		bus.post(new ObdDeviceAddressChanged(null));

                // Connect to CX-VI device
            	if(deviceAddress == null || deviceAddress.equals(AUTO_DEVICE_SELECTION_ENTRY)) {
            		deviceAddress = searchForVehicleInterface();
            	}

            	if(deviceAddress != null) {
            		getVehicleManager().addVehicleInterface(
            				BluetoothVehicleInterface.class, deviceAddress);
            	} else {
            		searchForVehicleInterface();
            		Log.d(TAG, "No Bluetooth device MAC set yet (" + deviceAddress +
            				"), not starting source");
            	}
        	}
        } else {
        	// No more connections please
        	getVehicleManager().removeVehicleInterface(BluetoothVehicleInterface.class);
        	bus.post(new ObdDeviceAddressChanged(null));
        }
            	
    }

    private String searchForVehicleInterface() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        String deviceAddress = null;
        if(adapter != null && adapter.isEnabled()){
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            for(BluetoothDevice device : pairedDevices) {
                if(device.getName().startsWith(
                            BluetoothVehicleInterface.DEVICE_NAME_PREFIX)) {
                    Log.d(TAG, "Found paired OpenXC BT VI " + device.getName() +
                            ", will be auto-connected.");
                    deviceAddress = device.getAddress();
                }
            }
        }
        return deviceAddress;
    }
}
