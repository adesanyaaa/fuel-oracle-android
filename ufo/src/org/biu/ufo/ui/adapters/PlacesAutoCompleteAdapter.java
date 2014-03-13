package org.biu.ufo.ui.adapters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.model.Place;
import org.biu.ufo.ui.widgets.PlaceItemView;
import org.biu.ufo.ui.widgets.PlaceItemView_;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

@EBean 
public class PlacesAutoCompleteAdapter extends BaseAdapter implements Filterable {
    
    private ArrayList<Place> resultList;
    
    @RootContext
    Context context;

    @Override
    public int getCount() {
    	if(resultList != null)
    		return resultList.size();
    	return 0;
    }

    @Override
    public Place getItem(int index) {
        return resultList.get(index);
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
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                	ArrayList<Place> placesList = loadPlaces(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = placesList;
                    filterResults.count = placesList.size();
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
			@Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                	resultList = (ArrayList<Place>) results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
    
	private ArrayList<Place> loadPlaces(final String query) {
		ArrayList<Place> places = new ArrayList<Place>();

		if(query.length() > 0) {
			autocomplete(places, query);
		}
		
		return places;
	}

	private ArrayList<Place> autocomplete(ArrayList<Place> places, final String query) {		
		if(query.length() > 0) {
			final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			
			try {
				List<Address> addresses = geocoder.getFromLocationName(query, 10);

				if (null != addresses) {
					for(Address address : addresses) {
						Place place = new Place(address);
						places.add(place);
					}
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} else {
			
		}
		return places;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}
}