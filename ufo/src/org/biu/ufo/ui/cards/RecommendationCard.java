package org.biu.ufo.ui.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;

public class RecommendationCard extends Card {

    public RecommendationCard(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

    //In order to allow the item to expand in list
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Example on the card
        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().setupView(getCardView());
        setViewToClickToExpand(viewToClickToExpand);
    }

}
