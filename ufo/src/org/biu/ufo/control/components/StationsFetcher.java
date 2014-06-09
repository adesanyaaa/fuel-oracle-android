package org.biu.ufo.control.components;

import java.util.LinkedList;
import java.util.List;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.biu.ufo.rest.Client;
import org.biu.ufo.rest.MGFClient;
import org.biu.ufo.rest.Station;
import org.biu.ufo.rest.UFOClient;

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

@EBean
public class StationsFetcher {

	public static interface StationsFetcherResultHandler {
		void onStationsResult(List<Station> stations);
	}
	
	@Bean(UFOClient.class)
	Client stationsClient;
	
	List<Station> stationsResult;
	
	Handler handler = new Handler();
	private volatile long currentRequestId;
	private volatile int pendingRequests;
	
	public void requestStations(List<LatLng> pos, StationsFetcherResultHandler resultHandler) {
		currentRequestId = (currentRequestId + 1) % 500;
		pendingRequests = pos.size();
		stationsResult = new LinkedList<Station>();
		
		for(LatLng latLng : pos) {
			fetchStations(resultHandler, currentRequestId, latLng.latitude, latLng.longitude);
		}
	}

	@Background
	protected void fetchStations(final StationsFetcherResultHandler resultHandler, final long requestId, double lat, double lng) {
		final float distance = 0.5f; // in KM
		final List<Station> stations = stationsClient.getStations(String.valueOf(lat), String.valueOf(lng), distance);
		Log.e("StationsFetcher", "size="+stations.size());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if(requestId == currentRequestId) {
					delieverStationsList(resultHandler, stations);
				}
			}
		});
	}

	protected void delieverStationsList(StationsFetcherResultHandler resultHandler, final List<Station> stations) {
		--pendingRequests;
		stationsResult.addAll(stations);
		if(pendingRequests == 0) {
			resultHandler.onStationsResult(stationsResult);
		}
	}

}
