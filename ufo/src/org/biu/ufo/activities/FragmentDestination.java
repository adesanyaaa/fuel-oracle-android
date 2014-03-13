package org.biu.ufo.activities;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.db.PlacesDataStore;
import org.biu.ufo.events.DestinationSelected;
import org.biu.ufo.events.SpeechStartCommand;
import org.biu.ufo.model.Place;
import org.biu.ufo.services.*;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.fragment_destination)
// TODO http://stackoverflow.com/questions/18039429/android-speech-recognition-continuous-service
public class FragmentDestination extends Fragment {
	static final int RECOGNIZER_REQ_CODE = 1234;
	
	@Bean
	OttoBus bus;

	@Bean
	PlacesDataStore placesDataStore;
	
	private int mBindFlag;
	private Messenger mServiceMessenger;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

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
		getActivity().bindService(new Intent(getActivity(), AlwaysListeningSpeechRecognizerService_.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
		getActivity().unbindService(mConnection);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		placesDataStore.open();
		
		DestinationChooseFragment recommendFrag = new DestinationChooseFragment_();
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
    		if(frag != null && frag instanceof DestinationSearchFragment) {
    			return true;
    		}
    	}
    	return false;
	}
	
	void openSearchFragment() {
		DestinationSearchFragment searchFrag = new DestinationSearchFragment_();
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		transaction.addToBackStack(null).replace(R.id.content_frame, searchFrag).commit();
	}
	
	protected void onPlaceSelected(Place place) {
		Toast.makeText(getActivity(), place.toString(), Toast.LENGTH_LONG).show();
		bus.post(new DestinationSelected(place));
	}


}
