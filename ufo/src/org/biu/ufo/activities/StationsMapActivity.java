package org.biu.ufo.activities;

import java.util.HashMap;
import java.util.List;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.R;
import org.biu.ufo.rest.Client;
import org.biu.ufo.rest.MGFClient;
import org.biu.ufo.rest.Station;
import org.biu.ufo.rest.UFOClient;

import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

@EActivity(R.layout.activity_map)
public class StationsMapActivity extends FragmentActivity {
	private static final String TAG = "StationsMapActivity";	    

	@FragmentById(R.id.map)
	MapFragment mapFragment;

	@Bean(UFOClient.class)
	Client ufoClient;

	@Bean(MGFClient.class)
	Client mgfClient;

	@ViewById(R.id.modeButton)
	Button modeButton;

	@ViewById(R.id.fetchButton)
	Button fetchButton;

	@ViewById(R.id.loc1)
	Button location1Button;

	@ViewById(R.id.loc2)
	Button location2Button;

	@ViewById(R.id.loc3)
	Button location3Button;

	private enum Mode {IL, US};
	private Mode mode = Mode.US;
	private double lat;
	private double lng;
	private long currentRequestId;
	private GoogleMap map;
	private HashMap<Marker,Station> markers = new HashMap<Marker,Station>();

	@AfterViews
	void setupMap() {
		map = mapFragment.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		map.setInfoWindowAdapter(mInfoAdapter);
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
				marker.showInfoWindow();
				return true;
			}
		});

		// Initial values for testing
		onModeChange();
		onLocation1Click();
	}

	@Click(R.id.fetchButton)
	void onFetchClick() {
		++currentRequestId;
		Client client = (mode==Mode.US)? mgfClient : ufoClient;
		fetchStations(currentRequestId, client, lat, lng);
	}

	@Click(R.id.modeButton)
	void onModeChange() {
		if(mode == Mode.US) {
			mode = Mode.IL;
			modeButton.setText("Israel");
			location1Button.setText("Holon");
			location2Button.setText("Petah-Tikva");
			location3Button.setText("Haifa");
		} else {
			mode = Mode.US;
			modeButton.setText("USA");
			location1Button.setText("NYC");
			location2Button.setText("Washington");
			location3Button.setText("Brooklyn");
		}
	}

	@Click(R.id.loc1)
	void onLocation1Click() {
		if(mode == Mode.US) {
			setCurrentPosition(40.714555, -74.006588);
		} else {
			setCurrentPosition(31.99217, 34.79936);
		}
	}

	@Click(R.id.loc2)
	void onLocation2Click() {
		if(mode == Mode.US) {
			setCurrentPosition(38.909458, -77.042656);
		} else {
			setCurrentPosition(32.084972, 34.887396);
		}
	}

	@Click(R.id.loc3)
	void onLocation3Click() {
		if(mode == Mode.US) {
			setCurrentPosition(40.650522, -73.951635);
		} else {
			setCurrentPosition(32.830086, 34.974954);
		}
	}

	@Background
	void fetchStations(long requestId, Client client, double lat, double lng) {
		final List<Station> stations = client.getStations(String.valueOf(lat), String.valueOf(lng), 0.5f);
		fillMarkers(requestId, stations);
	}

	@UiThread
	void fillMarkers(long requestId, final List<Station> stations) {
		if(requestId != currentRequestId)
			return;

		map.clear();
		markers.clear();

		for(Station station : stations) {
			Marker newMarker =  map.addMarker(
					new MarkerOptions().title(station.getCompany()).position(new LatLng(station.getLat(), station.getLng()))
					);
			markers.put(newMarker, station);
		}
	}

	private void setCurrentPosition(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
		adjustMapToCurrentLocation();
	}

	private void adjustMapToCurrentLocation() {
		// Add marker for current position
		map.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).
				icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).
				title("My Location"));

		CameraPosition cmraPosition = new CameraPosition.Builder()
			.target(new LatLng(lat,lng))// Sets the center of the map to location user
			.zoom(19)                   // Sets the zoom
			.bearing(60)                // Sets the orientation of the camera to east
			.tilt(80)                   // Sets the tilt of the camera to 30 degrees
			.build();                   // Creates a CameraPosition from the builder

		map.animateCamera(CameraUpdateFactory.newCameraPosition(cmraPosition));
	}

	private InfoWindowAdapter mInfoAdapter = new InfoWindowAdapter() {

		@Override
		public View getInfoWindow(Marker marker) {
			// Use default InfoWindow frame
			return null;
		}

		// Defines the contents of the InfoWindow
		@Override
		public View getInfoContents(Marker mark) {
			// Getting view from the layout file info_window_layout
			View v = getLayoutInflater().inflate(R.layout.info_window_layout, null);
			Station info = markers.get(mark);

			// Getting reference to the TextView to set latitude
			TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);

			// Getting reference to the TextView to set longitude
			TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
			ImageView cmpVal = (ImageView) v.findViewById(R.id.cmpImage);

			String markTitle = mark.getTitle();
			String cmp ="";
			// Setting the latitude
			tvLat.setText(markTitle);
			if (info !=null) {
				tvLng.setText(info.getPrice() + "$");
				cmp = info.getCompany();
			}

			if (cmp.equalsIgnoreCase("דלק")){
				cmpVal.setImageResource(R.drawable.delek);
			} else if (cmp.equalsIgnoreCase("פז")){	
				cmpVal.setImageResource(R.drawable.paz);
			} else if (cmp.equalsIgnoreCase("סונול")){
				cmpVal.setImageResource(R.drawable.sonol);
			} else{
				cmpVal.setImageResource(R.drawable.unknown);
			}

			// Returning the view containing InfoWindow contents
			return v;
		}
	};

}
