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

package com.redhat.thermostat.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.web.setup.MongoCoreServerTestSetup;

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
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"systemStuff\" : \"a\", \"systemId\" : \"systemId\" },\\{ \"systemStuff\" : \"b\", \"systemId\" : \"systemId\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgents/systems/*/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"agentId\" : \"agentId\" },\\{ \"agentStuff\" : \"b\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsLimit() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsLimit/systems/*/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("l", "1").send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsQuery() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsQuery/systems/*/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getAResponse = client.newRequest(url).method(HttpMethod.GET).param("q", "agentStuff==a").send();

        assertEquals(Response.Status.OK.getStatusCode(), getAResponse.getStatus());
        assertTrue(getAResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse getBResponse = client.newRequest(url).method(HttpMethod.GET).param("q", "agentStuff==b").send();

        assertEquals(Response.Status.OK.getStatusCode(), getBResponse.getStatus());
        assertTrue(getBResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));

    }

    @Test
    public void testGetAgentsProject() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsProject/systems/*/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\",\"otherStuff\":\"blob\"},{\"agentStuff\":\"b\",\"otherStuff\":\"blob\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("p", "agentStuff").send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsNotEqual() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsNotEqual/systems/*/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("q","agentStuff!=a").send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsSort() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsSort/systems/*/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse sortDescResponse = client.newRequest(url).method(HttpMethod.GET).param("s", "-agentStuff").send();

        assertEquals(Response.Status.OK.getStatusCode(), sortDescResponse.getStatus());
        assertTrue(sortDescResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\", \"agentId\" : \"agentId\" },\\{ \"agentStuff\" : \"a\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse sortAsceResponse = client.newRequest(url).method(HttpMethod.GET).param("s", "+agentStuff").send();

        assertEquals(Response.Status.OK.getStatusCode(), sortAsceResponse.getStatus());
        assertTrue(sortAsceResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"agentId\" : \"agentId\" },\\{ \"agentStuff\" : \"b\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetAgentsLimitSort() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetAgentsSortLimit/systems/*/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).param("l", "1").param("s", "-agentStuff").send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/GetJvms/systems/*/agents/*/jvms/jvmId";
        String putInput = "[{\"jvmStuff\":\"a\"},{\"jvmStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"jvmStuff\" : \"a\", \"vmId\" : \"jvmId\" },\\{ \"jvmStuff\" : \"b\", \"vmId\" : \"jvmId\" }],\"time\" : \"[0-9]*\"}"));
    }
}
