package org.biu.ufo.ui.cards;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardHeader.OnClickCardHeaderOtherButtonListener;
import it.gmariotti.cardslib.library.internal.CardThumbnail;

import org.biu.ufo.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SquareCarDataCard extends Card {
	String title;
	String line1;
	String line2;
	int thumbResId;
	
	TextView line1View;
	TextView line2View;
	
	public SquareCarDataCard(Context context, String title, int thumbResId) {
		super(context, R.layout.card_content_car_data);
		this.title = title;
		this.thumbResId = thumbResId;
        init();
	}
	
    private void init() {
        CardHeader header = new CardHeader(getContext());
//        header.setButtonOverflowVisible(true);
        header.setTitle(this.title);
//        header.setPopupMenu(R.menu.popupmain, new CardHeader.OnClickCardHeaderPopupMenuListener() {
//            @Override
//            public void onMenuItemClick(BaseCard card, MenuItem item) {
//                Toast.makeText(getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
//            }
//        });
        
      if(thumbResId > 0) {
    	  header.setOtherButtonDrawable(R.drawable.gasstation);
    	  header.setOtherButtonVisible(true);
          header.setOtherButtonClickListener(new OnClickCardHeaderOtherButtonListener() {
  			
  			@Override
  			public void onButtonItemClick(Card arg0, View arg1) {
  				// TODO Auto-generated method stub
  				
  			}
  		});

      }

        addCardHeader(header);

//        if(thumbResId > 0) {
//            ResourceThumbnail thumbnail = new ResourceThumbnail(getContext());
//            thumbnail.setDrawableResource(thumbResId);
//            addCardThumbnail(thumbnail);        	
//        }
    }
    
    public void setLine1Text(String text) {
    	line1 = text;
    	if(!TextUtils.isEmpty(line1)) {
    		line1View.setText(line1);
    		line1View.setVisibility(View.VISIBLE);
    	} else {
    		line1View.setVisibility(View.GONE);
    	}
    }
    
    public void setLine2Text(String text) {
    	line2 = text;
    	if(!TextUtils.isEmpty(line2)) {
    		line2View.setText(line2);
    		line2View.setVisibility(View.VISIBLE);
    	} else {
    		line2View.setVisibility(View.GONE);
    	}
    }
    
    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

    	line1View = (TextView) view.findViewById(R.id.content_line1);
    	line1View.setText(line1);

        line2View = (TextView) view.findViewById(R.id.content_line2);
        line2View.setText(line2);
    }

    
    class ResourceThumbnail extends CardThumbnail {

        public ResourceThumbnail(Context context) {
            super(context);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View viewImage) {


            //viewImage.getLayoutParams().width = 196;
            //viewImage.getLayoutParams().height = 196;
            if (viewImage != null) {
                if (parent!=null && parent.getResources()!=null){
                    DisplayMetrics metrics=parent.getResources().getDisplayMetrics();

                    int base = 98;

                    if (metrics!=null){
                        viewImage.getLayoutParams().width = (int)(base*metrics.density);
                        viewImage.getLayoutParams().height = (int)(base*metrics.density);
                    }else{
                        viewImage.getLayoutParams().width = 196;
                        viewImage.getLayoutParams().height = 196;
                    }
                }
            }

        }
    }

}
