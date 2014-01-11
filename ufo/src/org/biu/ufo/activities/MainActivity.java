package org.biu.ufo.activities;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.rest.RestService;
import org.biu.ufo.R;
import org.biu.ufo.rest.internal.mygasfeed.Client;
import org.biu.ufo.rest.internal.mygasfeed.RequestFuelType;
import org.biu.ufo.rest.internal.mygasfeed.RequestSortBy;
import org.biu.ufo.rest.internal.mygasfeed.StationsResponse;
import org.biu.ufo.services.*;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity {
	public static final String TAG = "MainActivity";
	
	@RestService
	Client stationsClient;
	
	@OptionsItem(R.id.action_settings)
	void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
		//getGasStationData();
	}

	@Background
	void getGasStationData() {
		StationsResponse response = stationsClient.getStations("45.492367", "-73.710915", 50, RequestFuelType.reg, RequestSortBy.distance);
		Log.d(TAG, "Got " + response.stations.size() + " stations");
	}
	
	@Click(R.id.start_ufo_service)
	void startMainService() {
		UfoMainService_.intent(this).start();
	}
	
	@Click(R.id.stop_ufo_service)
	void stopMainService() {
		UfoMainService_.intent(this).stop();
	}

}
