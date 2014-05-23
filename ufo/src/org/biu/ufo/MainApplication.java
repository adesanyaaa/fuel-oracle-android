package org.biu.ufo;

import org.androidannotations.annotations.EApplication;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import android.app.Application;

@EApplication
public class MainApplication extends Application {
	public static final String TAG = "MainApplication";
	
	private Tracker mAppTracker;
	
	/**
	 * return app's associated tracker 
	 * @return app's tracker
	 */
	public synchronized Tracker getTracker() {
		if (mAppTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			mAppTracker = analytics.newTracker("UA-50658697-2");;
		}
		return mAppTracker;
	}

}
