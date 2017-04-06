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

public class DeleteMongoCoreServerTest extends MongoCoreServerTestSetup {
    @Test
    public void testDeleteAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/DeleteAgents/systems/*/agents/agentId";
        String input = "[{\"agentStuff\":\"a\"},{\"agentStuff\":\"b\"}]";
        ContentResponse putResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(input), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putResponse.getStatus());
        assertEquals("PUT: true", putResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"agentId\" : \"agentId\" },\\{ \"agentStuff\" : \"b\", \"agentId\" : \"agentId\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse deleteResponse = client.newRequest(url).method(HttpMethod.DELETE).send();

        assertEquals(Response.Status.OK.getStatusCode(), deleteResponse.getStatus());
        assertEquals("DELETE: true", deleteResponse.getContentAsString());

        ContentResponse getAfterResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getAfterResponse.getStatus());
        assertTrue(getAfterResponse.getContentAsString().matches("\\{\"response\" : \\[],\"time\" : \"[0-9]*\"}"));
    }
    @Test
    public void testDeleteSpecificAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/DeleteSpecificAgents/systems/*/agents";

        String oneUrl = url + "/one";
        String twoUrl = url + "/two";
        String allUrl = url + "/*";

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
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"agentId\" : \"one\" },\\{ \"agentStuff\" : \"b\", \"agentId\" : \"one\" },\\{ \"otherStuff\" : \"c\", \"agentId\" : \"two\" },\\{ \"otherStuff\" : \"d\", \"agentId\" : \"two\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse deleteResponse = client.newRequest(oneUrl).method(HttpMethod.DELETE).send();

        assertEquals(Response.Status.OK.getStatusCode(), deleteResponse.getStatus());
        assertEquals("DELETE: true", deleteResponse.getContentAsString());

        ContentResponse getAfterResponse = client.newRequest(allUrl).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getAfterResponse.getStatus());
        assertTrue(getAfterResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"otherStuff\" : \"c\", \"agentId\" : \"two\" },\\{ \"otherStuff\" : \"d\", \"agentId\" : \"two\" }],\"time\" : \"[0-9]*\"}"));
    }
    @Test
    public void testDeleteQueries() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/testDeleteQueries/systems/*/agents/one";

        String oneInput = "[{\"agentStuff\":\"a\", \"item\":1},{\"agentStuff\":\"b\", \"item\":2}]";

        ContentResponse putOneResponse = client.newRequest(url).method(HttpMethod.PUT).content(new StringContentProvider(oneInput), "application/json").send();

        assertEquals(Response.Status.OK.getStatusCode(), putOneResponse.getStatus());
        assertEquals("PUT: true", putOneResponse.getContentAsString());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        assertTrue(getResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"a\", \"item\" : 1, \"agentId\" : \"one\" },\\{ \"agentStuff\" : \"b\", \"item\" : 2, \"agentId\" : \"one\" }],\"time\" : \"[0-9]*\"}"));

        ContentResponse deleteResponse = client.newRequest(url).method(HttpMethod.DELETE).param("q","item==1").send();

        assertEquals(Response.Status.OK.getStatusCode(), deleteResponse.getStatus());
        assertEquals("DELETE: true", deleteResponse.getContentAsString());

        ContentResponse getAfterResponse = client.newRequest(url).method(HttpMethod.GET).send();

        assertEquals(Response.Status.OK.getStatusCode(), getAfterResponse.getStatus());
        assertTrue(getAfterResponse.getContentAsString().matches("\\{\"response\" : \\[\\{ \"agentStuff\" : \"b\", \"item\" : 2, \"agentId\" : \"one\" }],\"time\" : \"[0-9]*\"}"));
    }
}
