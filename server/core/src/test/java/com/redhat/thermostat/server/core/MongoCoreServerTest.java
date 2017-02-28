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
    public void testPutGetSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetSystems/systems";

        String putInput = "[{\"systemId\":\"a\"},{\"systemId\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"systemId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] },\"1\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"systemId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPutGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetAgents/systems/all/agents";
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] },\"1\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAgentsLimit() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsLimit/systems/all/agents";
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", "1").send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAgentsSort() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsSort/systems/all/agents";
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse sortDescResponse = client.newRequest(url).method(HttpMethod.GET).param("sort", "-agentId").send();

        assertTrue(sortDescResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] },\"1\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(sortDescResponse.getStatus() == Response.Status.OK.getStatusCode());


        ContentResponse sortAsceResponse = client.newRequest(url).method(HttpMethod.GET).param("sort", "+agentId").send();

        assertTrue(sortAsceResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] },\"1\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(sortAsceResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testGetAgentsLimitSort() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsSortLimit/systems/all/agents";
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", "1").param("sort", "-agentId").send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"agentId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsLimit/systems/all/agents";
        String putInput = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
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
        String url = baseUrl + "/DeleteAgents/systems/all/agents";
        String input = "[{\"agentId\":\"a\"},{\"agentId\":\"b\"}]";
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
    public void testPutGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetJvms/systems/all/agents/all/jvms";
        String putInput = "[{\"jvmId\":\"a\"},{\"jvmId\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals("PUT: true", putResponse.getContentAsString());
        assertTrue(putResponse.getStatus() == Response.Status.OK.getStatusCode());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\{\"0\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"jvmId\" : \"a\", \"tags\" : \\[\"admin\", \"user\"] },\"1\" : \\{ \"_id\" : \\{ \"\\$oid\" : \".*\" }, \"jvmId\" : \"b\", \"tags\" : \\[\"admin\", \"user\"] }},\"time\" : \"[0-9]*\"}"));
        assertTrue(getResponse.getStatus() == Response.Status.OK.getStatusCode());
    }

}
