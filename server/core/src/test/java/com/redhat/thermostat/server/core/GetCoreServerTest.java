package com.redhat.thermostat.server.core;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Test;

public class GetCoreServerTest extends AbstractCoreServerTest {
    @Test
    public void testGetSystems() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.GET("http://" + host + ":" + port + "/" + "api/v100/namespace/systems");
        System.out.println(response.getContentAsString());
        assertTrue(response.getStatus() == 200);
    }

    @Test
    public void testGetSystem() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.GET("http://" + host + ":" + port + "/" + "api/v100/namespace/systems/systemId");
        System.out.println(response.getContentAsString());
        assertTrue(response.getStatus() == 200);
    }

    @Test
    public void testGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.GET("http://" + host + ":" + port + "/" + "api/v100/namespace/systems/systemId/agents");
        System.out.println(response.getContentAsString());
        assertTrue(response.getStatus() == 200);
    }

    @Test
    public void testGetAgent() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.GET("http://" + host + ":" + port + "/" + "api/v100/namespace/systems/systemId/agents/agentId");
        System.out.println(response.getContentAsString());
        assertTrue(response.getStatus() == 200);
    }

    @Test
    public void testGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.GET("http://" + host + ":" + port + "/" + "api/v100/namespace/systems/systemId/agents/agentId/jvms");
        System.out.println(response.getContentAsString());
        assertTrue(response.getStatus() == 200);
    }

    @Test
    public void testGetJvm() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.GET("http://" + host + ":" + port + "/" + "api/v100/namespace/systems/systemId/agents/agentId/jvms/jvmId");
        System.out.println(response.getContentAsString());
        assertTrue(response.getStatus() == 200);
    }
}
