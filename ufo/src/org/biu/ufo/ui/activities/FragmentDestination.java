package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.biu.ufo.MainApplication;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.events.user.DestinationSelectedMessage;
import org.biu.ufo.events.user.ShowScreenMain;
import org.biu.ufo.model.Place;
import org.biu.ufo.storage.PlacesDataStore;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

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
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		placesDataStore.open();
		
		FragmentDestinationChoose recommendFrag = new FragmentDestinationChoose_();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_out_up, R.anim.slide_in_up);
		transaction.replace(R.id.content_frame, recommendFrag).commit();
	}
		
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
		bus.post(new DestinationSelectedMessage(place));
		bus.post(new ShowScreenMain());
	}

}
