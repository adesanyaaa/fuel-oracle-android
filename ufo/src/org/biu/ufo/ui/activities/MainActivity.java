package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.biu.ufo.R;
import org.biu.ufo.services.UfoMainService_;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.WindowManager;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends FragmentActivity {
	public static final String TAG = "MainActivity";
	public static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";
	public static final String SELECT_SCREEN = "screen";
	
	public static final int SCREEN_DESTINATION = 100;
	public static final int SCREEN_MAIN = 101;
	public static final int SCREEN_STATIONS_LIST = 102;
	public static final int SCREEN_LAST_TRIP = 103;
	public static final int SCREEN_FEEDBACK = 104;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		UfoMainService_.intent(this).start();
	}

	@OptionsItem(R.id.action_settings)
	void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}
	
	@OptionsItem(R.id.action_stop_service)
	void stopService() {
		UfoMainService_.intent(this).stop();
		finish();
	}
	
	@Override
	public void onBackPressed() {
	    // if there is a fragment and the back stack of this fragment is not empty,
	    // then emulate 'onBackPressed' behaviour, because in default, it is not working
	    FragmentManager fm = getSupportFragmentManager();
	    for (Fragment frag : fm.getFragments()) {
	        if (frag != null && frag.isVisible()) {
	            FragmentManager childFm = frag.getChildFragmentManager();
	            if (childFm.getBackStackEntryCount() > 0) {
	                childFm.popBackStack();
	                return;
	            }
	        }
	    }
	    super.onBackPressed();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		int screen = getIntent().getIntExtra(SELECT_SCREEN, -1);
		if (savedInstanceState == null && screen == -1) {
			selectItem(SCREEN_DESTINATION, false, true);
		} else if(screen >= 0) {
			selectItem(screen, false, false);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		
		int screen = intent.getIntExtra(SELECT_SCREEN, -1);
		if(screen >= 0) {
			boolean addToBackStack = (screen == SCREEN_DESTINATION || screen == SCREEN_LAST_TRIP);
			selectItem(screen, addToBackStack, false);
		}
	}

	public Fragment getCurrentFragment() {
		return getSupportFragmentManager().findFragmentByTag(CURRENT_FRAGMENT);
	}

	private void selectItem(int position, boolean addToBackStack, boolean initialOpening) {
		boolean animate = false;
		Log.e(TAG, "selected screen =" + position + ". addToBackStack="+addToBackStack);
		
		// update the main content by replacing fragments
		Fragment fragment;
		switch (position) {
		case SCREEN_MAIN:
			fragment = new FragmentMain_();
			animate = true;
			break;
		case SCREEN_DESTINATION:
			fragment = new FragmentDestination_();
			break;	
		case SCREEN_STATIONS_LIST:
			fragment = new FragmentRecommendationsList_();
			break;
		case SCREEN_LAST_TRIP:
			fragment = new FragmentTripSummary_();
			break;
		default:
			fragment = null;
			break;
		}

		if(fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragment.setRetainInstance(true);
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			if(animate) {
				transaction.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
			}
			if(addToBackStack) {
				transaction.addToBackStack(null);
			} else {
				fragmentManager.popBackStack();
			}
			transaction.replace(R.id.content_frame, fragment, CURRENT_FRAGMENT).commit();			
		}
	}

}
