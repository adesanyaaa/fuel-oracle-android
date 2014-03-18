package org.biu.ufo.ui.popups;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.R;
import org.biu.ufo.ui.activities.MainActivity;
import org.biu.ufo.ui.activities.MainActivity_;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@EViewGroup(R.layout.popup_fuel_next_layout)
public class FuelNextContentView extends RelativeLayout {

	@ViewById
	TextView popup_fuel_price;
	
	@ViewById
	TextView popup_fuel_price_currency;
	
	@ViewById
	TextView popup_distance;
	
	@ViewById
	TextView popup_distance_unit;

	@ViewById
	TextView popup_station_address;
	
	@ViewById
	ImageView popup_gas_station_logo;

	@ViewById
	Button popup_more_button;
	
	private FuelNextPopup window;
	
	public FuelNextContentView(Context context) {
		this(context, null);
	}

	public FuelNextContentView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setStandOutWindow(FuelNextPopup window) {
		this.window = window;
	}
		
	@Click(R.id.popup_more_button)
	public void showMoreButton() {		
		getContext().startActivity(new Intent(getContext(), MainActivity_.class).putExtra("screen", MainActivity.RECOMMENDATIONS)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		window.closePopup();
	}
}
