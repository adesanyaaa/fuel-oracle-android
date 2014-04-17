package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedDestinationMessage;
import org.biu.ufo.control.events.user.DestinationSelectedMessage;
import org.biu.ufo.control.events.user.PeekNewDestinationMessage;
import org.biu.ufo.control.events.user.ShowRecommendationsMessage;
import org.biu.ufo.services.UfoMainService_;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;

import com.squareup.otto.Subscribe;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends FragmentActivity /* implements RecognitionListener */{
	public static final String TAG = "RealMainActivity";
	
	public static final int DEST = 0;
	public static final int MAIN = 1;
	public static final int RECOMMENDATIONS = 101;

//    private static final String KWS_SEARCH_NAME = "wakeup";
//    private static final String KEYPHRASE = "navigate";
//    private static final String POPUP_SEARCH = "popup";
    
	@Bean
	OttoBus bus;
	
//    private SpeechRecognizer recognizer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// TODO: make sure not opening the same shit twice!
		UfoMainService_.intent(this).start();
		
//		// PocketPhinix test
//		loadPocketPhinx();
	}
	
//    void loadPocketPhinx() {
//        File appDir;
//
//        try {
//            appDir = syncAssets(getApplicationContext());
//        } catch (IOException e) {
//            throw new RuntimeException("failed to synchronize assets", e);
//        }
//
//        recognizer = defaultSetup()
//                .setAcousticModel(new File(appDir, "models/hmm/en-us-semi"))
//                .setDictionary(new File(appDir, "models/lm/cmu07a.dic"))
//                .setRawLogDir(appDir)
//                .setKeywordThreshold(1e-5f)
//                .getRecognizer();
//
//        recognizer.addListener(this);
//        
////        recognizer.addKeywordSearch(POPUP_SEARCH, KEYPHRASE);
//        File popupGrammar = new File(appDir, "models/grammar/popup.gram");
//        recognizer.addGrammarSearch(POPUP_SEARCH, popupGrammar);
//        switchSearch(POPUP_SEARCH);
//
//    }
//    
//    private void switchSearch(String searchName) {
//        recognizer.stop();
//        recognizer.startListening(searchName);
//    }
//
//	@Override
//	public void onBeginningOfSpeech() {
//        Log.e(getClass().getSimpleName(), "onBeginningOfSpeech");
//	}
//
//	@Override
//	public void onEndOfSpeech() {
//        Log.e(getClass().getSimpleName(), "onEndOfSpeech");
//
//		switchSearch(POPUP_SEARCH);
//	}
//
//	@Override
//	public void onPartialResult(Hypothesis hypothesis) {
//        String text = hypothesis.getHypstr();
//        //Log.e(getClass().getSimpleName(), "on partial: " + text);
//		
//	}
//
//	@Override
//	public void onResult(Hypothesis hypothesis) {
//		
//        String text = hypothesis.getHypstr();
//        Log.e(getClass().getSimpleName(), "on result: " + text + ", " + hypothesis.getBestScore());
//		
//	}
//

	@Override
	protected void onPause() {
		super.onPause();
		bus.unregister(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@OptionsItem(R.id.action_settings)
	void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}
	
//	@OptionsItem(R.id.action_start_service)
//	void startService() {
//		UfoMainService_.intent(this).start();
//	}
	
	@OptionsItem(R.id.action_stop_service)
	void stopService() {
		UfoMainService_.intent(this).stop();
		finish();
	}
	
	@Override
	public void onBackPressed() {
	    // if there is a fragment and the back stack of this fragment is not empty,
	    // then emulate 'onBackPressed' behaviour, because in default, it is not working
	    FragmentManager fm = getSupportFragmentManager();
	    for (Fragment frag : fm.getFragments()) {
	        if (frag != null && frag.isVisible()) {
	            FragmentManager childFm = frag.getChildFragmentManager();
	            if (childFm.getBackStackEntryCount() > 0) {
	                childFm.popBackStack();
	                return;
	            }
	        }
	    }
	    super.onBackPressed();
	}

	@Subscribe
	public void onPeekNewDestination(PeekNewDestinationMessage message) {
		// TODO: only if current fragment isn't destination!
		if(!(getCurrentFragment() instanceof FragmentDestination)) {			
			selectItem(DEST, true);
		}
	}
	
	@Subscribe
	public void onShowRecommendationsMessage(ShowRecommendationsMessage message) {
		// TODO: only if current fragment isn't destination!
		if(!(getCurrentFragment() instanceof FragmentRecommendationsList)) {			
			selectItem(RECOMMENDATIONS, true);
		}
	}
	
	
	@Subscribe
	public void onEstimatedDestinationMessage(EstimatedDestinationMessage message) {
		// TODO: only if current fragment isn't main!
		int screen = getIntent().getIntExtra("screen", -1);
		if(screen == -1) {
			if(!(getCurrentFragment() instanceof FragmentMain)) {			
				selectItem(MAIN);
			}
		}
	}
	
	@Subscribe
	public void onDestinationSelected(DestinationSelectedMessage message) {
		// TODO: only if current fragment isn't main!
		if(!(getCurrentFragment() instanceof FragmentMain)) {			
			selectItem(MAIN);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		int screen = getIntent().getIntExtra("screen", -1);
		
		if (savedInstanceState == null && screen == -1) {
			// on first time display view for first nav item
			selectItem(DEST);
		} else if(screen >= 0) {
			selectItem(screen);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		int screen = intent.getIntExtra("screen", -1);
		if(screen >= 0) {
			boolean addToBackStack = (screen == RECOMMENDATIONS);
			selectItem(screen, addToBackStack);
		}
	}

	private Fragment getCurrentFragment() {
		return getSupportFragmentManager().findFragmentByTag("CURRENT_FRAGMENT");
	}
	
	private void selectItem(int position) {
		selectItem(position, false);
	}
	
	private void selectItem(int position, boolean addToBackStack) {
		boolean animate = false;
		Log.e(TAG, "selected screen =" + position + ". addToBackStack="+addToBackStack);
		
		// update the main content by replacing fragments
		Fragment fragment;
		switch (position) {
		case MAIN:
			fragment = new FragmentMain_();
			animate = true;
			break;
		case DEST:
			fragment = new FragmentDestination_();
			break;	
		case RECOMMENDATIONS:
			fragment = new FragmentRecommendationsList_();
			break;
		default:
			fragment = null;
			break;
		}

		if(fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragment.setRetainInstance(true);
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			if(animate) {
				transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
			}
			if(addToBackStack) {
				transaction.addToBackStack(null);
			} else {
				fragmentManager.popBackStack();
			}
			transaction.replace(R.id.content_frame, fragment, "CURRENT_FRAGMENT").commit();			
		}
	}

	public OttoBus getBus() {
		return bus;
	}

}
