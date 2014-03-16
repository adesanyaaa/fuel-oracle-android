package org.biu.ufo.control.events.analyzer.routemonitor;

import java.util.List;

public class RouteCompletedMessage {
	List<RouteSummaryMessage> routeParts;
	
	public RouteCompletedMessage(List<RouteSummaryMessage> routeParts) {
		this.routeParts = routeParts;
	}
	
	public List<RouteSummaryMessage> getRouteParts() {
		return routeParts;
	}

}
