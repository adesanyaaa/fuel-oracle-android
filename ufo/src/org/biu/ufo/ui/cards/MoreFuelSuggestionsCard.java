package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.Card;

import org.biu.ufo.R;
import org.biu.ufo.ui.activities.MainActivity;
import org.biu.ufo.ui.activities.MainActivity_;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MoreFuelSuggestionsCard extends Card {	
	
	Button moreButton;
	
	
	public MoreFuelSuggestionsCard(Context context) {
		super(context, R.layout.card_more_fuel_suggestions_layout);
	
	}
	
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
    	
    	moreButton = (Button)view.findViewById(R.id.more_button);
    	moreButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getContext().startActivity(new Intent(getContext(), MainActivity_.class).putExtra("screen", MainActivity.RECOMMENDATIONS));
				
			}
		});
    }


}
