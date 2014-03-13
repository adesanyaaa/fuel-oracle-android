package org.biu.ufo.ui.widgets;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.R;
import org.biu.ufo.model.Place;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@EViewGroup(R.layout.destination_list_item)
public class PlaceItemView extends RelativeLayout {

    @ViewById
    TextView location_item_text;

    @ViewById
    TextView location_item_subtext;

    @ViewById
    ImageView location_item_image;
    
    public PlaceItemView(Context context) {
        super(context);
    }

    public void bind(Place place) {
    	String label = place.getLabel();
    	if(TextUtils.isEmpty(label)) {
	    	location_item_text.setText(place.toString());
	    	location_item_subtext.setVisibility(View.GONE);
    	} else {
	    	location_item_text.setText(place.getLabel());
	    	location_item_subtext.setText(place.toString());
	    	location_item_subtext.setVisibility(View.VISIBLE);
    	}
    	
    	if(place.isFavorite()) {
	    	location_item_image.setVisibility(View.VISIBLE);	    		
    	} else {
	    	location_item_image.setVisibility(View.GONE);
    	}
    }
}