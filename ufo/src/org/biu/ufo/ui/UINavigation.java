package org.biu.ufo.ui;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.OttoBus;
import org.biu.ufo.events.user.ShowScreenDestinationSelect;
import org.biu.ufo.events.user.ShowScreenFuelingAlternatives;
import org.biu.ufo.events.user.ShowScreenLastTrip;
import org.biu.ufo.events.user.ShowScreenMain;
import org.biu.ufo.ui.activities.MainActivity;
import org.biu.ufo.ui.activities.MainActivity_;

import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Subscribe;

@EBean
public class UINavigation {

	@Bean
	OttoBus bus;
	
	@RootContext
	Context context;
	
	public void start() {
		bus.register(this);
	}
	
	public void stop() {
		bus.unregister(this);
	}
	
	@Subscribe
	public void onShowScreenMain(ShowScreenMain msg) {
		showScreen(MainActivity.SCREEN_MAIN);
	}
	
	@Subscribe
	public void onShowScreenMain(ShowScreenDestinationSelect msg) {
		showScreen(MainActivity.SCREEN_DESTINATION);
	}
	
	@Subscribe
	public void onShowScreenMore(ShowScreenFuelingAlternatives msg) {
		showScreen(MainActivity.SCREEN_STATIONS_LIST);
	}
	
	@Subscribe
	public void onShowScreenLastTrip(ShowScreenLastTrip msg) {
		showScreen(MainActivity.SCREEN_LAST_TRIP);
	}

	private void showScreen(int screen) {
		Intent intent = MainActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_NEW_TASK).get();
		intent.putExtra(MainActivity.SELECT_SCREEN, screen);
        context.startActivity(intent);
	}
}
