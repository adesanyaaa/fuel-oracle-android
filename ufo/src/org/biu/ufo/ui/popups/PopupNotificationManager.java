package org.biu.ufo.ui.popups;

import java.io.IOException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.services.UfoMainService;
import org.biu.ufo.services.UfoMainService_;
import org.biu.ufo.ui.utils.AnalyticsDictionary;
import org.biu.ufo.ui.utils.NavigationIntent;

import wei.mark.standout.StandOutWindow;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

@EBean
public class PopupNotificationManager {
	private static final double START_SPEED = 100;
	
	private double currentSpeed;
	private FuelRecommendationMessage recommendation;
	private FuelRecommendationMessage popupRecommendation;
	private boolean popupShown;
	
	@Bean
	OttoBus bus;

	@RootContext
	Context context;
	
	Tracker tracker;
	public void start() {
		currentSpeed = START_SPEED;
		recommendation = null;
		popupShown = false;
		bus.register(this);
		tracker = ((MainApplication)context.getApplicationContext()).getTracker();
	}
	
	public void stop() {
		bus.unregister(this);
	}
	
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		recommendation = message;
		showPopupIfNeededAndPossible();	
	}

	@Subscribe
	public void onVehicleSpeedMessage(VehicleSpeedMessage message) {
		currentSpeed = message.getSpeed();
		showPopupIfNeededAndPossible();	
	}
	
	private boolean hasGoodRecommendation() {
		return recommendation != null && recommendation.shouldFuel() && recommendation.getTopStation() != null;
	}
	
	private boolean hasLowSpeed() {
		return currentSpeed < 50.0;
	}
	
	private void showPopupIfNeededAndPossible() {
		if(!popupShown && hasGoodRecommendation() && hasLowSpeed()) {
			showPopup();
			
			tracker.setScreenName(AnalyticsDictionary.Screen.FUEL_NEXT);
			tracker.send(new HitBuilders.AppViewBuilder().build());
		}
	}
	
	private void showPopup() {
		popupRecommendation = recommendation;
		popupShown = true;
		StandOutWindow.show(context, UfoMainService_.class, UfoMainService.SERVICE_FUEL_NEXT_ID);
	}
	
	public void closePopup() {
		if(popupShown) {
			StandOutWindow.close(context, UfoMainService_.class, UfoMainService.SERVICE_FUEL_NEXT_ID);
			popupRecommendation = null;
			recommendation = null;
			popupShown = false;			
		}
	}
	
	public void onPopupClick() {
		if(popupShown) {
			Station top = getPopupRecommendation().getTopStation();
			context.startActivity(NavigationIntent.getNavigationIntent(new Location(top.getLat(), top.getLng())));
			closePopup();
			sendPopupInteractionAnalytic(true);
			
		}
	}
	
	private void sendPopupInteractionAnalytic(boolean accepted){
		
		String label = AnalyticsDictionary.Recommendation.ACCEPTED;
		
		if (!accepted){
			label = AnalyticsDictionary.Recommendation.IGNORED;
		}
		
		tracker.send(new HitBuilders.EventBuilder()
		.setCategory(AnalyticsDictionary.Recommendation.CATEGORTY)
		.setAction(AnalyticsDictionary.Recommendation.Action.RECOMMENDATION_INTERACTION)
		.setLabel(label)
		.build());
		
	}
	public FuelRecommendationMessage getPopupRecommendation() {
		return popupRecommendation;
	}

	public void playNotificationSound(){
		AssetFileDescriptor afd;
		try {
			afd = context.getAssets().openFd("popup_notification.mp3");
			MediaPlayer player = new MediaPlayer();
			player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
		    player.prepare();
		    player.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@UiThread(delay=10000)
	public void automaticClosing() {
		closePopup();
		sendPopupInteractionAnalytic(false);
	}	

}
