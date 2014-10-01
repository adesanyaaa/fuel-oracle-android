package org.biu.ufo.notifications;

import org.biu.ufo.services.UfoMainService_;

import wei.mark.standout.StandOutWindow;
import android.content.Context;
import android.os.Handler;
import android.view.View;

public abstract class PopupNotification {
	private int popupId;
	private boolean popupShown;

	protected Context context;
	protected Handler handler = new Handler();

	public PopupNotification(Context context, int popupId) {
		this.context = context;
		this.popupId = popupId;
		this.popupShown = false;
	}

	public boolean showPopup() {
		if(!popupShown) {
			popupShown = true;
			StandOutWindow.show(context, UfoMainService_.class, this.popupId);
			return true;
		}
		return false;
	}

	public void closePopup() {
		if(popupShown) {
			handler.removeCallbacks(automaticClosingTask);
			popupShown = false;
			StandOutWindow.close(context, UfoMainService_.class, this.popupId);
			onClosed();
		}
	}

	public void automaticClosing(int delay) {
		handler.postDelayed(automaticClosingTask, delay);
	}

	protected Runnable automaticClosingTask = new Runnable() {
		@Override
		public void run() {
			closePopup();
		}
	};

	public boolean isPopupShown() {
		return popupShown;
	}
	
	public int getPopupId() {
		return popupId;
	}
	
	public abstract void onPopupClick();
	public abstract void onShown();
	public abstract void onClosed();
	public abstract View createView();

}
