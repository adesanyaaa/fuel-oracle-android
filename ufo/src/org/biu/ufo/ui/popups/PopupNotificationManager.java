package org.biu.ufo.ui.popups;

import static edu.cmu.pocketsphinx.Assets.syncAssets;
import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.squareup.otto.Subscribe;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

@EBean
public class PopupNotificationManager implements RecognitionListener {
	private static final double START_SPEED = 100;
    private static final String POPUP_SEARCH = "popup";

	private double currentSpeed;
	private FuelRecommendationMessage recommendation;
	private FuelRecommendationMessage popupRecommendation;
	private boolean popupShown;

	@Bean
	OttoBus bus;

	@RootContext
	Context context;

    @SystemService
    PowerManager pm;

	private TextToSpeech ttobj;
    private SpeechRecognizer recognizer;
    private WakeLock wl;

	public void start() {
		currentSpeed = START_SPEED;
		recommendation = null;
		popupShown = false;
		bus.register(this);
		
		
		loadTextToSpeech();
		loadPocketPhinx();

	}

	public void stop() {
		bus.unregister(this);
		
		destroyTextToSpeech();
		destroyPocketPhinx();
		
		if(wl != null) {
			wl.release();
			wl = null;
		}

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
		}
	}

	private void showPopup() {
		popupRecommendation = recommendation;
		popupShown = true;
		StandOutWindow.show(context, UfoMainService_.class, UfoMainService.SERVICE_FUEL_NEXT_ID);
	}

	public void closePopup() {
		if(wl != null) {
			wl.release();
			wl = null;
		}
		
		if(popupShown) {
			popupShown = false;
			
			stopTextToSpeech();
			stopListening();
			
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
	
	@UiThread(delay=10000)
	public void automaticClosing() {
		closePopup();
	}


	public void onShown() {
		startTextToSpeech();
		startListening();
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
	    wl.acquire();

		/*
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
		 */
	}
	
	private void loadTextToSpeech() {
		ttobj = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if(status != TextToSpeech.ERROR){
					ttobj.setLanguage(Locale.US);
				}				
			}
		});
	}
	
	private void destroyTextToSpeech() {
		if(ttobj !=null ){
			ttobj.stop();
			ttobj.shutdown();
			ttobj = null;
		}
	}
	
	private void startTextToSpeech() {
		if(ttobj.isLanguageAvailable(Locale.ENGLISH) != TextToSpeech.LANG_NOT_SUPPORTED) {
		    ttobj.speak("Fuel next", TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	private void stopTextToSpeech() {
		if(ttobj !=null ){
			ttobj.stop();
		}
	}
		
    void loadPocketPhinx() {
        File appDir;
        try {
            appDir = syncAssets(context);
        } catch (IOException e) {
            throw new RuntimeException("failed to synchronize assets", e);
        }
        recognizer = defaultSetup()
                .setAcousticModel(new File(appDir, "models/hmm/en-us-semi"))
                .setDictionary(new File(appDir, "models/lm/cmu07a.dic"))
                .setRawLogDir(appDir)
                .setKeywordThreshold(1e-5f)
                .getRecognizer();
        recognizer.addListener(this);
        File popupGrammar = new File(appDir, "models/grammar/popup.gram");
        recognizer.addGrammarSearch(POPUP_SEARCH, popupGrammar);
    }
    
	private void destroyPocketPhinx() {
		if(recognizer != null) {
			recognizer.stop();
			recognizer.removeListener(this);
			recognizer = null;
		}
	}

	public void startListening() {
		stopListening();
        recognizer.startListening(POPUP_SEARCH);
	}
	
	public void stopListening() {
        recognizer.stop();
	}

	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onEndOfSpeech() {
        Log.e(getClass().getSimpleName(), "onEndOfSpeech");
        if(popupShown) {
        	startListening();
        }
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResult(Hypothesis hypothesis) {
        String text = hypothesis.getHypstr();
        Log.e(getClass().getSimpleName(), "on result: " + text + ", " + hypothesis.getBestScore());
        if(text.equals("navigate")) {
        	onPopupClick();
        } else if(text.equals("more")) {
        	showMore();
        }
	}	

}
