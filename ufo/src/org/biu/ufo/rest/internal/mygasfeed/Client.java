package org.biu.ufo.rest.internal.mygasfeed;

import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Rest;
import org.androidannotations.api.rest.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Rest(rootUrl="http://devapi.mygasfeed.com", converters={MappingJackson2HttpMessageConverter.class}, interceptors = { ContentTypeFixerInterceptor.class })
public interface Client {	
	
	@Get("/stations/radius/{latitude}/{longitude}/{distance}/{fuelType}/{sortBy}/rfej9napna.json")
	@Accept(MediaType.APPLICATION_JSON)
	StationsResponse getStations(String latitude, String longitude, float distance, RequestFuelType fuelType, RequestSortBy sortBy);

}
