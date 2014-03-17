package org.biu.ufo.ui.activities;

import it.gmariotti.cardslib.library.view.CardView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;

import android.support.v4.app.Fragment;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.popup_layout)
public class FuelNextFragment extends Fragment {
	
	@Bean
	OttoBus bus;

	@ViewById
	CardView popup_card_view;
	
	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}

	@AfterViews
	public void initialize() {
		// Maybe nothing
	}
	
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		if(!message.shouldFuel()) {
			getActivity().finish();
			return;
		}
		
		// Initialize Card and set on popup_card_view
	}

}
