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

package com.redhat.thermostat.server.core.internal.web.http.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.junit.Test;

import com.redhat.thermostat.server.core.web.setup.BasicCoreServerTestSetup;

public class CommandChannelEndpointHandlerTest
        extends BasicCoreServerTestSetup {

    @Test
    public void testHandshakeAllRoles() throws Exception {
        String agentUser = "foo-agent-user";
        String clientUser = "bar-client-user";
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc");
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                agentId);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {
                    @Override
                    public void run(Session session, String msg) {
                        System.out.printf("Got cmd-channel request.");
                        Request req = Request.fromMessage(msg);
                        System.out.println("Got request for VM ID: "
                                + req.getParam("vmId"));
                        System.out.println("Sending OK response.");
                        try {
                            session.getRemote().sendString(
                                    Response.ResponseType.OK.name());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Bye now!");
                    }
                });
        ClientUpgradeRequest clientRequest = new ClientUpgradeRequest();
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(agentUser, "agent-pwd"));
        clientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(clientUser, "client-pwd"));
        client.connect(agentSocket, agentUri, agentRequest);
        client.connect(clientSocket, clientUri, clientRequest);
        // wait for closed socket connection.
        clientSocket.awaitClose(2, TimeUnit.SECONDS);
        agentSocket.closeSession();
        agentSocket.awaitClose(2, TimeUnit.SECONDS);
        assertNotNull(clientSocket.getResponse());
        assertEquals(Response.ResponseType.OK,
                clientSocket.getResponse().getType());
    }

    @Test
    public void testHandshakeNotAuthenticated() throws Exception {
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc");
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                agentId);
        final String[] agentResponse = new String[1];
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {

                    @Override
                    public void run(Session session, String msg) {
                        agentResponse[0] = msg;
                    }
                });
        ClientUpgradeRequest clientRequest = new ClientUpgradeRequest();
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        client.connect(agentSocket, agentUri, agentRequest);
        client.connect(clientSocket, clientUri, clientRequest);
        // wait for closed socket connection.
        clientSocket.awaitClose(2, TimeUnit.SECONDS);
        agentSocket.closeSession();
        agentSocket.awaitClose(2, TimeUnit.SECONDS);
        assertNotNull(clientSocket.getResponse());
        Response agentResp = Response.fromMessage(agentResponse[0]);
        assertEquals(Response.ResponseType.AUTH_FAIL, agentResp.getType());
        assertEquals(Response.ResponseType.AUTH_FAIL,
                clientSocket.getResponse().getType());
    }

    @Test
    public void testHandshakeNotAuthorized() throws Exception {
        String agentUser = "insufficient-roles-agent";
        String clientUser = "insufficient-roles-client";
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc");
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                agentId);
        final String[] agentResponse = new String[1];
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {

                    @Override
                    public void run(Session session, String msg) {
                        agentResponse[0] = msg;
                    }
                });
        ClientUpgradeRequest clientRequest = new ClientUpgradeRequest();
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(agentUser, "agent-pwd"));
        clientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(clientUser, "client-pwd"));
        client.connect(agentSocket, agentUri, agentRequest);
        client.connect(clientSocket, clientUri, clientRequest);
        // wait for closed socket connection.
        clientSocket.awaitClose(2, TimeUnit.SECONDS);
        agentSocket.closeSession();
        agentSocket.awaitClose(2, TimeUnit.SECONDS);
        assertNotNull(clientSocket.getResponse());
        Response agentResp = Response.fromMessage(agentResponse[0]);
        assertEquals(Response.ResponseType.AUTH_FAIL, agentResp.getType());
        assertEquals(Response.ResponseType.AUTH_FAIL,
                clientSocket.getResponse().getType());
    }

    private String getBasicAuthHeaderValue(String testUser, String password) {
        String userpassword = testUser + ":" + password;
        @SuppressWarnings("restriction")
        String encodedAuthorization = new sun.misc.BASE64Encoder()
                .encode(userpassword.getBytes());
        return "Basic " + encodedAuthorization;
    }

}
