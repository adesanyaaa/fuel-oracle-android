package org.biu.ufo.ui.adapters;

import org.androidannotations.annotations.EBean;
import org.biu.ufo.model.Place;
import org.biu.ufo.storage.PlacesDataStore;
import org.biu.ufo.ui.widgets.PlaceItemView;
import org.biu.ufo.ui.widgets.PlaceItemView_;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

@EBean 
public class PlacesCursorAdapter extends CursorAdapter {
    
    public PlacesCursorAdapter(Context context) {
		super(context, null, true);
	}

    public void init(Cursor c) {
        this.changeCursor(c);
    }
    
    @Override
    public Object getItem(int position) {
    	getCursor().moveToPosition(position);
		return PlacesDataStore.cursorToPlace(getCursor());
    }
    
    @Override
    public long getItemId(int position) {
        Cursor cursor = getCursor();
        if(cursor != null && cursor.moveToPosition(position))
        	return cursor.getLong(cursor.getColumnIndex("_id"));
        return -1;
    }

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Place place = PlacesDataStore.cursorToPlace(cursor);
		PlaceItemView itemView = (PlaceItemView) view;
		itemView.bind(place);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return PlaceItemView_.build(context);
	}
    
}

//@EBean 
//public class PlacesCursorAdapter extends CardCursorAdapter  {
//
//	public PlacesCursorAdapter(Context context) {
//		super(context, null, true);
//	}
//
//	public void init(Cursor c) {
//		this.changeCursor(c);
//	}
//
//	@Override
//	public long getItemId(int position) {
//		Cursor cursor = getCursor();
//		if(cursor.moveToPosition(position))
//			return cursor.getLong(cursor.getColumnIndex("_id"));
//		return -1;
//	}
//
//	@Override
//	protected Card getCardFromCursor(Cursor cursor) {
//		Place place = PlacesDataStore.cursorToPlace(cursor);
//
//		Card card = new Card(getContext());
//		card.setId(cursor.getString(cursor.getColumnIndex("_id")));
//		
//		//Create a CardHeader
//		CardHeader header = new CardHeader(getContext());
//
//		//Set the header title
//		//      header.setTitle(place.getLabel());
//		header.setTitle(place.toString());
//		header.setPopupMenu(R.menu.main, new CardHeader.OnClickCardHeaderPopupMenuListener() {
//			@Override
//			public void onMenuItemClick(BaseCard card, MenuItem item) {
//				Toast.makeText(getContext(), "Click on card="+card.getId()+" item=" +  item.getTitle(), Toast.LENGTH_SHORT).show();
//			}
//		});
//
//		//Add Header to card
//		card.addCardHeader(header);
//
//		if(place.isFavorite()) {
//			CardThumbnail thumb = new CardThumbnail(getContext());
//			thumb.setDrawableResource(android.R.drawable.star_off);
//			card.addCardThumbnail(thumb);        	
//		}
//
//		card.setOnClickListener(new Card.OnCardClickListener() {
//			@Override
//			public void onClick(Card card, View view) {
//				Toast.makeText(getContext(), "Card id=" + card.getId() + " Title=" + card.getTitle(), Toast.LENGTH_SHORT).show();
//			}
//		});
//
//
//		return card;
//	}
//
//}