package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.user.DestinationSelectedMessage;
import org.biu.ufo.control.events.user.PeekNewDestinationMessage;
import org.biu.ufo.control.events.user.ShowRecommendationsMessage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends FragmentActivity {
	public static final String TAG = "MainActivity";
	
	private static final int HOME = 0;
	private static final int MAIN = 1;
	private static final int DEST = 2;
	private static final int STATUS_ANALYZER = 3;
	private static final int SETTINGS = 4;
	
	private static final int RECOMMENDATIONS = 101;

	
	

	@ViewById(R.id.drawer_layout)
	DrawerLayout mDrawerLayout;

	@ViewById(R.id.left_drawer)
	ListView mDrawerList;

	@StringRes(R.string.app_name)
	String mDrawerTitle;

	@StringRes(R.string.app_name)
	String mTitle;

	@StringArrayRes(R.array.drawer_elements)
	String[] mDrawerElemetsTitles;

	@Bean
	OttoBus bus;
	
	public OttoBus getBus() {
		return bus;
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		if (savedInstanceState == null) {
			// on first time display view for first nav item
			selectItem(0);
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		bus.unregister(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@AfterViews
	protected void setupDrawer() {
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mDrawerElemetsTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
	}

	void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);        		
		}
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
	
	
	@Subscribe
	public void onPeekNewDestination(PeekNewDestinationMessage message) {
		// TODO: only if current fragment isn't destination!
		if(!(getCurrentFragment() instanceof FragmentDestination)) {			
			selectItem(DEST, true);
		}
	}
	
	@Subscribe
	public void onShowRecommendationsMessage(ShowRecommendationsMessage message) {
		// TODO: only if current fragment isn't destination!
		if(!(getCurrentFragment() instanceof FragmentRecommendationsList)) {			
			selectItem(RECOMMENDATIONS, true);
		}
	}
	
	@Subscribe
	public void onDestinationSelected(DestinationSelectedMessage message) {
		// TODO: only if current fragment isn't main!
		if(!(getCurrentFragment() instanceof FragmentMain)) {			
			selectItem(MAIN);
		}
	}

	private Fragment getCurrentFragment() {
		return getSupportFragmentManager().findFragmentByTag("CURRENT_FRAGMENT");
	}
	
	private void selectItem(int position) {
		selectItem(position, false);
	}
	
	private void selectItem(int position, boolean addToBackStack) {
		boolean animate = false;
		
		// update the main content by replacing fragments
		Fragment fragment;
		switch (position) {
		case HOME:
			fragment = new FragmentHome_();
			break;
		case MAIN:
			fragment = new FragmentMain_();
			animate = true;
			break;
		case DEST:
			fragment = new FragmentDestination_();
			break;	
		case STATUS_ANALYZER:
			fragment = new FragmentStatusAnalyzer_();
			break;
		case RECOMMENDATIONS:
			fragment = new FragmentRecommendationsList_();
			break;
		case SETTINGS:
			openSettings();
			return;
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
			transaction.replace(R.id.content_frame, fragment, "CURRENT_FRAGMENT").commit();			
		}

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		mDrawerList.setSelection(position);
//		if(position == 0) {
//			setTitle(mDrawerTitle);
//		} else {
//			setTitle(mDrawerElemetsTitles[position]);        	
//		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}

}
