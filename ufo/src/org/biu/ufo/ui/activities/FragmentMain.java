package org.biu.ufo.ui.activities;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;
import it.gmariotti.cardslib.library.view.CardView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.Calculator;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedDestinationMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStartMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStopMessage;
import org.biu.ufo.control.events.raw.EngineSpeedMessage;
import org.biu.ufo.control.events.raw.FuelLevelMessage;
import org.biu.ufo.control.events.raw.LocationMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.model.Location;
import org.biu.ufo.rest.Station;
import org.biu.ufo.ui.cards.BasicInfoCard;
import org.biu.ufo.ui.cards.MoreFuelSuggestionsCard;
import org.biu.ufo.ui.cards.RecommendationCard;
import org.biu.ufo.ui.cards.RecommendationCardExpandInside;
import org.biu.ufo.ui.cards.RecommendationCardHeader;
import org.biu.ufo.ui.cards.RouteOverviewCard;
import org.biu.ufo.ui.cards.SquareCarDataCard;
import org.biu.ufo.ui.utils.UnitConverter;

import android.content.Intent;
import android.opengl.Visibility;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.fragment_main)
public class FragmentMain extends Fragment {

	private static final int FUEL_LEVEL_PER_DAY = 20;
	private static final String FUEL_SUGGESTION_MSG_DAYS_PART1 = "You can drive for ";
	private static final String FUEL_SUGGESTION_MSG_DAYS_PART2 = " days.";
	private static final String FUEL_SUGGESTION_MSG_DEFAULT = "Drive Carefully!";
	private static final String FUEL_SUGGESTION_MSG_NO_NEAR_STATIONS = "No stations on near path";

	@Bean
	OttoBus bus;
	
	@ViewById(R.id.card_scrollview)
	ScrollView mScrollView;
	
	@ViewById
	CardView card_route_overview;
	
	@ViewById
	CardView card_fuel_suggestion;
	
	@ViewById
	Button more_button;
	
	@ViewById
	CardView card_fuel_level;
	
	@ViewById
	CardView card_engine_speed;
	
	@ViewById
	CardView card_vehicle_speed;
	
	RecommendationCard recommendationCard;

	boolean displaysFuelSuggestion = false;
	@Override
	public void onResume() {
		super.onResume();
		bus.register(this);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		bus.unregister(this);
	}

