package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.biu.ufo.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

@EActivity(R.layout.activity_popup)
public class PopupActivity extends FragmentActivity {
	    	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams params = getWindow().getAttributes();
//		params.x = bounds.left;
		params.y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());;

//		params.height = bounds.bottom - bounds.top;				
//		params.width = bounds.right - bounds.left;
		params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
		params.flags = params.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR ;
		params.flags = params.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;	
		params.flags = params.flags & ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		getWindow().setAttributes(params);
	
		if(getIntent().getStringExtra("type").equals("fuel_next")) {
			FragmentManager fragmentManager = getSupportFragmentManager();
			Fragment fragment = new FuelNextFragment_();
			fragment.setRetainInstance(true);
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.replace(R.id.content_frame, fragment, "POPUP_FRAGMENT").commit();			
		}

		automaticClosing();
	}
		
	@Override
	protected void onPause() {
		overridePendingTransition(0, 0);
		super.onPause();
	}
			
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			finish();
		}
		return super.onTouchEvent(event);			
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(event.getKeyCode() == KeyEvent.KEYCODE_HOME ||
    			event.getKeyCode() == KeyEvent.KEYCODE_MENU ||
    			event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
    		finish();
    	}
    	return super.onKeyDown(keyCode, event);
    };

    @UiThread(delay=10000)
    public void automaticClosing() {
    	if(!isFinishing() && !isDestroyed()) {
    		finish();
    	}
    }
}

//package org.biu.ufo.ui.activities;
//
//import org.androidannotations.annotations.EActivity;
//import org.biu.ufo.R;
//
//import android.app.Activity;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.MotionEvent;
//import android.view.Window;
//import android.view.WindowManager;
//
//@EActivity(R.layout.card_layout)
//public class PopupActivity extends Activity {
//	    
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		WindowManager.LayoutParams params = getWindow().getAttributes();
////		params.x = bounds.left;
//		params.y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());;
//
////		params.height = bounds.bottom - bounds.top;				
////		params.width = bounds.right - bounds.left;
//		params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
//		params.flags = params.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR ;
//		params.flags = params.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;	
//		params.flags = params.flags & ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//		params.dimAmount = 0;
//		getWindow().setAttributes(params);
//		getWindow().setWindowAnimations(0);
//
//	}
//	
//	@Override
//	protected void onPause() {
//		overridePendingTransition(0, 0);
//		super.onPause();
//	}
//	@Override public void onStart() {
//	    super.onStart();
//
//	    Window window = getWindow();
//	    WindowManager.LayoutParams windowParams = window.getAttributes();
//	    windowParams.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
//	    windowParams.flags = windowParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR ;
//	    windowParams.flags = windowParams.flags | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;	
//	    windowParams.flags = windowParams.flags & ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//
////	    windowParams.dimAmount = 0;//0.90f;
////	    windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//	    window.setAttributes(windowParams);
//	}
//
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//		finish();
//	}
//	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
////			if(event.getX() < 0 || event.getX() > getWindow().getAttributes().width ||
////				event.getY() < -DisplayTools.toPixels(this, WidgetBase.HEADER_SIZE_IN_DIP) || event.getY() > getWindow().getAttributes().height /*+ DisplayTools.toPixels(this, WidgetBase.FOOTER_SIZE_IN_DIP)*/) {
//				finish();
////			}
//		}
//		return super.onTouchEvent(event);			
//	}
//
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//    	if(event.getKeyCode() == KeyEvent.KEYCODE_HOME ||
//    			event.getKeyCode() == KeyEvent.KEYCODE_MENU ||
//    			event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
//    		finish();
//    	}
//    	return super.onKeyDown(keyCode, event);
//    };
//
//}