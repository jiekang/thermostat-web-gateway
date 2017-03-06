package com.redhat.thermostat.server.core.web.performance;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.redhat.thermostat.server.core.web.setup.MongoCoreServerTestSetup;

@Ignore
public class GetPerformanceTest extends MongoCoreServerTestSetup {

    private static final int ITERATIONS = 10000;

    @BeforeClass
    public static void setupClass() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetPerformanceTest/systems/systemId";

        times.put("setupClass", new ArrayList<Long>());
        for (int i = 0; i < ITERATIONS; i++) {
            String putInput = "[{ \"timeStamp\" : { \"$numberLong\" : \"" + i + "\" }, \"vmId\" : \"fc8115b4-f71b-4634-bfa7-a55c86aa58d2\", \"cpuLoad\" : 10.0, \"agentId\" : \"d0bb207f-7de3-4f91-be8f-b71be6f75b33\" }]";

            long s = System.nanoTime();
            ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();
            long e = System.nanoTime() - s;
            times.get("setupClass").add(e);


            assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
            assertEquals("PUT: true", putResponse.getContentAsString());
        }
    }

    @Test
    public void testGetOne() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetPerformanceTest/systems/systemId";

        times.put("testGetOne", new ArrayList<Long>());
        for (int i = 0; i < ITERATIONS; i++) {
            long s = System.nanoTime();
            ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", "1").send();
            long e = System.nanoTime() - s;
            times.get("testGetOne").add(e);
            assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        }
    }

    @Test
    public void testGetSome() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetPerformanceTest/systems/systemId";

        times.put("testGetSome", new ArrayList<Long>());
        for (int i = 0; i < ITERATIONS/10; i++) {
            long s = System.nanoTime();
            ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", String.valueOf(ITERATIONS/100)).send();
            long e = System.nanoTime() - s;
            times.get("testGetSome").add(e);
            assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        }
    }

    @Test
    public void testGetAll() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetPerformanceTest/systems/systemId";

        times.put("testGetAll", new ArrayList<Long>());
        for (int i = 0; i < ITERATIONS/100; i++) {
            long s = System.nanoTime();
            ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("limit", String.valueOf(ITERATIONS)).send();
            long e = System.nanoTime() - s;
            times.get("testGetAll").add(e);
            assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        }
    }

}
