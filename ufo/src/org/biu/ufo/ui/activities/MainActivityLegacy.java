package org.biu.ufo.ui.activities;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringArrayRes;
import org.androidannotations.annotations.res.StringRes;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.NearbyStationsChanged;
import org.biu.ufo.rest.Station;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivityLegacy extends FragmentActivity {
	public static final String TAG = "MainActivity";

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
	
	private List<Station> _stations;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Subscribe
	public void onStationsData(NearbyStationsChanged event) {
		if(event.getStations() != null) {
			_stations = new ArrayList<Station>(event.getStations());
		} else {
			_stations = null;
		}
	}

	@Produce
	public NearbyStationsChanged produceStationsData() {
		return new NearbyStationsChanged(_stations);
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

		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description for accessibility */
				R.string.drawer_close  /* "close drawer" description for accessibility */
				) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		selectItem(0);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		
		MenuItem toggleView = menu.findItem(R.id.action_toggle_view);
		if(toggleView != null) {
			toggleView.setVisible(!drawerOpen);
		}
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return false;
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
		getActionBar().setTitle(mTitle);
	}

	@OptionsItem(R.id.action_settings)
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

	private void selectItem(int position) {
		// update the main content by replacing fragments
		Fragment fragment;
		switch (position) {
		case 0:
			fragment = new FragmentHome_();
			break;
		case 1:
			fragment = new FragmentStatusAnalyzer_();//FragmentCarData_();
			break;
		case 2:
			fragment = new FragmentStationsMap_();
			break;	
		default:
			fragment = null;
			break;
		}

		if(fragment != null) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();        	
		}

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		if(position == 0) {
			setTitle(mDrawerTitle);
		} else {
			setTitle(mDrawerElemetsTitles[position]);        	
		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}

}
