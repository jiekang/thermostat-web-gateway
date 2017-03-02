package com.redhat.thermostat.server.core.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.server.core.web.setup.MongoCoreServerTestSetup;

public class GetMongoCoreServerTest extends MongoCoreServerTestSetup {
    @Test
    public void testGetSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetSystems/systems/systemId";

        String putInput = "[{\"systemStuff\":\"a\"},{\"systemStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"systemStuff\" : \"a\" },\\{ \"systemStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsLimit() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsLimit/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", "1").send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsSort() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsSort/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse sortDescResponse = client.newRequest(url).method(HttpMethod.GET).param("sort", "-agentStuff").send();

        assertEquals(Response.Status.OK.getStatusCode(), sortDescResponse.getStatus());
        assertTrue(sortDescResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\" },\\{ \"agentStuff\" : \"a\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse sortAsceResponse = client.newRequest(url).method(HttpMethod.GET).param("sort", "+agentStuff").send();

        assertEquals(Response.Status.OK.getStatusCode(), sortAsceResponse.getStatus());
        assertTrue(sortAsceResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsLimitSort() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsSortLimit/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", "1").param("sort", "-agentStuff").send();

        System.out.println(getResponse.getContentAsString());
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetJvms/systems/all/agents/all/jvms/jvmId";
        String putInput = "[{\"jvmStuff\":\"a\"},{\"jvmStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"jvmStuff\" : \"a\" },\\{ \"jvmStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }
}
