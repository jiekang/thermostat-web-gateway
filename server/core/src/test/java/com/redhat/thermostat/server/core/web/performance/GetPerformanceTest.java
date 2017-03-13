/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

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
