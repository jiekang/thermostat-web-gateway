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

package com.redhat.thermostat.server.core.web;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.Response;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.server.core.web.setup.CoreServerTestSetup;

public class CoreServerHttpTest extends CoreServerTestSetup {
    @Test
    public void testGetSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testGetJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPutSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.PUT).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPutAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.PUT).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPutJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.PUT).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPostSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.POST).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPostAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.POST).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testPostJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.POST).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testDeleteSystems() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.DELETE).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testDeleteAgents() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.DELETE).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void testDeleteJvms() throws InterruptedException, ExecutionException, TimeoutException {
        String url = baseUrl + "/namespace/systems/systemId/agents/agentId/jvms/jvmId";
        ContentResponse response = client.newRequest(url).method(HttpMethod.DELETE).send();
        assertTrue(response.getStatus() == Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }
}
