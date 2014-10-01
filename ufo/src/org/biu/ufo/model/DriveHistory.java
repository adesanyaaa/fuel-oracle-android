package org.biu.ufo.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DriveHistory {
	private static SimpleDateFormat sDateFormatter =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
	
	//Members 
	private int totalRoutes = 0;
	private String createTime;
	ArrayList<DriveRoute> routes = new ArrayList<DriveRoute>();
	
	
	public DriveHistory() {
		Calendar calendar = GregorianCalendar.getInstance();
		this.createTime = sDateFormatter.format(calendar.getTime());
	}
	
	public DriveRoute getRouteByIndex(int index){
		if (index>=0 && index<routes.size()){
			return routes.get(index);
		}
		return null;
	}
	
	public void addRoute(DriveRoute dRoute){
		routes.add(dRoute);
		totalRoutes++;
	}

	public String getCreateTime() {
		return createTime;
	}

	public int getTotalRoutes() {
		return totalRoutes;
	}

}
