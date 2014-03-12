package org.biu.ufo.status_analyzer;

import org.androidannotations.annotations.sharedpreferences.DefaultFloat;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(value=SharedPref.Scope.UNIQUE)
public interface StatusSharedPref {
	
	@DefaultFloat(0)
	float lastLatitude();
	
	@DefaultFloat(0)
	float lastLongitude();
	
	@DefaultFloat(0)
	float lastFuelLevel();


}
