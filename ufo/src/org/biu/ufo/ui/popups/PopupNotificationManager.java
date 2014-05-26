package org.biu.ufo.ui.popups;

import java.util.ArrayList;
import java.util.Stack;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.events.analyzer.alert.AccelerationAlertMessage;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.notification.INotification;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.services.UfoMainService;
import org.biu.ufo.services.UfoMainService_;
import org.biu.ufo.ui.activities.MainActivity;
import org.biu.ufo.ui.activities.MainActivity_;
import org.biu.ufo.ui.utils.AnalyticsDictionary;
import org.biu.ufo.ui.utils.NavigationIntent;

import wei.mark.standout.StandOutWindow;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Subscribe;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

@EBean
public class PopupNotificationManager implements RecognitionListener {
	private static final double START_SPEED = 100;

    @App
    MainApplication application;
    
    private Location currentLocation;
	private double currentSpeed;
	private ArrayList<INotification> notifications;
	private INotification popupNotification;
	private boolean popupShown;
    private Handler handler = new Handler();
	
	private Tracker tracker;
	
	@Bean
	OttoBus bus;

	@RootContext
	Context context;

    @SystemService
    PowerManager pm;

	public void start() {
		currentSpeed = START_SPEED;
		notifications = new ArrayList<INotification>();
		popupShown = false;
		bus.register(this);
		tracker = ((MainApplication)context.getApplicationContext()).getTracker();
	}

	public void stop() {
		bus.unregister(this);
		handler.removeCallbacks(automaticClosingTask);

		closePopup();
		
		application.stopTextToSpeech();
		application.stopListening(MainApplication.VOICE_POPUP);
		
	}
	
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		notifications.add(message);
		showPopupIfNeededAndPossible();	
	}
	
	@Subscribe
	public void onFuelRecommendationMessage(AccelerationAlertMessage message) {
		notifications.add(message);
		showPopupIfNeededAndPossible();	
	}

	@Subscribe
	public void onLocationMessage(LocationMessage message) {
		currentLocation = message.getLocation();
		showPopupIfNeededAndPossible();	
	}

	@Subscribe
	public void onVehicleSpeedMessage(VehicleSpeedMessage message) {
		currentSpeed = message.getSpeed();
		showPopupIfNeededAndPossible();	
	}

	private boolean hasGoodRecommendation() {
		//returns if first notification is FuelRecommendationMessage and has good recommendation
		return notifications.size() != 0 &&
				notifications.get(0) instanceof FuelRecommendationMessage &&
				((FuelRecommendationMessage)notifications.get(0)).shouldFuel() && 
				((FuelRecommendationMessage)notifications.get(0)).getTopStation() != null;
	}

	private boolean hasAlert() {
		return notifications.size() != 0 &&
				notifications.get(0) instanceof AccelerationAlertMessage;
	}
	
	private boolean hasLowSpeed() {
		return currentSpeed < 50.0;
	}

	private void showPopupIfNeededAndPossible() {
		//fuel next notification
		if(!popupShown && hasGoodRecommendation() && hasLowSpeed() && isNear()) {
			showPopup();
		}
		
		//high acceleration notification
		if(!popupShown && hasAlert()) {
			showPopup();
		}
	}

	private boolean isNear() {
		//not FuelRecommendationMessage?
		if (!(notifications.get(0) instanceof FuelRecommendationMessage)){
			return false;
		}
		FuelRecommendationMessage recommendation = (FuelRecommendationMessage)notifications.get(0);
		Station top = recommendation.getTopStation();
		double distance = Calculator.distance(currentLocation, top.getLocation());
		if(distance < 3)
			return true;
		return false;
	}

	private void showPopup() {
		popupNotification = notifications.get(0);
		popupShown = true;
		application.getRecognizer().addListener(this);
		
		//TODO: handle non SERVICE_FUEL_NEXT_ID -> HIGH_ACCELERATION_ID
		StandOutWindow.show(context, UfoMainService_.class, UfoMainService.SERVICE_FUEL_NEXT_ID);
		
		tracker.setScreenName(AnalyticsDictionary.Screen.FUEL_NEXT);
		tracker.send(new HitBuilders.AppViewBuilder().build());

	}

	public void closePopup() {
		application.getRecognizer().removeListener(this);
		
		if(popupShown) {
			popupShown = false;
			
			application.stopTextToSpeech();
			application.stopListening(MainApplication.VOICE_POPUP);
			
			//TODO: handle non SERVICE_FUEL_NEXT_ID -> HIGH_ACCELERATION_ID
			StandOutWindow.close(context, UfoMainService_.class, UfoMainService.SERVICE_FUEL_NEXT_ID);
			popupNotification = null;
			notifications.remove(0);
		}
	}

	public void onPopupClick() {
		if(popupShown) {
			INotification notification = getPopupRecommendation();
			if (notification instanceof FuelRecommendationMessage){
				Station top = ((FuelRecommendationMessage)notification).getTopStation();
				context.startActivity(NavigationIntent.getNavigationIntent(new Location(top.getLat(), top.getLng())));
			}else if (notification instanceof AccelerationAlertMessage){
				//TODO
			}
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

	public void showMore() {
		if(popupShown) {
			context.startActivity(new Intent(context, MainActivity_.class)
				 .putExtra("screen", MainActivity.RECOMMENDATIONS)
				 .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			closePopup();
			
			tracker.setScreenName(AnalyticsDictionary.Screen.MORE_RECOMMENDATIONS);
			tracker.send(new HitBuilders.AppViewBuilder().build());
		}
	}

	public INotification getPopupRecommendation() {
		return popupNotification;
	}
	
	public void automaticClosing() {
        handler.postDelayed(automaticClosingTask, 10000);
	}

	private Runnable automaticClosingTask = new Runnable() {
        @Override
        public void run() {
    		closePopup();
        }
    };


	public void onShown() {
		//TODO: displaying High Acceleration notification  
		//application.startTextToSpeech("Slow down");
		//{no need for application.startListening(MainApplication.VOICE_POPUP);}
		
		application.startTextToSpeech("Fuel next");
		application.startListening(MainApplication.VOICE_POPUP);
	}
	
	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndOfSpeech() {
        Log.e(getClass().getSimpleName(), "onEndOfSpeech");
        if(popupShown) {
        	application.startListening(MainApplication.VOICE_POPUP);
        }
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		
	}

	@UiThread
	public void speakAddress() {
		handler.removeCallbacks(automaticClosingTask);
		INotification notification = getPopupRecommendation();
		if (notification instanceof FuelRecommendationMessage){
			Station top = ((FuelRecommendationMessage)notification).getTopStation();
	    	application.startTextToSpeech(top.getAddress());
	    		
		}
		
    	automaticClosing();
	}

	@Override
	public void onResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();
        Log.e(getClass().getSimpleName(), "on result: " + text + ", " + hypothesis.getBestScore());
        if(text.equals("navigate")) {
        	onPopupClick();
        } else if(text.equals("more")) {
        	showMore();
        } else if(text.equals("where")) {
        	speakAddress();
        }
	}

}
