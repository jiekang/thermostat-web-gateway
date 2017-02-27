package com.redhat.thermostat.server.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

public class  MongoCoreServerTest extends AbstractMongoCoreServerTest {

    @Test
    public void testSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

    @Test
    public void testPutGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        String url = baseUrl + "/PutGetAgents/systems/all/agents";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] },\"1\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAgentsLimit() throws InterruptedException, ExecutionException, TimeoutException {
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        String url = baseUrl + "/GetAgentsLimit/systems/all/agents";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", "1").send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        String url = baseUrl + "/GetAgentsLimit/systems/all/agents";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        String postInput = "[\"agentId=a\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(postResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDeleteAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String input = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        String url = baseUrl + "/DeleteAgents/systems/all/agents";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(input), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] },\"1\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse deleteResponse = client.newRequest(url).method(HttpMethod.DELETE).send();

        assertEquals("DELETE: true", deleteResponse.getContentAsString());
        assertTrue(deleteResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getAfterResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertTrue(getAfterResponse.getContentAsString().matches("\\{\"response\" : \\{},\"time\" : \"[0-9]*\"}"));
        assertTrue(getAfterResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/all/agents/all/jvms";
        ContentResponse response = client.GET(url);
        assertTrue(response.getStatus() == Response.Status.NOT_IMPLEMENTED.getStatusCode());
    }

}
