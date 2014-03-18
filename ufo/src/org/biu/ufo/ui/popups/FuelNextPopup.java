package org.biu.ufo.ui.popups;

import java.io.IOException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.utils.NavigationIntent;
import org.biu.ufo.ui.utils.UnitConverter;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.squareup.otto.Subscribe;


@EService
public class FuelNextPopup extends StandOutWindow {
	@Bean
	OttoBus bus;
	
	Location location;
	FuelNextContentView view;
	
	@Override
	public String getAppName() {
		return getString(R.string.app_name);
	}

	@Override
	public int getAppIcon() {
		return R.drawable.ic_launcher;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		view = FuelNextContentView_.build(this);
		view.setStandOutWindow(this);
		frame.addView(view);
		bus.register(this);
	}

	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		int y = StandOutLayoutParams.TOP + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
		int x = StandOutLayoutParams.LEFT;
		int w = StandOutLayoutParams.WRAP_CONTENT;
		int h = StandOutLayoutParams.WRAP_CONTENT;
		StandOutLayoutParams params = new StandOutLayoutParams(id, w, h, x, y);
		params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
		return params;
	}
		
	@Override
	public boolean onTouchBody(int id, Window window, View view, MotionEvent event) {
		super.onTouchBody(id, window, view, event);
		if(event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			closePopup();
		}
		return false;
	}
	
	@Override
	public boolean onBringToFront(int id, Window window) {
		if(location != null) {
			startActivity(NavigationIntent.getNavigationIntent(location));
			closePopup();
		}
		return true;
	}
	
	@Override
	public int getFlags(int id) {
		return super.getFlags(id) |
				StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE |
				StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE |
				StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP;
	}

	@Override
	public boolean onShow(int id, Window window) {
		playNotificationSound();
		automaticClosing();
		return super.onShow(id, window);
	}
	
	@Override
	public void onDestroy() {
		bus.unregister(this);
		super.onDestroy();
	}
	
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		if(!message.shouldFuel() || message.getTopStation() == null) {
			closePopup();
			return;
		}
		
		Station station = message.getTopStation();
		
		view.popup_fuel_price.setText(String.format("%.2f", station.getPrice()));
		view.popup_fuel_price_currency.setText(UnitConverter.getResourceForPriceCurrency(station.getPriceCurrency()));

		view.popup_distance.setText(String.format("%.2f", station.getDistance()));
		view.popup_distance_unit.setText(UnitConverter.getResourceForDistanceUnit(station.getDistanceUnit()));

		view.popup_station_address.setText(station.getAddress());
		view.popup_gas_station_logo.setImageResource(UnitConverter.getResourceForStationLogo(station.getCompany()));
		
		location = new Location(station.getLat(), station.getLng());
	}

	private void playNotificationSound(){
		AssetFileDescriptor afd;
		try {
			afd = getAssets().openFd("popup_notification.mp3");
			MediaPlayer player = new MediaPlayer();
			player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
		    player.prepare();
		    player.start();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void closePopup() {
		StandOutWindow.closeAll(this, FuelNextPopup_.class);
	}
	
    @UiThread(delay=10000)
    public void automaticClosing() {
    	closePopup();
    }
    
}
