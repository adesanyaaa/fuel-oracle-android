package org.biu.ufo.ui.popups;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.services.UfoMainService;
import org.biu.ufo.services.UfoMainService_;
import org.biu.ufo.ui.activities.MainActivity;
import org.biu.ufo.ui.activities.MainActivity_;
import org.biu.ufo.ui.utils.NavigationIntent;

import wei.mark.standout.StandOutWindow;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

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
	private FuelRecommendationMessage recommendation;
	private FuelRecommendationMessage popupRecommendation;
	private boolean popupShown;
    private Handler handler = new Handler();

	@Bean
	OttoBus bus;

	@RootContext
	Context context;

    @SystemService
    PowerManager pm;

	public void start() {
		currentSpeed = START_SPEED;
		recommendation = null;
		popupShown = false;
		bus.register(this);
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
		recommendation = message;
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
		return recommendation != null && recommendation.shouldFuel() && recommendation.getTopStation() != null;
	}

	private boolean hasLowSpeed() {
		return currentSpeed < 50.0;
	}

	private void showPopupIfNeededAndPossible() {
		if(!popupShown && hasGoodRecommendation() && hasLowSpeed() && isNear()) {
			showPopup();
		}
	}

	private boolean isNear() {
		Station top = getPopupRecommendation().getTopStation();
		double distance = Calculator.distance(currentLocation, top.getLocation());
		if(distance < 3)
			return true;
		return false;
	}

	private void showPopup() {
		popupRecommendation = recommendation;
		popupShown = true;
		application.getRecognizer().addListener(this);
		StandOutWindow.show(context, UfoMainService_.class, UfoMainService.SERVICE_FUEL_NEXT_ID);
	}

	public void closePopup() {
		application.getRecognizer().removeListener(this);
		
		if(popupShown) {
			popupShown = false;
			
			application.stopTextToSpeech();
			application.stopListening(MainApplication.VOICE_POPUP);
			
			StandOutWindow.close(context, UfoMainService_.class, UfoMainService.SERVICE_FUEL_NEXT_ID);
			popupRecommendation = null;
			recommendation = null;
		}
	}

	public void onPopupClick() {
		if(popupShown) {
			Station top = getPopupRecommendation().getTopStation();
			context.startActivity(NavigationIntent.getNavigationIntent(new Location(top.getLat(), top.getLng())));
			closePopup();
		}
	}
	
	public void showMore() {
		if(popupShown) {
			context.startActivity(new Intent(context, MainActivity_.class)
				 .putExtra("screen", MainActivity.RECOMMENDATIONS)
				 .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			closePopup();
		}
	}

	public FuelRecommendationMessage getPopupRecommendation() {
		return popupRecommendation;
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

		Station top = getPopupRecommendation().getTopStation();
    	application.startTextToSpeech(top.getAddress());
    	
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
