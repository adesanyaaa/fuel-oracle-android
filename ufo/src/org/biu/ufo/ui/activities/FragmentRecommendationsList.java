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
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.cards.RecommendationCard;
import org.biu.ufo.ui.cards.RecommendationCardExpandInside;
import org.biu.ufo.ui.cards.RecommendationCardHeader;
import org.biu.ufo.ui.utils.UnitConverter;

import android.support.v4.app.Fragment;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.card_expandinside_list_view)
public class FragmentRecommendationsList extends Fragment {
	@Bean
	OttoBus bus;

	@ViewById(android.R.id.list)
	CardListView listView;

	private final ArrayList<Card> cards = new ArrayList<Card>();

	@AfterViews
	public void initializeList() {
        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(getActivity(), cards);
        listView.setAdapter(mCardArrayAdapter);
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
	
	@Subscribe
	public void onFuelNextRecommendation(FuelRecommendationMessage message) {
		loadCards(message);
	}
	
    private void loadCards(FuelRecommendationMessage message) {
    	cards.clear();
        
    	for(Station station : message.getStations()) {
            Card card = getRecommendationCard(message, station);
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
    private Card getRecommendationCard(FuelRecommendationMessage recommendation, Station station) {
        RecommendationCard card = new RecommendationCard(getActivity());

        // Header
        RecommendationCardHeader header = new RecommendationCardHeader(getActivity());
        header.setTitle(station.getCompany());
        header.setPrice(String.format("%.2f", station.getPrice()));
        header.setPriceCurrencyResId(UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency()));
        card.addCardHeader(header);

        // Expand area
        RecommendationCardExpandInside expand = new RecommendationCardExpandInside(getActivity());
        expand.setLocation(new Location(station.getLat(), station.getLng()));
        expand.setStationAddress(station.getAddress());
        expand.setStationDistance(station.getDistance());
        expand.setFuelCostCurrencyResId(UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency()));
        expand.setStationDistanceUnitResId(UnitConverter.getResourceForDistanceUnit(station.getDistanceUnit()));
        expand.setFuelMeasurementResId(UnitConverter.getResourceForCapacityUnit(station.getCapacityUnit()));        
        double fuelAmount = recommendation.getFuelAmount(station.getCapacityUnit());
        expand.setFuelAmount(fuelAmount);
        expand.setFuelTotalCost(fuelAmount * station.getPrice());
        card.addCardExpand(expand);

        return card;
    }
    
 
}
