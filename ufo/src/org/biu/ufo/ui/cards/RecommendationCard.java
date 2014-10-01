package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;

import org.biu.ufo.events.control.FuelRecommendationMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.utils.UnitConverter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class RecommendationCard extends Card {

	RecommendationCardHeader header;
	RecommendationCardExpandInside expand;
	
    public RecommendationCard(Context context, 
    		FuelRecommendationMessage recommendation,
    		Station station,
    		Location currentLocation) {
    	
		super(context);
		header = new RecommendationCardHeader(getContext(), 
        		station.getAddress(), String.format("%.2f", station.getPrice()), 
        		UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency()));
        
        addCardHeader(header);
        
        expand = new RecommendationCardExpandInside(getContext(), recommendation, station, currentLocation);
        addCardExpand(expand);

	}

	//In order to allow the item to expand in list
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Example on the card
        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().setupView(getCardView());
        setViewToClickToExpand(viewToClickToExpand);
    }

	public void setCurrentLocation(Location location) {
		expand.setCurrentLocation(location);
	}
    
//    public void setDistance(double distance){
//    	expand.setStationDistance(distance);
//    }

}
