package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

import org.biu.ufo.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BasicInfoCard extends Card {

	String title;

	public BasicInfoCard(Context context, String title) {
		super(context, R.layout.card_basic_content);
		addCardHeader(new CardHeader(context));
		this.title = title;
	}

	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		TextView msg = (TextView)view.findViewById(R.id.card_main_inner_simple_title);
		if (msg!=null){
			msg.setText(title);
		}

	}

}
