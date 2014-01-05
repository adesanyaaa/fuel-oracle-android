package org.biu.ufo.services;


import android.app.Service;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.openxc.sources.WakeLockManager;

/**
 * 
 * @author Roee Shlomo
 *
 */
public abstract class BoundedWorkerService extends Service {
	private final static String TAG = "BoundedWorkerService";

	private volatile Looper mServiceLooper;
	private volatile Handler mServiceHandler;
	private Handler mMainThreadHandler;
	protected WakeLockManager mWakeLocker;

	private String mName;

	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public BoundedWorkerService(String name) {
		super();
		mName = name;
	}

	@Override
	public void onCreate() {
		// TODO: It would be nice to have an option to hold a partial wakelock
		// during processing, and to have a static startService(Context, Intent)
		// method that would launch the service & hand off a wakelock.

		super.onCreate();

		HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new Handler(mServiceLooper);
		mMainThreadHandler = new Handler();

		mWakeLocker = new WakeLockManager(this, TAG);

	}

	public void runOnForground(Runnable task) {
		mMainThreadHandler.post(task);
	}

	public void runOnBackground(Runnable task) {
		mServiceHandler.post(task);
	}

	@Override
	public void onDestroy() {
		mServiceLooper.quit();
	}

}

