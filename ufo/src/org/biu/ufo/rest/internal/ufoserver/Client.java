package org.biu.ufo.rest.internal.ufoserver;

import java.util.List;

import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(rootUrl="http://ufo-project.appspot.com", converters={FormHttpMessageConverter.class, MappingJackson2HttpMessageConverter.class})
public interface Client {	
	
	@Get("/get_stations?distance={distance}&latitude={latitude}&longitude={longitude}")
//	@Accept(MediaType.APPLICATION_JSON)
	List<Station> getStations(String latitude, String longitude, float distance);
}
