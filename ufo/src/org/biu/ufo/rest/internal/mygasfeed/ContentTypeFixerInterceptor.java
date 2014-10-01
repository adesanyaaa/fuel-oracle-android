package org.biu.ufo.rest.internal.mygasfeed;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class ContentTypeFixerInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] data, ClientHttpRequestExecution execution) throws IOException {
        // do something
    	ClientHttpResponse response = execution.execute(request, data);
    	response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    	return response;
    }
}