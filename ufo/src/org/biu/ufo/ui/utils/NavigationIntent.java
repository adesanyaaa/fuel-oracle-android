package org.biu.ufo.ui.utils;

import org.biu.ufo.model.Location;

import android.content.Intent;
import android.net.Uri;

public class NavigationIntent {
	public static Intent getNavigationIntent(Location location) {
        String destStr = "waze://?ll=";
        destStr += String.valueOf(location.getLatitude());
        destStr += ",";
        destStr += String.valueOf(location.getLongitude());
        destStr += "&navigate=yes";
        return new Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).setData(Uri.parse(destStr));            		

	}
}
