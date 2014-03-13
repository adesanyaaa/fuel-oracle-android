package org.biu.ufo.car.obd.connection;

import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

class ConnectTask extends TimerTask {
	private static final int RECONNECTION_ATTEMPT_WAIT_TIME_S = 10;

	private Connection mConnection;
	private Timer mTimer = new Timer();

	public ConnectTask(Connection connection) {
		mConnection = connection;
		mTimer.schedule(this, 0, RECONNECTION_ATTEMPT_WAIT_TIME_S * 1000);
	}

	public void run() {
		if(!mConnection.isRunning() || mConnection.isConnected()) {
			return;
		}

		try {
			mConnection.connect();
		} catch(ConnectionException e) {
			Log.i(mConnection.toString(), "Unable to connect to source, trying again in " +
					RECONNECTION_ATTEMPT_WAIT_TIME_S + "s");
			Log.d(mConnection.toString(), "Unable to connect because of exception", e);
		}

		if(mConnection.isConnected()) {
			mTimer.cancel();
		}
	}
}
