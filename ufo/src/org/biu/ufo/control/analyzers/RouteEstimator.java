package org.biu.ufo.control.analyzers;

import java.util.LinkedList;
import java.util.List;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.Controller;
import org.biu.ufo.control.PlaceResolver;
import org.biu.ufo.control.PlaceResolver.OnPlaceResolved;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedDestinationMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedRouteMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteCompletedMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStartMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteSummaryMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.events.user.DestinationSelectedMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.model.Place;

import android.content.Context;

import com.directions.route.Routing;
import com.directions.route.Routing.TravelMode;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

/**
 * RouteEstimator
 * Higher level route analyzer (uses RouteAnalyzer)
 * 
 * TODO: estimate destination if not selected
 */
@EBean
public class RouteEstimator implements IAnalyzer {
	private static final float CLOSE_ENOUGH_DISTANCE = 0.2f;
	private static final long MIN_INTERVAL_BETWEEN_ROUTE_REQUESTS = 2*60*1000;
	
	static class WayPoint {
		Location location;
		Place place;
	}
	
	@RootContext
	Context context;

	@Bean
	OttoBus bus;
	
	@Bean
	PlaceResolver placeResolver;
	
	Controller controller;
	
	WayPoint destPoint;		// Final destination point : as selected by user or estimated
	boolean isDestLocationEstimated;	// False if destination selected by user
	
	List<WayPoint> stopPoints;	// All stop points on route
	List<RouteSummaryMessage> routeParts;	// All sub trips on route
	
	List<LatLng> estimatedRoute;	// Estimated route points
	int currentPositionInEstimatedRoute;
	boolean isEstimatedRouteNeeded = false;

	Location currentLocation;
	long lastRouteFetchTime = 0;
	
	/**
	 * Constructor
	 */
	public RouteEstimator() {
		initializeRoute();
	}
	
	/**
	 * New trip
	 */
	private void initializeRoute() {
		// Clear destination
		destPoint = null;
		isDestLocationEstimated = true;
		
		// Clear route estimation
		clearEstimatedRoute();
		lastRouteFetchTime = 0;
		
		// Clear stop points
		routeParts = new LinkedList<RouteSummaryMessage>();
		stopPoints = new LinkedList<RouteEstimator.WayPoint>();
	}

	/**
	 * User started driving
	 * @param message
	 */
	@Subscribe
	public void onRouteStarted(RouteStartMessage message) {		
		final WayPoint point = new WayPoint();
		point.location = message.getLocation(); 		
		placeResolver.resolvePlace(point.location, new OnPlaceResolved() {
			@Override
			public void onResult(Location location, Place place) {
				point.place = place;
			}

			@Override
			public void onFailure(Location location) {
				// TODO Auto-generated method stub
			}
		});
		
		stopPoints.add(point);
	}
	
	/**
	 * User stopped driving
	 * @param message
	 */
	@Subscribe
	public void onRouteEnded(RouteSummaryMessage message) {
		final WayPoint point = new WayPoint();
		point.location = message.getEndLocation();
		
		// Trip completed?
		boolean reachedDestination = false;
		if(destPoint != null && Calculator.distance(destPoint.location, point.location) < CLOSE_ENOUGH_DISTANCE) {
			point.place = destPoint.place;
			reachedDestination = true;
		} else {
			placeResolver.resolvePlace(point.location, new OnPlaceResolved() {
				@Override
				public void onResult(Location location, Place place) {
					point.place = place;
				}

				@Override
				public void onFailure(Location location) {
					// TODO Auto-generated method stub
				}
			});
		}
		
		stopPoints.add(point);
		routeParts.add(message);
		
		if(reachedDestination) {
			routeCompleted();
		}
	}

	private void routeCompleted() {
		if(routeParts.size() > 0) {
			bus.post(new RouteCompletedMessage(routeParts));			
		}
		initializeRoute();
	}

	/**
	 * User manually selected destination
	 * @param message
	 */
	@Subscribe
	public void onDestinationSelected(DestinationSelectedMessage message) {
		double latitude = message.getPlace().getAddress().getLatitude();
		double longitude = message.getPlace().getAddress().getLongitude();
		
		destPoint = new WayPoint();
		destPoint.location = new Location(latitude, longitude);
		destPoint.place = message.getPlace();
		isDestLocationEstimated = false;
		
		// Get route estimation
		if(isEstimatedRouteNeeded) {
			getNewRouteEstimation();			
		}
		
		bus.post(new EstimatedDestinationMessage(destPoint.place, isDestLocationEstimated));		
	}

	/**
	 * Fetch route directions
	 */
	private void getNewRouteEstimation() {
		clearEstimatedRoute();
		
		if(currentLocation == null || destPoint == null)
			return;
		
		Routing routing = new Routing(TravelMode.DRIVING);
		routing.registerListener(new RoutingListener() {
			
			@Override
			public void onRoutingSuccess(PolylineOptions mPolyOptions) {
				onEstimatedRouteChanged(mPolyOptions.getPoints());
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
		routing.execute(currentLocation.getLatLng(), destPoint.location.getLatLng());
	}
	
	/**
	 * Called on new route estimation
	 * @param points
	 */
	protected void onEstimatedRouteChanged(List<LatLng> points) {
		estimatedRoute = points;
		currentPositionInEstimatedRoute =  0;
		bus.post(new EstimatedRouteMessage(destPoint.place, estimatedRoute));
	}
	
	/**
	 * Current location changed
	 * @param message
	 */
	@Subscribe
	public void onLocationUpdate(LocationMessage message){
		currentLocation = message.getLocation();
		
		// Check if needs new route estimation
		if(isEstimatedRouteNeeded) {
			boolean onRoute = false;
			
			if(estimatedRoute != null) {
				for(int i = currentPositionInEstimatedRoute; i < estimatedRoute.size(); ++i) {
					LatLng point = estimatedRoute.get(i);
					
					if(Calculator.distance(currentLocation, new Location(point)) < CLOSE_ENOUGH_DISTANCE) {
						currentPositionInEstimatedRoute = i;
						onRoute = true;
					} else if(i - currentPositionInEstimatedRoute > 10) {
						break;
					}
				}
			}
			
			if(!onRoute && System.currentTimeMillis() - lastRouteFetchTime > MIN_INTERVAL_BETWEEN_ROUTE_REQUESTS) {
				getNewRouteEstimation();
			}
			
		}
	}

	private void clearEstimatedRoute() {
		estimatedRoute = null;
		currentPositionInEstimatedRoute = 0;
	}
	
	public void setRouteEstimationNeeded(boolean isNeeded) {
		if(isEstimatedRouteNeeded == isNeeded) {
			return;
		}
		
		isEstimatedRouteNeeded = isNeeded;			
		if(isEstimatedRouteNeeded) {
			getNewRouteEstimation();
		} else {
			clearEstimatedRoute();
		}
	}
	
	public void requestRouteEstimation() {
		if(estimatedRoute != null) {
			bus.post(new EstimatedRouteMessage(destPoint.place, estimatedRoute));
		} else {
			setRouteEstimationNeeded(true);
		}
	}

	@Produce
	public EstimatedDestinationMessage produceEstimatedDestination() {
		if(destPoint != null && destPoint.place != null) {
			return new EstimatedDestinationMessage(destPoint.place, isDestLocationEstimated);			
		}
		return null;
	}

	@Override
	public void start() {
		bus.register(this);
	}

	@Override
	public void stop() {
		routeCompleted();
		bus.unregister(this);
	}
	
	@Override
	public void setController(Controller controller) {
		this.controller = controller;
	}

	
}