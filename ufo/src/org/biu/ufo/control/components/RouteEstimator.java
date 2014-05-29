package org.biu.ufo.control.components;

import java.util.List;

import org.androidannotations.annotations.EBean;
import org.biu.ufo.control.monitors.TripMonitor;
import org.biu.ufo.control.utils.Calculator;
import org.biu.ufo.model.Location;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.Routing.TravelMode;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

@EBean
public class RouteEstimator {
	private static final long MIN_INTERVAL_BETWEEN_ROUTE_REQUESTS = 2*60*1000;

	long lastRouteFetchTime = 0;

	public static interface EstimatedRouteResult {
		void onEstimatedRouteResult(EstimatedRoute route);
	}

	public static class EstimatedRoute {
		public Route fullRoute;
		public List<LatLng> points;		
		public int posInPoints;
	}

	public double getMinDistanceFromRoute(EstimatedRoute route, Location location){
		double minDistance = Double.POSITIVE_INFINITY;
		
		if(route != null) {
			double distance;
			for (int i = route.posInPoints; i < route.points.size(); ++i){
				Location routeLocation = new Location(route.points.get(i));
				distance = Calculator.distance(location,routeLocation);
				if (minDistance > distance){
					minDistance = distance;
				}
			}			
		}		
		return minDistance;
	}
	
	public boolean isOnRoute(EstimatedRoute route, Location location) {
		boolean onRoute = false;
		
		if(route != null) {
			for(int i = route.posInPoints; i < route.points.size(); ++i) {
				LatLng point = route.points.get(i);
				
				if(Calculator.distance(location, new Location(point)) < TripMonitor.CLOSE_ENOUGH_DISTANCE) {
					route.posInPoints = i;
					onRoute = true;
					break;
				} else if(i - route.posInPoints > 50) {
					break;
				}
			}
		}
		
		return onRoute;
	}
	
	public boolean isQueryAllowed() {
		return System.currentTimeMillis() - lastRouteFetchTime > MIN_INTERVAL_BETWEEN_ROUTE_REQUESTS;
	}

	/**
	 * Fetch route directions
	 */
	public void getNewRouteEstimation(Location source, Location dest, final EstimatedRouteResult resultHandler) {				
		Routing routing = new Routing(TravelMode.DRIVING);
		routing.registerListener(new RoutingListener() {
			
			@Override
			public void onRoutingSuccess(Route route, PolylineOptions mPolyOptions) {
				EstimatedRoute result = new EstimatedRoute();
				result.fullRoute = route;
				result.points = mPolyOptions.getPoints();
				resultHandler.onEstimatedRouteResult(result);
			}
			
			@Override
			public void onRoutingStart() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onRoutingFailure() {
				// TODO Auto-generated method stub
				
			}
		});
		
		lastRouteFetchTime = System.currentTimeMillis();
		routing.execute(source.getLatLng(), dest.getLatLng());
	}
}
