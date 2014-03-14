package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardHeader.OnClickCardHeaderOtherButtonListener;

import org.biu.ufo.R;
import org.biu.ufo.model.Place;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class RouteOverviewCard extends Card {

    protected TextView mText;
    protected ImageButton mButtonOpenGPS ;
    protected Place destination;
    

    public RouteOverviewCard(Context context) {
        super(context, R.layout.card_content_destination_overview);
        init();
    }
        
    public void setDestination(Place dest) {
        this.destination = dest;
    }
    
    private void init() {
        CardHeader header = new CardHeader(getContext());
        if(destination != null && !TextUtils.isEmpty(destination.getLabel())) {
            header.setTitle("Driving " +  destination.getLabel());
        } else {
            header.setTitle("Driving");        	
        }
                
        header.setOtherButtonDrawable(R.drawable.card_menu_button_rounded_overflow);
        header.setOtherButtonVisible(true);
        header.setOtherButtonClickListener(new OnClickCardHeaderOtherButtonListener() {
			
			@Override
			public void onButtonItemClick(Card arg0, View arg1) {
				// TODO Auto-generated method stub
				
			}
		});
        addCardHeader(header);
        
        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
            	if(destination != null) {
                    String destStr = "waze://?ll=";
                    destStr += String.valueOf(destination.getAddress().getLatitude());
                    destStr += ",";
                    destStr += String.valueOf(destination.getAddress().getLongitude());
                    destStr += "&navigate=yes";
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(destStr)));            		
            	}
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        //Retrieve elements
        mText = (TextView) parent.findViewById(R.id.destination_text);

        if (mText != null && destination != null)
            mText.setText(destination.toString());

    }
}

