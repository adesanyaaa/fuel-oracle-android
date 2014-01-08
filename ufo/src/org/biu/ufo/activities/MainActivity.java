package org.biu.ufo.activities;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.biu.ufo.R;
import org.biu.ufo.services.*;

import android.app.Activity;
import android.content.Intent;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends Activity {

	@OptionsItem(R.id.action_settings)
	void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
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
