package com.redhat.thermostat.server.core;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Test;

public class MongoCoreServerTest extends AbstractMongoCoreServerTest {

    @Test
    public void testSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    public void testAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/all/agents";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/all/agents/all/jvms";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

}
