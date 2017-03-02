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

public class  MongoCoreServerTest extends AbstractMongoCoreServerTest {

    @Test
    public void testPutGetSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetSystems/systems/systemId";

        String putInput = "[{\"systemStuff\":\"a\"},{\"systemStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"systemStuff\" : \"a\" },\\{ \"systemStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testPutGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetAgents/systems/all/agents/agentId";
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

        System.out.println(postResponse.getContentAsString());
        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"item\" : \"1\" }],\"time\" : \"[0-9]*\"}"));
    }

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


    @Test
    public void testPutGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/PutGetJvms/systems/all/agents/all/jvms/jvmId";
        String putInput = "[{\"jvmStuff\":\"a\"},{\"jvmStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"jvmStuff\" : \"a\" },\\{ \"jvmStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

}
