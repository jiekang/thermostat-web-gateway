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

public class PostMongoCoreServerTest extends MongoCoreServerTestSetup {

    @Test
    public void testEqualQueryPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/EqualQueryPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff=a\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testLessQueryPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/LessQueryPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff<b\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGreaterQueryPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GreaterQueryPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff>a\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testLessEqualQueryPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/LessEqualQueryPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff<=b\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGreaterEqualQueryPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GreaterEqualQueryPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff>=a\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }


    @Test
    public void testMultiQueryPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/MultiQueryPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\", \"item\":\"1\"},{\"agentStuff\":\"a\", \"item\":\"2\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff=a\", \"item<2\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"item\" : \"1\" }],\"time\" : \"[0-9]*\"}"));
    }
}
