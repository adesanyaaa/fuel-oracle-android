package org.biu.ufo.notifications;

import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.components.RouteEstimator;
import org.biu.ufo.control.components.RouteEstimator.EstimatedRoute;
import org.biu.ufo.control.components.RouteEstimator.EstimatedRouteResult;
import org.biu.ufo.control.components.RouteEstimator_;
import org.biu.ufo.control.utils.Calculator;
import org.biu.ufo.events.car.raw.LocationMessage;
import org.biu.ufo.events.car.raw.VehicleSpeedMessage;
import org.biu.ufo.events.control.FuelRecommendationMessage;
import org.biu.ufo.events.user.ShowScreenFuelingAlternatives;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.services.UfoMainService;
import org.biu.ufo.tracker.FuelNextClickedEvent;
import org.biu.ufo.tracker.FuelNextShownEvent;
import org.biu.ufo.ui.utils.NavigationIntent;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

public class FuelNextNotification extends PopupNotification implements RecognitionListener {
	private static final double START_SPEED = 100;
	private static final double LOW_SPEED = 50.0;
	private static final double NEAR_DISTANCE_SLOW = 1.0;
	private static final double NEAR_DISTANCE_FAST = 2.0;

	private EstimatedRoute estimatedRouteToStation;
	private FuelRecommendationMessage recommendation;
	private FuelRecommendationMessage visibleRecommendation;
	private Location currentLocation;
	private double currentSpeed = START_SPEED;

	private MainApplication application;
	private OttoBus bus;
	
	public FuelNextNotification(MainApplication context, OttoBus bus) {
		super(context, UfoMainService.SERVICE_FUEL_NEXT_ID);
		this.application = context; 
		this.bus = bus;
	}

	public void onFuelRecommendationMessage(FuelRecommendationMessage recommendationMessage) {
		recommendation = recommendationMessage;
	}

	public void onLocationMessage(LocationMessage message) {
		currentLocation = message.getLocation();
	}

	public void onVehicleSpeedMessage(VehicleSpeedMessage message) {
		currentSpeed = message.getSpeed();
	}

	public void showPopupIfNeededAndPossible() {
		if(hasGoodRecommendation() && hasLowSpeed() && isNear()) {
			Station top = recommendation.getTopStation();
			RouteEstimator routeEstimator = RouteEstimator_.getInstance_(context);
			routeEstimator.getNewRouteEstimation(currentLocation, top.getLocation(), new EstimatedRouteResult() {

				@Override
				public void onEstimatedRouteResult(EstimatedRoute route) {
					estimatedRouteToStation = route;
					showPopup();
				}
			});
		}			
	}

	private boolean hasGoodRecommendation() {
		return recommendation != null && recommendation.shouldFuel() && recommendation.getTopStation() != null;
	}

	private boolean hasLowSpeed() {
		return currentSpeed < LOW_SPEED;
	}
	
	private boolean isNear() {
		if(currentLocation == null)
			return false;
		
		Station top = recommendation.getTopStation();
		double distance = Calculator.distance(currentLocation, top.getLocation());
		if(hasLowSpeed() && distance < NEAR_DISTANCE_SLOW)
			return true;
		if(!hasLowSpeed() && distance < NEAR_DISTANCE_FAST)
			return true;
		return false;
	}
	
	public void showMore() {
		bus.post(new ShowScreenFuelingAlternatives());
		closePopup();
	}

	public void speakAddress() {
		handler.removeCallbacks(automaticClosingTask);

		Station top = visibleRecommendation.getTopStation();
		if(top != null) {
	    	application.startTextToSpeech(top.getAddress());			
		}
    	
    	automaticClosing(10000);
	}

	@Override
	public void onPopupClick() {
		Station top = recommendation.getTopStation();
		Intent intent = NavigationIntent.getNavigationIntent(top.getLocation());
		context.startActivity(intent);
		closePopup();
		
		bus.post(new FuelNextClickedEvent());
	}

	@Override
	public void onShown() {
		visibleRecommendation = recommendation;
		application.startTextToSpeech("Fuel next");
		application.startListening(MainApplication.VOICE_POPUP);
		application.getRecognizer().addListener(this);
		
		bus.post(new FuelNextShownEvent());
    	automaticClosing(10000);
	}

	@Override
	public void onClosed() {
		visibleRecommendation = null;
		recommendation = null;
		estimatedRouteToStation = null;
		application.getRecognizer().removeListener(this);
		application.stopListening(MainApplication.VOICE_POPUP);
		application.stopTextToSpeech();
	}
	
	@Override
	public void onBeginningOfSpeech() {		
	}

	@Override
	public void onEndOfSpeech() {
		Log.e("TEST", "onEndOfSpeech");

        if(isPopupShown()) {
    		Log.e("TEST", "isPopupShown");

        	application.startListening(MainApplication.VOICE_POPUP);
        }		
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		Log.e("TEST", "onPartialResult");

	}

	@Override
	public void onResult(Hypothesis hypothesis) {
		Log.e("TEST", "onResult Hypothesis");

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

	@Override
	public View createView() {
		final FuelNextContentView view = FuelNextContentView_.build(context);	
		view.fillContent(this, recommendation, estimatedRouteToStation);
		return view;
	}

}
