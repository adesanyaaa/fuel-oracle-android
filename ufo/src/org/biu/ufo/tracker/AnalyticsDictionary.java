package org.biu.ufo.tracker;

public class AnalyticsDictionary {

	public class Screen{
		public static final String FUEL_NEXT = "Fuel Next popup";
		
		public static final String MORE_RECOMMENDATIONS = "More Recommendations";

	}
	public class Navigation {
		public static final String CATEGORY = "Navigation";
		public static final String POSITION = "position";
		
		public class Action {
			public static final String OPEN_GPS = "open GPS app";
			public static final String RECOMMENDATION_OPTION = "destination recommedation option";
		}
	}
	
	public class Recommendation {
		public static final String CATEGORTY = "Recommendation";
		public static final String FUEL_LEVEL = "fuel Level";
		public static final String ACCEPTED = "accepted";
		public static final String IGNORED = "ignored";
		
		
		public class Action {
			public static final String DISPLAY_POPUP_FUEL_NEXT = "displayed FUEL NEXT popup";
			public static final String RECOMMENDATION_INTERACTION = "recommendation interaction";
		}
	
	}
}