package org.biu.ufo.rest;

import java.util.List;

public interface Client {
	public List<Station> getStations(String latitude, String longidute, float distance);
}
