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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.junit.Test;

import com.redhat.thermostat.server.core.internal.web.cmdchannel.AgentRequest;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.AgentRequestFactory;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.WebSocketResponse;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.WebSocketResponse.ResponseType;
import com.redhat.thermostat.server.core.web.setup.BasicCoreServerTestSetup;

public class CommandChannelEndpointHandlerTest
        extends BasicCoreServerTestSetup {

    @Test
    public void testHandshakeAllRoles() throws Exception {
        String agentUser = "foo-agent-user";
        String clientUser = "bar-client-user";
        long clientSequence = 1L;
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + clientSequence);
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                clientSequence);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {
                    @Override
                    public void run(Session session, String msg) {
                        System.out.printf("Got cmd-channel request.");
                        AgentRequest req = AgentRequestFactory.fromMessage(msg);
                        System.out.println("Got request for VM ID: "
                                + req.getParam("vmId"));
                        System.out.println("Sending OK response.");
                        WebSocketResponse resp = new WebSocketResponse(req.getSequenceId(), ResponseType.OK);
                        try {
                            session.getRemote().sendString(
                                    resp.asStringMesssage());
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
        assertEquals(WebSocketResponse.ResponseType.OK,
                clientSocket.getResponse().getType());
    }

    /**
     * A client which tries to communicate with an agent that has not yet
     * connected should fail.
     *
     * @throws Exception
     */
    @Test
    public void testHandshakeAuthorizedMissingAgentConnect() throws Exception {
        long sequenceId = 333L;
        String clientUser = "bar-client-user";
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + sequenceId);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                sequenceId);
        ClientUpgradeRequest clientRequest = new ClientUpgradeRequest();
        clientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(clientUser, "client-pwd"));
        client.connect(clientSocket, clientUri, clientRequest);
        // wait for closed socket connection.
        clientSocket.awaitClose(2, TimeUnit.SECONDS);
        assertNotNull(clientSocket.getResponse());
        assertEquals(WebSocketResponse.ResponseType.ERROR,
                clientSocket.getResponse().getType());
    }

    /**
     * There is no guarantee in which order messages get processed on the server
     * side. Multiple clients might connect before an agent responds. It's possible
     * that one channel connect which happened later
     * channel request is faster than another. This test simulates some of
     * this behavior.
     *
     * @throws Exception
     */
    @Test(timeout = 2000)
    public void testMultipleHandshakesInterleavedAllRoles() throws Exception {
        final long clientSequenceFirst = 901l;
        final long clientSequenceSecond = 902l;
        String agentUser = "foo-agent-user";
        String clientUser = "bar-client-user";
        String clientPassword = "client-pwd";
        String agentId = "testAgent";
        URI firstClientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + clientSequenceFirst);
        URI secondClientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + clientSequenceSecond);
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        CountDownLatch clientsHaveSentMessages = new CountDownLatch(2);
        CmdChannelClientSocket firstClientSocket = new CmdChannelClientSocket(clientSequenceFirst, clientsHaveSentMessages);
        CmdChannelClientSocket secondClientSocket = new CmdChannelClientSocket(clientSequenceSecond, clientsHaveSentMessages);
        final CountDownLatch waitForSecondClientConnect = new CountDownLatch(1);
        final CountDownLatch waitForAgentConnect = new CountDownLatch(1);
        final AtomicInteger agentRespCount = new AtomicInteger(0);
        final CountDownLatch allResponsesSent = new CountDownLatch(2);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {
                    @Override
                    public void run(Session session, String msg) {
                        System.err.println("running callback");
                        if (agentRespCount.get() == 0) {
                            try {
                                waitForSecondClientConnect.await(2, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                        int count = agentRespCount.getAndAdd(1);
                        System.out.printf("Got cmd-channel request.");
                        AgentRequest req = AgentRequestFactory.fromMessage(msg);
                        System.out.println("Got request " + count + " for VM ID: "
                                + req.getParam("vmId"));
                        try {
                            if (req.getSequenceId() == clientSequenceFirst) {
                                WebSocketResponse resp = new WebSocketResponse(req.getSequenceId(), ResponseType.OK);
                                session.getRemote().sendString(
                                        resp.asStringMesssage());
                                session.getRemote().flush();
                                allResponsesSent.countDown();
                            } else if (req.getSequenceId() == clientSequenceSecond) {
                                WebSocketResponse resp = new WebSocketResponse(req.getSequenceId(), ResponseType.ERROR);
                                session.getRemote().sendString(
                                        resp.asStringMesssage());
                                session.getRemote().flush();
                                allResponsesSent.countDown();
                            } else {
                                throw new AssertionError("Should not get here");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Bye now!");
                    }
                }, waitForAgentConnect);
        ClientUpgradeRequest firstClientRequest = new ClientUpgradeRequest();
        ClientUpgradeRequest secondClientRequest = new ClientUpgradeRequest();
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(agentUser, "agent-pwd"));
        firstClientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(clientUser, clientPassword));
        secondClientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(clientUser, clientPassword));
        client.connect(agentSocket, agentUri, agentRequest);
        waitForAgentConnect.await();

        client.connect(firstClientSocket, firstClientUri, firstClientRequest);
        client.connect(secondClientSocket, secondClientUri, secondClientRequest);

        // There is no guarantee that the second client actually connected, but
        // at least we know that we've called relevant code to do the connection.
        waitForSecondClientConnect.countDown();

        clientsHaveSentMessages.await();

        // wait before clients have been processed before we close the agent
        allResponsesSent.await();

        // wait for closed socket connection.
        secondClientSocket.awaitClose(2, TimeUnit.SECONDS);
        firstClientSocket.awaitClose(2, TimeUnit.SECONDS);

        agentSocket.closeSession();
        agentSocket.awaitClose(2, TimeUnit.SECONDS);
        assertNotNull(firstClientSocket.getResponse());
        assertEquals(WebSocketResponse.ResponseType.OK,
                firstClientSocket.getResponse().getType());
        assertNotNull(secondClientSocket.getResponse());
        assertEquals(WebSocketResponse.ResponseType.ERROR, secondClientSocket.getResponse().getType());
    }

    @Test
    public void testHandshakeNotAuthenticated() throws Exception {
        long clientSequence = 324L;
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + clientSequence);
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                 clientSequence /* doesn't matter */);
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
        WebSocketResponse agentResp = WebSocketResponse.fromMessage(agentResponse[0]);
        assertEquals(WebSocketResponse.ResponseType.AUTH_FAIL, agentResp.getType());
        assertEquals(WebSocketResponse.ResponseType.AUTH_FAIL,
                clientSocket.getResponse().getType());
    }

    @Test
    public void testHandshakeNotAuthorized() throws Exception {
        long clientSequence = 332l;
        String agentUser = "insufficient-roles-agent";
        String clientUser = "insufficient-roles-client";
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + clientSequence);
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                 clientSequence /* does not matter */);
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
        WebSocketResponse agentResp = WebSocketResponse.fromMessage(agentResponse[0]);
        assertEquals(WebSocketResponse.ResponseType.AUTH_FAIL, agentResp.getType());
        assertEquals(WebSocketResponse.ResponseType.AUTH_FAIL,
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
