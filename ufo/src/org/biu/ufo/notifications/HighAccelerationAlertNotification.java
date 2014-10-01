package org.biu.ufo.notifications;

import org.biu.ufo.MainApplication;
import org.biu.ufo.services.UfoMainService;

import android.view.View;

public class HighAccelerationAlertNotification extends PopupNotification {
	private MainApplication application;

	public HighAccelerationAlertNotification(MainApplication context) {
		super(context, UfoMainService.SERVICE_HIGH_ACCELERATION_ALERT_ID);
		application = context;
	}

	@Override
	public void onPopupClick() {
		closePopup();
	}

	@Override
	public void onShown() {
		application.startTextToSpeech("Alert");
    	automaticClosing(3000);
	}

	@Override
	public void onClosed() {
		application.stopTextToSpeech();
	}

	@Override
	public View createView() {
		// TODO Auto-generated method stub
		return null;
	}

}
