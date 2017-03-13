package com.redhat.thermostat.server.core.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.server.core.web.setup.SwaggerCoreServerTestSetup;

public class SwaggerCoreServerTest extends SwaggerCoreServerTestSetup {
    @Test
    public void testGetSwaggerJson() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/swagger.json";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();

        assertFalse(response.getContentAsString().equals(""));
        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetSwaggerIndex() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/apidocs";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();

        assertFalse(response.getContentAsString().equals(""));
        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());

    }


}
