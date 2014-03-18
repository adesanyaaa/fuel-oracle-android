package org.biu.ufo.ui.cards;

import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.utils.UnitConverter;

import com.google.android.gms.internal.ex;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;

public class RecommendationCard extends Card {

	RecommendationCardHeader header;
	RecommendationCardExpandInside expand;
	
    public RecommendationCard(Context context, FuelRecommendationMessage recommendation, Station station) {
		super(context);
		header = new RecommendationCardHeader(getContext(), 
        		station.getAddress(), String.format("%.2f", station.getPrice()), 
        		UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency()));
        
        addCardHeader(header);
        
        expand = new RecommendationCardExpandInside(getContext(), recommendation, station);
        addCardExpand(expand);

	}

	//In order to allow the item to expand in list
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Example on the card
        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().setupView(getCardView());
        setViewToClickToExpand(viewToClickToExpand);
    }
    
    public void setDistance(double distance){
    	expand.setStationDistance(distance);
    }

}
