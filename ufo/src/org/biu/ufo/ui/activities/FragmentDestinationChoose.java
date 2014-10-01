package org.biu.ufo.ui.activities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.ml.KNNRouteEstimator;
import org.biu.ufo.control.utils.Calculator;
import org.biu.ufo.events.car.raw.LocationMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;
import org.biu.ufo.tracker.AnalyticsDictionary;
import org.biu.ufo.tracker.DestinationSelectedEvent;
import org.biu.ufo.tracker.TrackerEvent;
import org.biu.ufo.ui.adapters.PlacesAdapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

@SuppressLint("ValidFragment")
@EFragment(R.layout.destination_chooser) 
class FragmentDestinationChoose extends Fragment {
	public static final double radiusDistance = 0.02; 	//in KM 
	
		
	@Bean
	OttoBus bus;
	
	@ViewById
	EditText searchView;
	
	@ViewById
	ImageButton searchVoiceButton;

	@ViewById(android.R.id.list)
	ListView listView;
	
	@Bean
	KNNRouteEstimator estimator;
	
	FragmentDestination parent;
	
	@Bean
	PlacesAdapter placesAdapter;
	
	Location currentLocation = null;
	int hour = 0;
	boolean updateNeeded = true;
	
	
	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
		updateNeeded = true;
	}
	
	@UiThread
	@Subscribe
	public void onLocationMessage(LocationMessage locationMessage){
		if (updateNeeded){
			updateNeeded = false;
			currentLocation = locationMessage.location;
			hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				
			ArrayList<Double> testData = new ArrayList<Double>();

			testData.add(Double.valueOf(hour));
			testData.add(currentLocation.getLatitude());
			testData.add(currentLocation.getLongitude());
			estimator.evaluate(testData);
			
			List<Place> estimatedDestination = estimator.getTrainingListSorted();
			ArrayList<Place> places = new ArrayList<Place>();
			places.addAll(estimatedDestination);
			places.addAll(parent.placesDataStore.getAllPlaces());
					
			placesAdapter.setPlaces(getUniqueList(places));
			listView.setAdapter(placesAdapter);

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		estimator.setTrainingSet();
	}

	@AfterViews
	protected void setupContent() {
		parent = (FragmentDestination) getParentFragment();
		searchVoiceButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				parent.voiceActionListener.onClick(v);
				parent.openSearchFragment();
			}
		});
		
		// Initialize search view
		searchView.setFocusable(false);
		searchView.setInputType(InputType.TYPE_NULL);
		searchView.setClickable(false);
		searchView.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(MotionEvent.ACTION_UP == event.getAction())
					parent.openSearchFragment();
		        return false;
		    }
		});
		
		
		// Initialize list view
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				Place place = (Place) adapterView.getItemAtPosition(position);
				parent.onPlaceSelected(place);

				bus.post(new DestinationSelectedEvent(position));
			}
		});
	}
	
	
	private static boolean isUniquePlace(Place place, List<Place> places){
		
		Location first = new Location(place.getAddress().getLatitude(), place.getAddress().getLongitude());
		for (Place existingPlace: places){
			Location second = new Location(existingPlace.getAddress().getLatitude(), existingPlace.getAddress().getLongitude());
			if (Calculator.distance(first, second) <= radiusDistance){
				return false;
			}
		}
		return true;
	}
	
	private static ArrayList<Place> getUniqueList(List<Place> places){
  		ArrayList<Place> uniquePlaces = new ArrayList<Place>();
		
		for (Place place : places){
			if (isUniquePlace(place, uniquePlaces)){
				uniquePlaces.add(place);
			}
		}
		
		return uniquePlaces;
	}
	
}