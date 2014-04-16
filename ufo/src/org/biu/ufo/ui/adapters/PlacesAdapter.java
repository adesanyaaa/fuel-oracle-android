package org.biu.ufo.ui.adapters;

import java.util.ArrayList;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.model.Place;
import org.biu.ufo.ui.widgets.PlaceItemView;
import org.biu.ufo.ui.widgets.PlaceItemView_;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

@EBean 
public class PlacesAdapter extends BaseAdapter {
    
    private ArrayList<Place> placesList;
    
    @RootContext
    Context context;

    @Override
    public int getCount() {
    	if(placesList != null)
    		return placesList.size();
    	return 0;
    }

    public void setPlaces(ArrayList<Place> placesList){
    	this.placesList = placesList;
    }
    
    @Override
    public Place getItem(int index) {
        return placesList.get(index);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlaceItemView placeItemView;
        if (convertView == null) {
            placeItemView = PlaceItemView_.build(context);
        } else {
            placeItemView = (PlaceItemView) convertView;
        }
        
        placeItemView.bind(getItem(position));
        return placeItemView;
    }
 

	@Override
	public long getItemId(int position) {
		return position;
	}
}