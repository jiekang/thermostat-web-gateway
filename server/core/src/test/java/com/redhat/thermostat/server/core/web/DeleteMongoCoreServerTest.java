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

public class DeleteMongoCoreServerTest extends MongoCoreServerTestSetup {
    @Test
    public void testDeleteAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/DeleteAgents/systems/all/agents/agentId";
        String input = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(input), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse deleteResponse = client.newRequest(url).method(HttpMethod.DELETE).send();

        assertEquals(Response.Status.OK.getStatusCode(), deleteResponse.getStatus());
        assertEquals("DELETE: true", deleteResponse.getContentAsString());

        ContentResponse getAfterResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getAfterResponse.getStatus());
        assertTrue(getAfterResponse.getContentAsString().matches("\\{\"response\" : \\[],\"time\" : \"[0-9]*\"}"));
    }
    @Test
    public void testDeleteSpecificAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/DeleteSpecificAgents/systems/all/agents";

        String oneUrl = url + "/one";
        String twoUrl = url + "/two";
        String allUrl = url + "/all";

        String oneInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        String twoInput = "[{\"otherStuff\":\"c\"},{\"otherStuff\":\"d\"}]";

        ContentResponse putOneResponse = client.newRequest(oneUrl).method(HttpMethod.PUT).content(new StringContentProvider(oneInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putOneResponse.getStatus());
        assertEquals("PUT: true", putOneResponse.getContentAsString());


        ContentResponse putTwoResponse = client.newRequest(twoUrl).method(HttpMethod.PUT).content(new StringContentProvider(twoInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putTwoResponse.getStatus());
        assertEquals("PUT: true", putTwoResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(allUrl).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" },\\{ \"otherStuff\" : \"c\" },\\{ \"otherStuff\" : \"d\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse deleteResponse = client.newRequest(oneUrl).method(HttpMethod.DELETE).send();

        assertEquals(Response.Status.OK.getStatusCode(), deleteResponse.getStatus());
        assertEquals("DELETE: true", deleteResponse.getContentAsString());

        ContentResponse getAfterResponse = client.newRequest(allUrl).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getAfterResponse.getStatus());
        assertTrue(getAfterResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"otherStuff\" : \"c\" },\\{ \"otherStuff\" : \"d\" }],\"time\" : \"[0-9]*\"}"));
    }
}
