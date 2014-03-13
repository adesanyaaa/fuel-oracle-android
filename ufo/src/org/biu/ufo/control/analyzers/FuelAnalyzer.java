package org.biu.ufo.control.analyzers;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.OttoBus;
import org.biu.ufo.control.events.analyzer.fueling.FuelLossProcessEndedStatusMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuelLossProcessStartedStatusMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuelProcessDetailsMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuellingProcessEndedStatusMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuellingProcessStartedStatusMessage;
import org.biu.ufo.control.events.analyzer.fueling.FuellingProcessStatusMessage;
import org.biu.ufo.control.events.raw.FuelLevelMessage;

import android.os.Handler;
import android.os.Message;

import com.squareup.otto.Subscribe;

/**
 * FuelAnalyzer
 * according to messages received from the car\simulator determines:
 * 	1. if a fuelling process has began\ended
 *  2. if a fuel loss process has began\ended
 * notifies via bus
 * @author Danny Karmon
 *
 */
@EBean
public class FuelAnalyzer {
	public static final int FUELLING_PROCESS = 1;
	public static final int FUEL_LOSS_PROCESS = 2;
	
	//if factors apply - alert on fuel change
	public static float MIN_PERCENTAGE_CHANGE = 5;	//minimum change in fuel status
	public static long MAX_FUEL_DURATION_SECONDS = 10;
	public static long MAX_FUEL_DURATION = MAX_FUEL_DURATION_SECONDS*1000;	//max fuelling duration time

	@Bean
	OttoBus bus;
	
	float refFuelLevel;
	float currentFuelLevel;
	
	int fuelStatus = 0;
	
	boolean firstTimeInit = true;
	
	FuelProcessDetailsMessage details = new FuelProcessDetailsMessage();

	Handler durationHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			details.setEndFuelLevel(currentFuelLevel);
			details.setEndTime(System.currentTimeMillis());
			
			FuellingProcessStatusMessage endProcessMessage;
			if (msg.what == FUELLING_PROCESS){
				endProcessMessage = new FuellingProcessEndedStatusMessage();
			}else {
				endProcessMessage = new FuelLossProcessEndedStatusMessage();
			}
			
			//the process has ended
			bus.post(endProcessMessage);
			bus.post(details);
			
			fuelStatus = 0;
			firstTimeInit = true;
		}

	};

	
	@UiThread
	@Subscribe
	public void onFuelLevelUpdate(FuelLevelMessage message){
		if (firstTimeInit){
			firstTimeInit = false;
			refFuelLevel= (float) message.getFuelLevelValue();
			
		}else{
			currentFuelLevel = (float) message.getFuelLevelValue();
			fuelDeltaCheck();
		}

	}
	
	
	private void fuelDeltaCheck(){
		float delta_fuel = currentFuelLevel - refFuelLevel;
		//observable change in fuel level - alert
		if (Math.abs(delta_fuel) >= MIN_PERCENTAGE_CHANGE) {
			
			//increase in fuel level
			if (delta_fuel>0){
				
				//notify that fuelling process has began
				if (fuelStatus != FUELLING_PROCESS){
					bus.post(new FuellingProcessStartedStatusMessage());
					fuelStatus = FUELLING_PROCESS;
					
					initFuelDetails();
				}
				
				
			}else{//decrease in fuel level - CAUTION!
				
				//notify that fuelling loss process has began
				if (fuelStatus != FUEL_LOSS_PROCESS){
					bus.post(new FuelLossProcessStartedStatusMessage());
					fuelStatus = FUEL_LOSS_PROCESS;
					initFuelDetails();
				}
				
			}
			//reset timeout for fuelling process
			restartTimer();
		}
		//update reference fuel level
		refFuelLevel = currentFuelLevel;
	}
	private void initFuelDetails(){
		details = new FuelProcessDetailsMessage();
		details.setStartTime(System.currentTimeMillis());
		details.setStartFuelLevel(refFuelLevel);
	}
	
	
	/**
	 * for ending the current fuel process.
	 * if not called within MIN_FUEL_DURATION - the process ended
	 */
	private void restartTimer(){
			
		//remove pending messages
		durationHandler.removeMessages(FUELLING_PROCESS);
		durationHandler.removeMessages(FUEL_LOSS_PROCESS);
		
		//set new one
		durationHandler.sendEmptyMessageDelayed(fuelStatus, MAX_FUEL_DURATION);
	}
}
