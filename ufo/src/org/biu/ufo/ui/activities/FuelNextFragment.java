package org.biu.ufo.ui.activities;

import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.component.CardHeaderView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;

import android.support.v4.app.Fragment;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.card_layout)
public class FuelNextFragment extends Fragment {
	
	@Bean
	OttoBus bus;

	@ViewById
	CardHeaderView card_header_layout;
	
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
	
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		if(!message.shouldFuel()) {
			getActivity().finish();
			return;
		}
		
		CardHeader cardHeader = new CardHeader(getActivity());
		cardHeader.setTitle(message.getTopStation().getAddress());
		card_header_layout.addCardHeader(cardHeader);
	}

}
