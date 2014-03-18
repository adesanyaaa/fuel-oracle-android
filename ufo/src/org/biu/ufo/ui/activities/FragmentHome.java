package org.biu.ufo.ui.activities;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.biu.ufo.R;
import org.biu.ufo.services.UfoMainService_;

import android.support.v4.app.Fragment;

@EFragment(R.layout.fragment_start_stop)
public class FragmentHome extends Fragment {
	public static final String TAG = "StartUFOFragment";
	
	@Click(R.id.start_ufo_service)
	void startMainService() {
		UfoMainService_.intent(this).start();
	}
	
	@Click(R.id.stop_ufo_service)
	void stopMainService() {
		UfoMainService_.intent(this).stop();
	}

}
