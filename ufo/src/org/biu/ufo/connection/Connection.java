package org.biu.ufo.connection;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.content.Context;
import android.util.Log;

import com.openxc.sources.DataSourceException;

public abstract class Connection {
	private static final String TAG = "Connection";

	private Context mContext;
	private ConnectionCallback mCallback;

	private AtomicBoolean mRunning = new AtomicBoolean(false);
	protected final ReadWriteLock mConnectionLock = new ReentrantReadWriteLock();
	private final Condition mDeviceChanged = mConnectionLock.writeLock().newCondition();
	private ConnectTask mConnectionCheckTask;

	public Connection(Context context, ConnectionCallback callback) {
		mContext = context;
		mCallback = callback;
	}

	protected Context getContext() {
		return mContext;
	}

	public boolean start() {
		if(mRunning.compareAndSet(false, true)) {
			return true;
		}
		return false;
	}

	public boolean stop() {
		if(mRunning.compareAndSet(true, false)) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if connection is active
	 * TODO: Must override it!
	 */
	 public boolean isConnected() {
		 return isRunning();
	 }

	 /**
	  * Returns true if this source should be running, or if it should die.
	  *
	  * This is different than isConnected - they just happen to return the same
	  * thing in this base data source.
	  */
	 protected boolean isRunning() {
		 return mRunning.get();
	 }

	 /**
	  * Must have the connection lock before calling this function
	  */
	 protected void disconnected() {
		 mDeviceChanged.signal();
		 if(mCallback != null) {
			 mCallback.sourceDisconnected(this);
		 }
	 }

	 /**
	  * Must have the connection lock before calling this function
	  */
	 protected void connected() {
		 mDeviceChanged.signal();
		 if(mCallback != null) {
			 mCallback.sourceConnected(this);
		 }
	 }


	 /**
	  * If not already connected, initiate the connection and
	  * block until ready to be read.
	  *
	  * You must have the mConnectionLock locked before calling this
	  * function.
	  *
	  * @throws DataSourceException The connection is still alive, but it
	  *      returned an unexpected result that cannot be handled.
	  * @throws InterruptedException if the interrupted while blocked -- probably
	  *      shutting down.
	  */
	 protected void waitForConnection() throws InterruptedException {
		 if(!isConnected() && mConnectionCheckTask == null) {
			 mConnectionCheckTask = new ConnectTask(this);
		 }

		 while(isRunning() && !isConnected()) {
			 mConnectionLock.writeLock().lock();
			 try {
				 Log.d(TAG, "Still no device available");
				 mDeviceChanged.await();
			 } finally {
				 mConnectionLock.writeLock().unlock();
			 }
		 }

		 mConnectionCheckTask = null;
	 }

	 /**
	  * Read data from the source into the given array.
	  *
	  * No more than bytes.length bytes will be read, and there is no guarantee
	  * that any bytes will be read at all.
	  *
	  * @param bytes the destination array for bytes from the data source.
	  * @return the number of bytes that were actually copied into bytes.
	  * @throws IOException if the source is unexpectedly closed or returns an
	  *      error.
	  */
	 public abstract int read(byte[] bytes) throws IOException;

	 /**
	  * Write data.
	  *
	  * @param bytes data to write.
	  * @return true on success.
	  * @throws IOException if the connection is unexpectedly closed or returns an
	  *      error.
	  */
	 public abstract boolean write(byte[] bytes) throws IOException;

	 /**
	  * Perform any cleanup necessary to disconnect
	  */
	 protected abstract void disconnect();

	 /** Initiate a connection */
	 protected abstract void connect() throws ConnectionException;


}
