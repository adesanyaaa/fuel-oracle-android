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
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.ml.KNNRouteEstimator;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;
import org.biu.ufo.ui.adapters.PlacesAdapter;

import android.annotation.SuppressLint;
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
	
	@Bean
	OttoBus bus;
	
	@ViewById
	EditText searchView;
	
	@ViewById
	ImageButton searchVoiceButton;

	@ViewById(android.R.id.list)
	ListView listView;
	
//	@Bean
//	PlacesCursorAdapter historyAdapter;

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
			
			KNNRouteEstimator estimator = new KNNRouteEstimator(KNNRouteEstimator.getStaticTrainingSet());			
			ArrayList<Double> testData = new ArrayList<Double>();

			testData.add(Double.valueOf(hour));
			testData.add(currentLocation.getLatitude());
			testData.add(currentLocation.getLongitude());
			estimator.evaluate(testData);
			
			List<Place> estimatedDestination = estimator.getTrainingListSorted();
			ArrayList<Place> places = new ArrayList<Place>();
			places.addAll(estimatedDestination);
			places.addAll(parent.placesDataStore.getAllPlaces());
			placesAdapter.setPlaces(places);
			listView.setAdapter(placesAdapter);

		}
	}
	
//	private class MyFormatCountDownCallback implements CountDownFormatter {
//
//		@Override
//		public String getCountDownString(final long millisUntilFinished) {
//			if(isVisible()) {
//				int seconds = (int) Math.ceil(millisUntilFinished / 1000.0);
//
//				if (seconds > 0) {
//					return getResources().getQuantityString(R.plurals.countdown_seconds, seconds, seconds);
//				}
//				return getString(R.string.countdown_dismissing);				
//			}
//			return "";
//		}
//	}


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
//		searchView.setEnabled(false);
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
			}
		});
		
//		ContextualUndoAdapter undoAdapter = new ContextualUndoAdapter(historyAdapter,
//				R.layout.undo_row, R.id.undo_row_undobutton, 3000, R.id.undo_row_texttv, this, new MyFormatCountDownCallback());
//        ContextualUndoAdapter undoAdapter = new ContextualUndoAdapter(historyAdapter, R.layout.undo_row, R.id.undo_row_undobutton, 3000, this);
//
//		undoAdapter.setAbsListView(listView);
//		listView.setAdapter(undoAdapter);			
		
		// Load data
		//getLoaderManager().initLoader(0, null, this);
	}
	
//	@Override
//	public void deleteItem(int position) {
//		long itemId = historyAdapter.getItemId(position);
//		parent.placesDataStore.deletePlace(itemId);
//        getLoaderManager().restartLoader(0, null, this);
//	}
//
//	@Override
//	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
//		SimpleCursorLoader cursorLoader = new SimpleCursorLoader(getActivity()) {
//			@Override
//			public Cursor loadInBackground() {
//				Cursor historyCursor = parent.placesDataStore.getAllPlacesCursor(); 		
//				Cursor estimatedCursor = getEstimatedDestination();
//				if (estimatedCursor!=null){
//					MergeCursor mergedCursor = new MergeCursor(new Cursor[]{estimatedCursor, historyCursor});
//					return mergedCursor;
//				}
//				
//				return historyCursor;
//			}
//		};
//		return cursorLoader;	
//	}
//
//
//	@Override
//	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//		historyAdapter.swapCursor(data);
//	}
//
//	@Override
//	public void onLoaderReset(Loader<Cursor> loader) {
//		historyAdapter.swapCursor(null);
//	}


}