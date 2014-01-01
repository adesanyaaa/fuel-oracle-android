package org.biu.ufo.connection;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.google.common.base.Objects;
import com.openxc.interfaces.bluetooth.BluetoothException;
import com.openxc.interfaces.bluetooth.DeviceManager;

public class BluetoothConnection extends Connection {
	private static final String TAG = "BluetoothConnection";
	
	private DeviceManager mDeviceManager;
    private String mAddress;
    
    private BufferedWriter mOutStream;
    private BufferedInputStream mInStream;
    private BluetoothSocket mSocket;
    
    public BluetoothConnection(Context context, ConnectionCallback callback, String address) throws BluetoothException {
    	super(context, callback);
    	
        mDeviceManager = new DeviceManager(context);
        setAddress(address);
    }

    /**
     * Connects to device
     */
    @Override
    public boolean start() {
    	if(super.start()) {

    		try {
        		// Connect to device
                waitForConnection();
                return true;
            } catch(InterruptedException e) {
                Log.i(TAG, "Interrupted while waiting for connection - stopping the source");
                stop();
            }
    	}
    	
        return false;    	
    }
    
    /**
     * Disconnects from device
     */
    @Override
    public boolean stop() {
        mDeviceManager.stop();
        closeSocket();
        return super.stop();
    }
    
    /**
     * Check if connected
     */
    @Override
    public boolean isConnected() {
        mConnectionLock.readLock().lock();

        boolean connected = super.isConnected();
        if(mSocket == null) {
            connected = false;
        } else {
            try {
                connected &= mSocket.isConnected();
            } catch (NoSuchMethodError e) {
                // Cannot get isConnected() result before API 14
                // Assume previous result is correct.
            }
        }

        mConnectionLock.readLock().unlock();
        return connected;
    }

	@Override
	public int read(byte[] bytes) throws IOException {
        mConnectionLock.readLock().lock();
        int bytesRead = -1;
        try {
            if(isConnected()) {
                bytesRead = mInStream.read(bytes, 0, bytes.length);
            }
        } finally {
            mConnectionLock.readLock().unlock();
        }
        return bytesRead;
	}

	@Override
	public boolean write(byte[] bytes) throws IOException {
		// TODO Auto-generated method stub
        mConnectionLock.readLock().lock();
        boolean success = false;
        try {
            if(isConnected()) {
            	String message = new String(bytes);	// TODO: this is stupid!
                Log.d(TAG, "Writing message to Bluetooth: " + message);
                mOutStream.write(message);
                // TODO what if we didn't flush every time? might be faster for
                // sustained writes.
                mOutStream.flush();
                success = true;
            } else {
                Log.w(TAG, "Unable to write -- not connected");
            }
        } catch(IOException e) {
            Log.d(TAG, "Error writing to stream", e);
        } finally {
            mConnectionLock.readLock().unlock();
        }
        return success;
	}

	@Override
    protected void disconnect() {
        closeSocket();
        mConnectionLock.writeLock().lock();
        try {
            try {
                if(mInStream != null) {
                    mInStream.close();
                    Log.d(TAG, "Disconnected from the input stream");
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the input stream", e);
            } finally {
                mInStream = null;
            }

            try {
                if(mOutStream != null) {
                    mOutStream.close();
                    Log.d(TAG, "Disconnected from the output stream");
                }
            } catch(IOException e) {
                Log.w(TAG, "Unable to close the output stream", e);
            } finally {
                mOutStream = null;
            }

            disconnected();
        } finally {
            mConnectionLock.writeLock().unlock();
        }
    }
    
	@Override
	protected void connect() throws ConnectionException {
        if(!isRunning()) {
            return;
        }

        mConnectionLock.writeLock().lock();
        try {
            if(!isConnected()) {
                mSocket = mDeviceManager.connect(mAddress);
                connectStreams();
                connected();
            }
        } catch(BluetoothException e) {
            String message = "Unable to connect to device at address "
                + mAddress;
            Log.w(TAG, message, e);
            disconnected();
        } finally {
            mConnectionLock.writeLock().unlock();
        }		
	}
	
    private synchronized void closeSocket() {
        // The Bluetooth socket is thread safe, so we don't grab the connection
        // lock - we also want to forcefully break the connection NOW instead of
        // waiting for the lock if BT is going down
        try {
            if(mSocket != null) {
                mSocket.close();
                Log.d(TAG, "Disconnected from the socket");
            }
        } catch(IOException e) {
            Log.w(TAG, "Unable to close the socket", e);
        } finally {
            mSocket = null;
        }
    }

    private void connectStreams() throws BluetoothException {
        mConnectionLock.writeLock().lock();
        try {
            try {
                mOutStream = new BufferedWriter(new OutputStreamWriter(
                            mSocket.getOutputStream()));
                mInStream = new BufferedInputStream(mSocket.getInputStream());
                Log.i(TAG, "Socket stream to vehicle interface opened successfully");
            } catch(IOException e) {
                Log.e(TAG, "Error opening streams ", e);
                disconnect();
                throw new BluetoothException();
            }
        } finally {
            mConnectionLock.writeLock().unlock();
        }
    }

    private void setAddress(String address) {
        // TODO verify this is a valid MAC address
        mAddress = address;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("deviceAddress", mAddress)
            .add("socket", mSocket)
            .toString();
    }


}
