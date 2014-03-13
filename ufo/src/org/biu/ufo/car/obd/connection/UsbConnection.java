package org.biu.ufo.car.obd.connection;

import java.io.IOException;
import java.net.URI;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.interfaces.UriBasedVehicleInterfaceMixin;
import com.openxc.interfaces.usb.UsbDeviceException;
import com.openxc.interfaces.usb.UsbDeviceUtilities;
import com.openxc.sources.DataSourceException;
import com.openxc.sources.DataSourceResourceException;

/**
 * A USB connection handler
 * 
 * The device used (if different from the default) can be specified by passing
 * an custom URI to the constructor. The expected format of this URI is defined
 * in {@link UsbDeviceUtilities}.
 *
 * According to Android's USB device usage requirements, this class requests
 * permission for the USB device from the user before accessing it. This may
 * cause a pop-up dialog that the user must dismiss before the data source will
 * become active.
 */
@TargetApi(12)
public class UsbConnection extends Connection {
	private static final String TAG = "UsbConnection";

	private static final int ENDPOINT_COUNT = 2;
	public static final String ACTION_USB_PERMISSION =
			"com.ford.openxc.USB_PERMISSION";
	public static final String ACTION_USB_DEVICE_ATTACHED =
			"com.ford.openxc.USB_DEVICE_ATTACHED";

	private UsbManager mManager;
	private UsbDeviceConnection mConnection;
	private UsbInterface mInterface;
	private UsbEndpoint mInEndpoint;
	private UsbEndpoint mOutEndpoint;
	private PendingIntent mPermissionIntent;
	private URI mDeviceUri;