	@AfterViews
	void initialize() {
		RouteOverviewCard routeOverviewCard = new RouteOverviewCard(getActivity());
		card_route_overview.setCard(routeOverviewCard);
		
//		FuelSuggestionCard fuelSuggestionCard = new FuelSuggestionCard(getActivity());
//		card_fuel_suggestion.setCard(fuelSuggestionCard);
		initFuelSuggestion(true, FUEL_SUGGESTION_MSG_DEFAULT);
		
		more_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), MainActivity_.class).putExtra("screen", MainActivity.RECOMMENDATIONS));
				
			}
		});
		
		
		SquareCarDataCard fuelLevelCard = new SquareCarDataCard(getActivity(), "Fuel", 0);
		card_fuel_level.setCard(fuelLevelCard);
		
		SquareCarDataCard engineSpeedCard = new SquareCarDataCard(getActivity(), "Engine", 0);
		card_engine_speed.setCard(engineSpeedCard);
		
		SquareCarDataCard vehicleCard = new SquareCarDataCard(getActivity(), "Speed", 0);
		card_vehicle_speed.setCard(vehicleCard);
	}
	
	private void initFuelSuggestion(boolean init, String message) {
		displaysFuelSuggestion = false;
		
/*		BasicInfoCard card = new BasicInfoCard(getActivity(), message);
 */
		//TODO: maybe create a custom card header 
		//Create a Card
		//Create a Card
        Card card = new Card(getActivity());

        //Create a CardHeader
        CardHeader header = new CardHeader(getActivity());

        //Set the header title
        header.setTitle(message);

        card.addCardHeader(header);

//		card.setExpanded(true);
//		card_fuel_suggestion.setExpanded(true);

		
		if (init){
			card_fuel_suggestion.setCard(card);
		}else{
			card_fuel_suggestion.replaceCard(card);
		}
		
		more_button.setVisibility(View.GONE);
	}
	
	@Subscribe
	public void onRouteStartMessage(RouteStartMessage message) {		
		RouteOverviewCard oldCard = (RouteOverviewCard)card_route_overview.getCard();
		
		RouteOverviewCard card = new RouteOverviewCard(oldCard.getContext());
		card.setDestination(oldCard.getDestination());
		card.setDrivingState(true);
		card.initialize();		
		card_route_overview.replaceCard(card);
		

	}
	
	@Subscribe
	public void onRouteStopMessage(RouteStopMessage message) {
		RouteOverviewCard oldCard = (RouteOverviewCard)card_route_overview.getCard();
		
		RouteOverviewCard card = new RouteOverviewCard(oldCard.getContext());
		card.setDestination(oldCard.getDestination());
		card.setDrivingState(false);
		card.initialize();
		card_route_overview.replaceCard(card);
	}

	@Subscribe
	public void onEstimatedDestinationMessage(EstimatedDestinationMessage message) {
		RouteOverviewCard card = (RouteOverviewCard)card_route_overview.getCard();
		card.setDestination(message.getPlace());
		card_route_overview.refreshCard(card);
	}
	
	@UiThread
	@Subscribe
	public void onFuelRecommendationMessage(FuelRecommendationMessage message) {
		Log.d("FragmentMain", "onFuelNextRecommendation");
		if(message.shouldFuel()) {
			if(!message.getStations().isEmpty()) {
				displaysFuelSuggestion = true;
				Station station = message.getTopStation();
				//Create a Card
				recommendationCard = new RecommendationCard(getActivity(), message, station);
				
				//Set card in the cardView
				ViewToClickToExpand viewToClickToExpand =
						ViewToClickToExpand.builder()
						.highlightView(false)
						.setupView(card_fuel_suggestion);
				recommendationCard.setViewToClickToExpand(viewToClickToExpand);

				card_fuel_suggestion.setExpanded(true);
				recommendationCard.setExpanded(true);
				card_fuel_suggestion.replaceCard(recommendationCard);
				
				if (message.getStations().size()>1){
					more_button.setVisibility(View.VISIBLE);
				}
				
			}else{
				initFuelSuggestion(false, FUEL_SUGGESTION_MSG_NO_NEAR_STATIONS);
			}
		} else{
			initFuelSuggestion(false, FUEL_SUGGESTION_MSG_DEFAULT);
		}
	}
	
	@UiThread
	@Subscribe
	public void onFuelLevelUpdate(FuelLevelMessage message){
		SquareCarDataCard card = (SquareCarDataCard)card_fuel_level.getCard();
		card.setLine1Text(String.valueOf(message.getFuelLevelValue()) + " %");
		card.setLine2Text("");
		
//		card.setBackgroundResource(new ColorDrawable(Color.RED));
		
		card_fuel_level.refreshCard(card);
		
		if (!displaysFuelSuggestion){
			int fuelDays = (int) (message.getFuelLevelValue()/FUEL_LEVEL_PER_DAY);
			String daysMessage = FUEL_SUGGESTION_MSG_DAYS_PART1 + String.valueOf(fuelDays)
					+ FUEL_SUGGESTION_MSG_DAYS_PART2 + " " + FUEL_SUGGESTION_MSG_DEFAULT;
			initFuelSuggestion(false, daysMessage);
		}
		
		
//		fuelLevelLayout.setBackgroundResource(message.background);
//		fuelLevelMainMessage.setText(message.mainMessage);
//		fuelLevelsubMessage1.setText(message.subMessage_1);
//		fuelLevelsubMessage2.setText(message.subMessage_2);
	}

	@UiThread
	@Subscribe
	public void onEngineSpeedUpdate(EngineSpeedMessage message) {
		SquareCarDataCard card = (SquareCarDataCard)card_engine_speed.getCard();
		card.setLine1Text(message.engineSpeed);
		card.setLine2Text("RPM");
	}

	@UiThread
	@Subscribe
	public void onVehicleSpeedUpdate(VehicleSpeedMessage message) {

		SquareCarDataCard card = (SquareCarDataCard)card_vehicle_speed.getCard();
		card.setLine1Text(message.vehicleSpeed);
		card.setLine2Text("km/h");
	}
	
	@UiThread
	@Subscribe
	public void onLocationUpdate(LocationMessage message) {

		if (recommendationCard != null){
			double distance = ((RecommendationCardExpandInside)recommendationCard.getCardExpand()).getStationDistance();
			Location stationLocation = ((RecommendationCardExpandInside)recommendationCard.getCardExpand()).getLocation();
			double currentDistance = Calculator.distance(stationLocation, message.location);
			
			//above than 100 meter - update
			if (Math.abs(distance - currentDistance) >= 0.1){
				recommendationCard.setDistance(currentDistance);
			}
		}
	}

}
