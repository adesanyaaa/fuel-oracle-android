package org.biu.ufo.ui.activities;

import it.gmariotti.cardslib.library.view.CardView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.biu.ufo.OttoBus;
import org.biu.ufo.R;
import org.biu.ufo.control.events.analyzer.recommendation.FuelRecommendationMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.EstimatedDestinationMessage;
import org.biu.ufo.control.events.analyzer.routemonitor.RouteStartMessage;
import org.biu.ufo.control.events.raw.EngineSpeedMessage;
import org.biu.ufo.control.events.raw.FuelLevelMessage;
import org.biu.ufo.control.events.raw.VehicleSpeedMessage;
import org.biu.ufo.ui.cards.FuelSuggestionCard;
import org.biu.ufo.ui.cards.RouteOverviewCard;
import org.biu.ufo.ui.cards.SquareCarDataCard;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ScrollView;

import com.squareup.otto.Subscribe;

@EFragment(R.layout.fragment_main)
public class FragmentMain extends Fragment {
	
	@Bean
	OttoBus bus;
	
	@ViewById(R.id.card_scrollview)
	ScrollView mScrollView;
	
	@ViewById
	CardView card_route_overview;
	
	@ViewById
	CardView card_fuel_suggestion;
	
	@ViewById
	CardView card_fuel_level;
	
	@ViewById
	CardView card_engine_speed;
	
	@ViewById
	CardView card_vehicle_speed;

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
		routeOverviewCard.initialize();
		card_route_overview.setCard(routeOverviewCard);
		
		FuelSuggestionCard fuelSuggestionCard = new FuelSuggestionCard(getActivity());
		card_fuel_suggestion.setCard(fuelSuggestionCard);

		SquareCarDataCard fuelLevelCard = new SquareCarDataCard(getActivity(), "Fuel", 0);
		card_fuel_level.setCard(fuelLevelCard);
		
		SquareCarDataCard engineSpeedCard = new SquareCarDataCard(getActivity(), "Engine", 0);
		card_engine_speed.setCard(engineSpeedCard);
		
		SquareCarDataCard vehicleCard = new SquareCarDataCard(getActivity(), "Speed", 0);
		card_vehicle_speed.setCard(vehicleCard);
		
//		fuelLevelCard.setTitle("Fuel Level");
//		fuelLevelCard.setImageResource(R.drawable.gasstation);
//		fuelLevelCard.setTileColor(getActivity().getResources().getColor(R.color.green_tile));
//		
//		engineSpeedCard.setTitle("Engine Speed");
//		engineSpeedCard.setTileColor(getActivity().getResources().getColor(R.color.navy_tile));
//
//		vehicleSpeedCard.setTitle("Vehicle Speed");
//		vehicleSpeedCard.setTileColor(getActivity().getResources().getColor(R.color.navy_tile));
	}
	
	@Subscribe
	public void onStartOfRouteMessage(RouteStartMessage message) {
		RouteOverviewCard oldCard = (RouteOverviewCard)card_route_overview.getCard();
		
		RouteOverviewCard card = new RouteOverviewCard(oldCard.getContext());
		card.setDestination(oldCard.getDestination());
		card.setDrivingState(true);
		card.initialize();
		
		card_route_overview.replaceCard(card);
	}

	@Subscribe
	public void onEstimatedDestination(EstimatedDestinationMessage message) {
		RouteOverviewCard card = (RouteOverviewCard)card_route_overview.getCard();
		card.setDestination(message.getPlace());
		card_route_overview.refreshCard(card);
	}
	
	@UiThread
	@Subscribe
	public void onFuelNextRecommendation(FuelRecommendationMessage message) {
		Log.d("FragmentMain", "onFuelNextRecommendation");
		FuelSuggestionCard card = (FuelSuggestionCard)card_fuel_suggestion.getCard();
		if(message.shouldFuel()) {
			if(message.getStations().isEmpty()) {
				card.setTitle("No nearby stations");	
			} else {
				card.setTitle(message.getTopStation().getAddress());					
			}
		} else {
			card.setTitle("all good");
		}
		card_fuel_suggestion.refreshCard(card);
	}
	
	@UiThread
	@Subscribe
	public void onFuelLevelUpdate(FuelLevelMessage message){
		SquareCarDataCard card = (SquareCarDataCard)card_fuel_level.getCard();
		card.setLine1Text(String.valueOf(message.getFuelLevelValue()) + " %");
		card.setLine2Text("");
		
//		card.setBackgroundResource(new ColorDrawable(Color.RED));
		
		card_fuel_level.refreshCard(card);
		
		
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

}
