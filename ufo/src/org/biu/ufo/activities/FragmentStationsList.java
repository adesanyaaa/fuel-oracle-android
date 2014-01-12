package org.biu.ufo.activities;

import java.util.ArrayList;
import java.util.List;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.events.NearbyStationsChanged;
import org.biu.ufo.rest.Station;

import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.fragment_stations_list)
public class FragmentStationsList extends Fragment {
	@ViewById(R.id.stations_list)
	ListView stationsList;

	@Bean
	OttoBus bus;


	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		bus.unregister(this);
	}


	@Subscribe
	public void onStationsData(NearbyStationsChanged event) {
		List<String> values = new ArrayList<String>();

		if(event.getStations() != null) {
			for(Station station : event.getStations()) {
				StringBuilder builder = new StringBuilder();
				builder.append(station.getCompany()).append(" | ")
				.append(station.getPrice()).append(station.getPriceCurrency()).append(" | ")
				.append(station.getDistance());
				values.add(builder.toString());
			}
		}

		if(values.isEmpty()) {
			values.add("Sorry, no gas stations :(");
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
				android.R.layout.simple_list_item_1, android.R.id.text1, values.toArray(new String[0]));
		stationsList.setAdapter(adapter);
	}

}
