package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.SpeechStartCommand;
import org.biu.ufo.control.events.user.DestinationSelectedMessage;
import org.biu.ufo.model.Place;
import org.biu.ufo.storage.PlacesDataStore;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

@EFragment(R.layout.fragment_destination)
public class FragmentDestination extends Fragment /*implements RecognitionListener*/ {
	private static final String TAG = "FragmentDestination";
	public static final int RECOGNIZER_REQ_CODE = 1234;

	
	@App
	MainApplication application;
	@Bean
	OttoBus bus;

	@Bean
	PlacesDataStore placesDataStore;
	
	@FragmentArg("initialOpening")
	boolean initialOpening;
	boolean shouldListen;
	
	OnClickListener voiceActionListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			try {
				startActivityForResult(intent, RECOGNIZER_REQ_CODE);
			} catch (ActivityNotFoundException a) {
			    Toast.makeText(getActivity(), "Oops! Your device doesn't support Speech to Text",Toast.LENGTH_SHORT).show();
			}				
		}
	};
	
	@Subscribe
	public void onStartListening(SpeechStartCommand cmd) {
		if(!isInSearchMode()) {
			openSearchFragment();
		}
		voiceActionListener.onClick(null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
	}
		
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		closeVoice();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		placesDataStore.open();
		
		FragmentDestinationChoose recommendFrag = new FragmentDestinationChoose_();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_out_up, R.anim.slide_in_up);
		transaction.replace(R.id.content_frame, recommendFrag).commit();
		
		initializeVoice();
	}

	@UiThread(delay=2000)
	public void initializeVoice() {
		if(initialOpening && isVisible() && !isInSearchMode()) {
			Log.e(TAG, "initializeVoice");
			application.startTextToSpeech("What's your destination?");
//			shouldListen = true;
//			application.getRecognizer().addListener(this);
//			application.startListening(MainApplication.VOICE_DESTINATION);
//			autoCloseVoice();
		}
	}
	
//	@UiThread(delay=10000)
//	public void autoCloseVoice() {
//		closeVoice();
//	}
//	
//	public void closeVoice() {
//		shouldListen = false;
//		application.getRecognizer().removeListener(this);
//		application.stopListening(MainApplication.VOICE_DESTINATION);
//	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		placesDataStore.close();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
    	for(Fragment frag : getChildFragmentManager().getFragments()) {
    		if(frag != null) {
        		frag.onActivityResult(requestCode, resultCode, data);    			
    		}
    	}
	}
		
	private boolean isInSearchMode() {
    	for(Fragment frag : getChildFragmentManager().getFragments()) {
    		if(frag != null && frag instanceof FragmentDestinationSearch) {
    			return true;
    		}
    	}
    	return false;
	}
	
	void openSearchFragment() {
		FragmentDestinationSearch searchFrag = new FragmentDestinationSearch_();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		transaction.addToBackStack(null).replace(R.id.content_frame, searchFrag).commit();
	}
	
	protected void onPlaceSelected(Place place) {
//		Toast.makeText(getActivity(), place.toString(), Toast.LENGTH_LONG).show();
		bus.post(new DestinationSelectedMessage(place));
	}

//	@Override
//	public void onBeginningOfSpeech() {
//		// TODO Auto-generated method stub
//		Log.e(TAG, "onBeginningOfSpeech");
//	}
//
//	@Override
//	public void onEndOfSpeech() {
//		// TODO Auto-generated method stub
//		Log.e(TAG, "onEndOfSpeech");
//		if(shouldListen && application.getRecognizer().getSearchName().equals(MainApplication.VOICE_DESTINATION)) {
//			application.startListening(MainApplication.VOICE_DESTINATION);
//		} else { 
//			closeVoice();
//		}
//	}
//
//	@Override
//	public void onPartialResult(Hypothesis hypothesis) {
//		// TODO Auto-generated method stub
//		String result = hypothesis.getHypstr();
//		Log.e(TAG, "PARTIAL:" + result);
//
//	}
//
//	@Override
//	public void onResult(Hypothesis hypothesis) {
//		// TODO Auto-generated method stub
//		String result = hypothesis.getHypstr();
//		Log.e(TAG, result);
//		if(!isInSearchMode()) {
//			if(result.equals("home") || result.equals("work")) {
////				closeVoice();
//			}
//		}
//	}

}
