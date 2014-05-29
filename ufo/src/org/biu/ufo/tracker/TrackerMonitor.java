package org.biu.ufo.tracker;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;

import com.squareup.otto.Subscribe;

@EBean
public class TrackerMonitor {
	@Bean 
	OttoBus bus;
	
	@App
	MainApplication application;
	
	Tracker tracker;
	
	public void start() {
		bus.register(this);
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
		tracker = analytics.newTracker("UA-50658697-2");;
	}
	
	public void stop() {
		bus.unregister(this);
	}
	
	@Subscribe
	public void onTrackerEvent(TrackerEvent e) {
		tracker.send(e.getData());
	}
	
	@Subscribe
	public void onGPSOpenedEvent(GPSOpenedEvent e) {
		tracker.send(new HitBuilders.EventBuilder()
		.setCategory(AnalyticsDictionary.Navigation.CATEGORY)
		.setAction(AnalyticsDictionary.Navigation.Action.OPEN_GPS)
		.build());
	}

	@Subscribe
	public void onDestinationSelectedEvent(DestinationSelectedEvent e) {
		tracker.send(new HitBuilders.EventBuilder()
		.setCategory(AnalyticsDictionary.Navigation.CATEGORY)
		.setAction(AnalyticsDictionary.Navigation.Action.RECOMMENDATION_OPTION)
		.setLabel(AnalyticsDictionary.Navigation.POSITION)
		.setValue(e.getPos())
		.build());
	}
	
	@Subscribe
	public void onFuelNextClickedEvent(FuelNextClickedEvent e) {
		tracker.send(new HitBuilders.EventBuilder()
		.setCategory(AnalyticsDictionary.Recommendation.CATEGORTY)
		.setAction(AnalyticsDictionary.Recommendation.Action.RECOMMENDATION_INTERACTION)
		.setLabel(AnalyticsDictionary.Recommendation.ACCEPTED)
		.build());
	}
	
	@Subscribe
	public void onFuelNextShownEvent(FuelNextShownEvent e) {
		tracker.send(new HitBuilders.EventBuilder()
		.setCategory(AnalyticsDictionary.Recommendation.CATEGORTY)
		.setAction(AnalyticsDictionary.Recommendation.Action.DISPLAY_POPUP_FUEL_NEXT)
		.build());
	}
	
	@Subscribe
	public void onScreenDisplayEvent(ScreenDisplayEvent e) {
		tracker.setScreenName(e.getScreen());
		tracker.send(new HitBuilders.AppViewBuilder().build());
	}

}
