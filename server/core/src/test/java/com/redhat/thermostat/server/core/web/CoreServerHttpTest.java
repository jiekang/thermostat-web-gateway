package com.redhat.thermostat.server.core.web;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.server.core.web.setup.CoreServerTestSetup;

public class CoreServerHttpTest extends CoreServerTestSetup {
    @Test
    public void testGetSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPutSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.PUT).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPutAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.PUT).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPutJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.PUT).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPostSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.POST).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.POST).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPostJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.POST).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testDeleteSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.DELETE).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testDeleteAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.DELETE).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testDeleteJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.DELETE).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }
}
