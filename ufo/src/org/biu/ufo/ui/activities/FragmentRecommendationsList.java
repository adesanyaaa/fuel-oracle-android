/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Gabriele Mariotti.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */

package org.biu.ufo.ui.activities;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.cards.RecommendationCard;

import android.support.v4.app.Fragment;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.card_expandinside_list_view)
public class FragmentRecommendationsList extends Fragment {
	@Bean
	OttoBus bus;

	@ViewById(android.R.id.list)
	CardListView listView;

	private final ArrayList<Card> cards = new ArrayList<Card>();
	private FuelRecommendationMessage fuelRecommendationMessage;
	private LocationMessage locationMessage;
	private boolean isRegistedOnBus = false;
	
	@AfterViews
	public void initializeList() {
        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
        listView.setAdapter(mCardArrayAdapter);
        isRegistedOnBus = true;
        bus.register(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(isRegistedOnBus) {
			bus.unregister(this);	
			isRegistedOnBus = false;
		}
	}
	
	@Subscribe
	public void onFuelNextRecommendation(FuelRecommendationMessage message) {
		fuelRecommendationMessage = message;
		loadCards();
	}
	
	@Subscribe
	public void onLocationMessage(LocationMessage message) {
		locationMessage = message;
		loadCards();
	}
	
    private void loadCards() {
    	if(locationMessage == null || fuelRecommendationMessage == null) {
    		return;
    	}
    	
		if(isRegistedOnBus) {
			bus.unregister(this);	
			isRegistedOnBus = false;
		}

    	cards.clear();
        
    	for(Station station : fuelRecommendationMessage.getStations()) {
            Card card = getRecommendationCard(station);
            cards.add(card);
        }
        
        if(cards.size() > 0) {
        	cards.get(0).setExpanded(true);
        }
        
        ((CardArrayAdapter)listView.getAdapter()).notifyDataSetChanged();
    }


    /**
     * This method builds a standard header with a custom expand/collpase
     * @param recommendation
     */
    private Card getRecommendationCard(Station station) {
        RecommendationCard card = new RecommendationCard(getActivity(),
        		fuelRecommendationMessage, station, locationMessage.getLocation());

        return card;
    }
    
 
}
