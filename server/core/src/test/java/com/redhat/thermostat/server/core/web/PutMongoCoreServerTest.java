package com.redhat.thermostat.server.core.web;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.server.core.web.setup.MongoCoreServerTestSetup;

public class PutMongoCoreServerTest extends MongoCoreServerTestSetup {
    @Test
    public void testPutAllSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetSystems/systems/all";

        String putInput = "[{\"systemStuff\":\"a\"},{\"systemStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), putResponse.getStatus());
        assertEquals("", putResponse.getContentAsString());
    }

    @Test
    public void testPutAllAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetSystems/systems/systemId/agents/all";

        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), putResponse.getStatus());
        assertEquals("", putResponse.getContentAsString());
    }

    @Test
    public void testPutAllJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetSystems/systems/systemId/agents/agentId/jvms/all";

        String putInput = "[{\"jvmStuff\":\"a\"},{\"jvmStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), putResponse.getStatus());
        assertEquals("", putResponse.getContentAsString());
    }
}