	public UsbConnection(Context context, ConnectionCallback callback,
			URI deviceUri) throws ConnectionException {
		super(context, callback);

		mDeviceUri = createUri(deviceUri);

		try {
			mManager = (UsbManager) getContext().getSystemService(
					Context.USB_SERVICE);
		} catch(NoClassDefFoundError e) {
			String message = "No USB service found on this device -- " +
					"can't use USB vehicle interface";
			Log.w(TAG, message);
			throw new ConnectionException(message);
		}
		mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0,
				new Intent(ACTION_USB_PERMISSION), 0);
	}

	public UsbConnection(Context context, ConnectionCallback callback, String uriString)
			throws ConnectionException {
		this(context, callback, createUri(uriString));
	}

	@Override
	public synchronized boolean start() {
		if(super.start()) {
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			getContext().registerReceiver(mBroadcastReceiver, filter);

			filter = new IntentFilter();
			filter.addAction(ACTION_USB_DEVICE_ATTACHED);
			getContext().registerReceiver(mBroadcastReceiver, filter);

			filter = new IntentFilter();
			filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
			getContext().registerReceiver(mBroadcastReceiver, filter);

			initializeDevice();
			return true;
		}
		return false;
	}

	/**
	 * Unregister USB device intent broadcast receivers and stop waiting for a
	 * connection.
	 *
	 * This should be called before the object is given up to the garbage
	 * collector to avoid leaking a receiver in the Android framework.
	 */
	@Override
	public boolean stop() {
		if(super.stop()) {
			try {
				getContext().unregisterReceiver(mBroadcastReceiver);
			} catch(IllegalArgumentException e) {
				Log.d(TAG, "Unable to unregster receiver when stopping, probably not registered");
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isConnected() {
		return mConnection != null && super.isConnected();
	}

	@Override
	protected void disconnect() {
		if(!isConnected()) {
			return;
		}

		Log.d(TAG, "Closing connection " + mConnection +
				" with USB device");
		mConnectionLock.writeLock().lock();
		try {
			mConnection.close();
			mConnection = null;
			mInEndpoint = null;
			mOutEndpoint = null;
			mInterface = null;
			disconnected();
		} finally {
			mConnectionLock.writeLock().unlock();
		}    	
	}

	@Override
	public int read(byte[] bytes) throws IOException {
		mConnectionLock.readLock().lock();
		int bytesRead = 0;
		try {
			if(isConnected()) {
				bytesRead = mConnection.bulkTransfer(mInEndpoint, bytes, bytes.length, 0);
			}
		} finally {
			mConnectionLock.readLock().unlock();
		}
		return bytesRead;
	}

	@Override
	public boolean write(byte[] bytes) throws IOException {
		if(mConnection != null) {
			if(mOutEndpoint != null) {
				Log.d(TAG, "Writing bytes to USB: " + bytes);
				int transferred = mConnection.bulkTransfer(
						mOutEndpoint, bytes, bytes.length, 0);
				if(transferred < 0) {
					Log.w(TAG, "Unable to write CAN message to USB endpoint, error "
							+ transferred);
					return false;
				}
			} else {
				Log.w(TAG, "No OUT endpoint available on USB device, " +
						"can't send write command");
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	protected void connect() throws ConnectionException {
		// Do nothing! it will reconnect using mBroadcastReceiver
	}

	private void initializeDevice() {
		try {
			connectToDevice(mManager, mDeviceUri);
		} catch(DataSourceException e) {
			Log.i(TAG, "Unable to load USB device -- " +
					"waiting for it to appear", e);
		}
	}

	private void connectToDevice(UsbManager manager, URI deviceUri)
			throws DataSourceResourceException {
		connectToDevice(manager,
				UsbDeviceUtilities.vendorFromUri(mDeviceUri),
				UsbDeviceUtilities.productFromUri(mDeviceUri));
	}

	private void connectToDevice(UsbManager manager, int vendorId,
			int productId) throws DataSourceResourceException {
		UsbDevice device = findDevice(manager, vendorId, productId);
		if(manager.hasPermission(device)) {
			Log.d(TAG, "Already have permission to use " + device);
			openConnection(device);
		} else {
			Log.d(TAG, "Requesting permission for " + device);
			manager.requestPermission(device, mPermissionIntent);
		}
	}

	private UsbDevice findDevice(UsbManager manager, int vendorId,
			int productId) throws DataSourceResourceException {
		Log.d(TAG, "Looking for USB device with vendor ID " + vendorId +
				" and product ID " + productId);

		for(UsbDevice candidateDevice : manager.getDeviceList().values()) {
			if(candidateDevice.getVendorId() == vendorId
					&& candidateDevice.getProductId() == productId) {
				Log.d(TAG, "Found USB device " + candidateDevice);
				return candidateDevice;
			}
		}

		throw new DataSourceResourceException("USB device with vendor " +
				"ID " + vendorId + " and product ID " + productId +
				" not found");
	}

	private void openConnection(UsbDevice device) {
		if (device != null) {
			mConnectionLock.writeLock().lock();
			try {
				mConnection = setupDevice(mManager, device);
				connected();
				Log.i(TAG, "Connected to USB device with " +
						mConnection);
			} catch(UsbDeviceException e) {
				Log.w("Couldn't open USB device", e);
			} finally {
				mConnectionLock.writeLock().unlock();
			}
		} else {
			Log.d(TAG, "Permission denied for device " + device);
		}
	}

	private UsbDeviceConnection setupDevice(UsbManager manager,
			UsbDevice device) throws UsbDeviceException {
		if(device.getInterfaceCount() != 1) {
			throw new UsbDeviceException("USB device didn't have an " +
					"interface for us to open");
		}
		UsbInterface iface = null;
		for(int i = 0; i < device.getInterfaceCount(); i++) {
			iface = device.getInterface(i);
			if(iface.getEndpointCount() == ENDPOINT_COUNT) {
				break;
			}
		}

		if(iface == null) {
			Log.w(TAG, "Unable to find a USB device interface with the " +
					"expected number of endpoints (" + ENDPOINT_COUNT + ")");
			return null;
		}

		for(int i = 0; i < iface.getEndpointCount(); i++) {
			UsbEndpoint endpoint = iface.getEndpoint(i);
			if(endpoint.getType() ==
					UsbConstants.USB_ENDPOINT_XFER_BULK) {
				if(endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
					Log.d(TAG, "Found IN endpoint " + endpoint);
					mInEndpoint = endpoint;
				} else {
					Log.d(TAG, "Found OUT endpoint " + endpoint);
					mOutEndpoint = endpoint;
				}
			}

			if(mInEndpoint != null && mOutEndpoint != null) {
				break;
			}
		}
		return openInterface(manager, device, iface);
	}

	private UsbDeviceConnection openInterface(UsbManager manager,
			UsbDevice device, UsbInterface iface)
					throws UsbDeviceException {
		UsbDeviceConnection connection = manager.openDevice(device);
		if(connection == null) {
			throw new UsbDeviceException("Couldn't open a connection to " +
					"device -- user may not have given permission");
		}
		mInterface = iface;
		connection.claimInterface(mInterface, true);
		return connection;
	}


	private static URI createUri(String uriString) throws ConnectionException {
		URI uri;
		if(uriString == null) {
			uri = null;
		} else {
			try {
				uri = UriBasedVehicleInterfaceMixin.createUri(uriString);
			} catch (DataSourceException e) {
				throw new ConnectionException(e.getMessage());
			}
		}
		return createUri(uri);
	}

	private static URI createUri(URI uri) throws ConnectionException {
		if(uri == null) {
			uri = UsbDeviceUtilities.DEFAULT_USB_DEVICE_URI;
			Log.i(TAG, "No USB device specified -- using default " +
					uri);
		}

		if(!validateResource(uri)) {
			throw new ConnectionException(
					"USB device URI must have the usb:// scheme");
		}

		// will throw an exception if not in the correct format
		try {
			UsbDeviceUtilities.vendorFromUri(uri);
			UsbDeviceUtilities.productFromUri(uri);
		} catch (DataSourceResourceException e) {
			throw new ConnectionException(e.getMessage());
		}

		return uri;
	}

	private static boolean validateResource(URI uri) {
		return uri.getScheme() != null && uri.getScheme().equals("usb");
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				UsbDevice device = (UsbDevice) intent.getParcelableExtra(
						UsbManager.EXTRA_DEVICE);

				if(intent.getBooleanExtra(
						UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
					openConnection(device);
				} else {
					Log.i(TAG, "User declined permission for device " +
							device);
				}
			} else if(ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				Log.d(TAG, "Device attached");
				try {
					connectToDevice(mManager, mDeviceUri);
				} catch(DataSourceException e) {
					Log.i(TAG, "Unable to load USB device -- waiting for it " +
							"to appear", e);
				}
			} else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Log.d(TAG, "Device detached");
				disconnect();
			}
		}
	};

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("device", mDeviceUri)
				.add("connection", mConnection)
				.add("in_endpoint", mInEndpoint)
				.add("out_endpoint", mOutEndpoint)
				.toString();
	}

}
