package com.redhat.thermostat.server.core;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Test;

public class GetCoreServerTest extends AbstractCoreServerTest {
    @Test
    public void testGetSystem() throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.GET("http://" + host + ":" + port + "/" + "api/v100/namespace/systems");
        System.out.println(response.getContentAsString());
        assertTrue(response.getStatus() == 200);
    }
}
