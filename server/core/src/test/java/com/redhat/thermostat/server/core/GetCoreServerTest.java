package com.redhat.thermostat.server.core;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Test;

public class GetCoreServerTest extends AbstractCoreServerTest {
    @Test
    public void testGetSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetSystem() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents";
        ContentResponse response = client.GET(url);
        System.out.println(response.getStatus());
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetAgent() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetJvm() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }
}
