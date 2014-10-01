package org.biu.ufo.ui.activities;

import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.R;
import org.biu.ufo.model.Place;
import org.biu.ufo.ui.adapters.PlacesAutoCompleteAdapter;
import org.biu.ufo.ui.utils.DelayedTextWatcher;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

@EFragment(R.layout.destination_search) 
class FragmentDestinationSearch extends Fragment {
	@ViewById
	EditText searchView;
	
	@ViewById
	ImageButton searchVoiceButton;

	@ViewById(android.R.id.list)
	ListView listView;
	
	@Bean
	PlacesAutoCompleteAdapter searchAdapter;
	
    @SystemService
    InputMethodManager inputMethodManager;

	FragmentDestination parent;
	
	@AfterViews
	protected void setupContent() {
		parent = (FragmentDestination) getParentFragment();
		searchVoiceButton.setOnClickListener(parent.voiceActionListener);
		
		searchView.addTextChangedListener(new DelayedTextWatcher(1000) {

			@Override
			public void afterTextChangedDelayed(Editable s) {
				constraintChanged(s.toString());
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		    	inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

				Place place = (Place) adapterView.getItemAtPosition(position);
				parent.placesDataStore.storePlace(place);
				parent.onPlaceSelected(place);
			}
		});

		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(searchAdapter);
		swingBottomInAnimationAdapter.setInitialDelayMillis(300);
		swingBottomInAnimationAdapter.setAbsListView(listView);
		listView.setAdapter(swingBottomInAnimationAdapter);
		
		searchView.setFocusableInTouchMode(true);
		searchView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					searchView.post(new Runnable() {
					    public void run() {
					    	inputMethodManager.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT);
					    }
					});
				}
			}
		});
		searchView.requestFocus();
	}
		
	protected void constraintChanged(String constraint) {
		searchAdapter.getFilter().filter(constraint);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	    switch(requestCode) {
	    case FragmentDestination.RECOGNIZER_REQ_CODE:
	        if(resultCode == Activity.RESULT_OK) {
	            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	            if(results.size() > 0) {
	            	searchView.setText(results.get(0));
	            }	            	
	        }
	        break;
	    }
	}
}