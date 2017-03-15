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

public class PostMongoCoreServerTest extends MongoCoreServerTestSetup {

    @Test
    public void testEmptyStringPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/EmptyStringPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        System.out.println(postResponse.getContentAsString());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testEmptyQueryPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/EmptyQueryPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        System.out.println(postResponse.getContentAsString());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\" },\\{ \"agentStuff\" : \"b\" }],\"time\" : \"[0-9]*\"}"));
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
        String putInput = "[{\"agentStuff\":5},{\"agentStuff\":10}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff<=10\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        System.out.println(postResponse.getContentAsString());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : 5 },\\{ \"agentStuff\" : 10 }],\"time\" : \"[0-9]*\"}"));
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
        String putInput = "[{\"agentStuff\":\"a\", \"item\":1},{\"agentStuff\":\"a\", \"item\":2}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff=\\\"a\\\"\", \"item<2\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        System.out.println(postResponse.getContentAsString());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"item\" : 1 }],\"time\" : \"[0-9]*\"}"));
    }

    @Test
    public void testQueryStringPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/QueryStringPostAgents/systems/all/agents/agentId";
        String putInput = "[{\"agentStuff\":\"a\", \"item\":\"1\"},{\"agentStuff\":\"a\", \"item\":\"2\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(putInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        String postInput = "[\"agentStuff=\\\"a\\\"\", \"item<\\\"2\\\"\"]";
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST).content(new StringContentProvider(postInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());
        System.out.println(postResponse.getContentAsString());
        assertTrue(postResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"item\" : \"1\" }],\"time\" : \"[0-9]*\"}"));
    }
}
