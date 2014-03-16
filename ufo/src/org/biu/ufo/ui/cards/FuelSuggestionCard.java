package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.Card;

import org.biu.ufo.control.events.user.ShowRecommendationsMessage;
import org.biu.ufo.ui.activities.MainActivity;

import android.content.Context;
import android.view.View;

public class FuelSuggestionCard extends Card {

	public FuelSuggestionCard(Context context) {
		super(context);
		
	    //Add ClickListener
	    setOnClickListener(new OnCardClickListener() {
	        @Override
	        public void onClick(Card card, View view) {
				((MainActivity)getContext()).getBus().post(new ShowRecommendationsMessage());
	        }
	    });

	}

}
