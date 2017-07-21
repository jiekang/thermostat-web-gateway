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

package com.redhat.thermostat.gateway.service.commands.http.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.redhat.thermostat.gateway.service.commands.channel.coders.typeadapters.MessageTypeAdapterFactory;
import com.redhat.thermostat.gateway.service.commands.channel.model.AgentRequest;
import com.redhat.thermostat.gateway.service.commands.channel.model.ClientRequest;
import com.redhat.thermostat.gateway.service.commands.channel.model.Message;
import com.redhat.thermostat.gateway.service.commands.channel.model.Message.MessageType;
import com.redhat.thermostat.gateway.service.commands.channel.model.WebSocketResponse;
import com.redhat.thermostat.gateway.service.commands.channel.model.WebSocketResponse.ResponseType;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CommandChannelEndpointHandlerTest extends AuthBasicCoreServerTest {

    private Gson gson;
    private ClientRequest clientRequest;

    @Before
    public void setUp() {
        gson = new GsonBuilder()
                .registerTypeAdapterFactory(new MessageTypeAdapterFactory())
                .serializeNulls()
                .create();
        clientRequest = new ClientRequest(Message.UNKNOWN_SEQUENCE);
    }

    @Test(timeout = 2000)
    public void testHandshakeAllRoles() throws Exception {
        String agentUser = "foo-agent-user";
        String clientUser = "bar-client-user";
        long clientSequence = 144L;
        clientRequest.setSequenceId(clientSequence);
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + clientSequence);
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        final CountDownLatch clientHasSentMessages = new CountDownLatch(1);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(clientRequest, clientHasSentMessages);
        final CountDownLatch waitForAgentConnect = new CountDownLatch(1);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {
                    @Override
                    public void run(Session session, Message msg) {
                        if (msg.getMessageType() != MessageType.AGENT_REQUEST) {
                            throw new AssertionError("Wrong message type. Got: " + msg.getClass().getName());
                        }
                        AgentRequest req = (AgentRequest)msg;
                        WebSocketResponse resp = new WebSocketResponse(req.getSequenceId(), ResponseType.OK);
                        String jsonResp = gson.toJson(resp);
                        try {
                            session.getRemote().sendString(jsonResp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, waitForAgentConnect, gson);
        ClientUpgradeRequest clientRequest = new ClientUpgradeRequest();
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(agentUser, "agent-pwd"));
        clientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(clientUser, "client-pwd"));

        // Ensure agent conneted before we actually do anything
        client.connect(agentSocket, agentUri, agentRequest);
        waitForAgentConnect.await();

        // Ensure client has connected and initiated handshake by
        // sending its message
        client.connect(clientSocket, clientUri, clientRequest);
        clientHasSentMessages.await();

        // wait for client to close the socket
        clientSocket.awaitClose();

        // now we are ready to close the agent socket too
        agentSocket.closeSession();
        agentSocket.awaitClose();

        assertNotNull(clientSocket.getResponse());
        assertEquals(WebSocketResponse.ResponseType.OK,
                clientSocket.getResponse().getResponseType());
    }

    /**
     * A client which tries to communicate with an agent that has not yet
     * connected should fail.
     *
     * @throws Exception
     */
    @Test(timeout = 2000)
    public void testHandshakeAuthorizedMissingAgentConnect() throws Exception {
        long sequenceId = 333L;
        clientRequest.setSequenceId(sequenceId);
        String clientUser = "bar-client-user";
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + sequenceId);
        final CountDownLatch clientHasSentMessages = new CountDownLatch(1);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(clientRequest, clientHasSentMessages);
        ClientUpgradeRequest clientRequest = new ClientUpgradeRequest();
        clientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(clientUser, "client-pwd"));
        client.connect(clientSocket, clientUri, clientRequest);

        boolean clientConnected = clientHasSentMessages.await(1, TimeUnit.SECONDS);
        // For some reason the client might not connect when stress-tested.
        // Avoid false test failures by using assumeTrue
        assumeTrue(clientConnected);

        // wait for closed socket connection.
        clientSocket.awaitClose();
        assertNotNull(clientSocket.getResponse());
        assertEquals(WebSocketResponse.ResponseType.ERROR,
                clientSocket.getResponse().getResponseType());
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
        ClientRequest r1 = new ClientRequest(clientSequenceFirst);
        ClientRequest r2 = new ClientRequest(clientSequenceSecond);

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
        CmdChannelClientSocket firstClientSocket = new CmdChannelClientSocket(r1, clientsHaveSentMessages);
        CmdChannelClientSocket secondClientSocket = new CmdChannelClientSocket(r2, clientsHaveSentMessages);
        final CountDownLatch waitForSecondClientConnect = new CountDownLatch(1);
        final CountDownLatch waitForAgentConnect = new CountDownLatch(1);
        final AtomicInteger agentRespCount = new AtomicInteger(0);
        final CountDownLatch allResponsesSent = new CountDownLatch(2);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {
                    @Override
                    public void run(Session session, Message msg) {
                        if (agentRespCount.get() == 0) {
                            try {
                                waitForSecondClientConnect.await(2, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                // ignore
                            }
                        }
                        if (msg.getMessageType() != MessageType.AGENT_REQUEST) {
                            throw new AssertionError("Wrong message type. Got: " + msg.getClass().getName());
                        }
                        AgentRequest req = (AgentRequest)msg;
                        agentRespCount.getAndAdd(1);
                        try {
                            if (req.getSequenceId() == clientSequenceFirst) {
                                WebSocketResponse resp = new WebSocketResponse(req.getSequenceId(), ResponseType.OK);
                                session.getRemote().sendString(gson.toJson(resp));
                                session.getRemote().flush();
                                allResponsesSent.countDown();
                            } else if (req.getSequenceId() == clientSequenceSecond) {
                                WebSocketResponse resp = new WebSocketResponse(req.getSequenceId(), ResponseType.ERROR);
                                session.getRemote().sendString(gson.toJson(resp));
                                session.getRemote().flush();
                                allResponsesSent.countDown();
                            } else {
                                throw new AssertionError("Should not get here");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, waitForAgentConnect, gson);
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
        boolean isAllResponsesSent = allResponsesSent.await(1, TimeUnit.SECONDS);
        // We might hit a window where we've got the signal that the agent is
        // "ready", but actually still in progress of getting its socket added
        // to the registry. In that case, the agent call-back is not going to be
        // sending the response. Instead the server aborts with ERROR, since it
        // cannot find the socket in the agent socket registry yet. assumeTrue
        // that the latch did not expire in order to avoid false test failures.
        assumeTrue(isAllResponsesSent);

        // wait for client connections to close
        secondClientSocket.awaitClose();
        firstClientSocket.awaitClose();

        agentSocket.closeSession();
        agentSocket.awaitClose();

        assertNotNull(firstClientSocket.getResponse());
        assertEquals(WebSocketResponse.ResponseType.OK,
                firstClientSocket.getResponse().getResponseType());
        assertNotNull(secondClientSocket.getResponse());
        assertEquals(WebSocketResponse.ResponseType.ERROR, secondClientSocket.getResponse().getResponseType());
    }

    @Test(timeout = 2000)
    public void testHandshakeNotAuthenticatedClient() throws Exception {
        long clientSequence = 324L;
        TestUser clientUser = null /* no auth-creds */;
        doNoAuthTestClient(clientSequence, clientUser);
    }

    @Test(timeout = 2000)
    public void testHandshakeNotAuthenticatedAgent() throws Exception {
        TestUser agentUser = null /* no auth-creds */;
        doNoAuthTestAgent(agentUser);
    }

    @Test(timeout = 2000)
    public void testHandshakeNotAuthorizedClient() throws Exception {
        long clientSequence = 332l;
        TestUser clientUser = new TestUser();
        clientUser.username = "insufficient-roles-client";
        clientUser.password = "client-pwd";
        doNoAuthTestClient(clientSequence, clientUser);
    }

    @Test(timeout = 2000)
    public void testHandshakeNotAuthorizedAgent() throws Exception {
        TestUser agentUser = new TestUser();
        agentUser.username = "insufficient-roles-agent";
        agentUser.password = "agent-pwd";
        doNoAuthTestAgent(agentUser);
    }

    /**
     * Tests whether the client can ping the server end-point. A server response is
     * expected.
     *
     * @throws Exception
     */
    @Test(timeout = 2000)
    public void testAgentPing() throws Exception {
        String agentUser = "foo-agent-user";
        String agentId = "testAgent";
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        final CountDownLatch waitForAgentConnect = new CountDownLatch(1);
        final CountDownLatch pongSignal = new CountDownLatch(1);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(waitForAgentConnect, pongSignal, false);
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(agentUser, "agent-pwd"));

        // Ensure agent conneted before we actually do anything
        client.connect(agentSocket, agentUri, agentRequest);
        waitForAgentConnect.await();

        // Initiate ping from client
        String pingMsgPayload = "testAgent ping message";
        agentSocket.sendPingToServer(pingMsgPayload);

        // Wait for server pong to come back
        pongSignal.await();

        assertEquals(pingMsgPayload, agentSocket.getPongMsg());

        // now we are ready to close the agent socket too
        agentSocket.closeSession();
        agentSocket.awaitClose();
    }

    /**
     * Tests whether a server ping is properly responded to by the client by sending a
     * pong response.
     *
     * Note: The service sends a ping onConnect() and then every subsequent X minutes
     *       where X is strictly less than the currently set timeout value for the
     *       agent/receiver sockets.
     *
     * @throws Exception
     */
    @Test(timeout = 2000)
    public void testAgentPong() throws Exception {
        String agentUser = "foo-agent-user";
        String agentId = "testAgent";
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        final CountDownLatch waitForAgentConnect = new CountDownLatch(1);
        final CountDownLatch pongResponseSent = new CountDownLatch(1);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(waitForAgentConnect, pongResponseSent, true);
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                getBasicAuthHeaderValue(agentUser, "agent-pwd"));

        // Ensure agent conneted before we actually do anything
        client.connect(agentSocket, agentUri, agentRequest);
        waitForAgentConnect.await();

        String expectedPingPayload = "1|" + agentId;
        pongResponseSent.await();
        assertEquals(expectedPingPayload, agentSocket.getPingMsg());

        // now we are ready to close the agent socket too
        agentSocket.closeSession();
        agentSocket.awaitClose();
    }

    private void doNoAuthTestClient(long clientSequence, TestUser clientUser) throws Exception {
        ClientRequest noMatter = new ClientRequest(clientSequence);
        String agentId = "testAgent";
        URI clientUri = new URI(
                baseUrl + "actions/dump-heap/systems/foo/agents/" + agentId
                        + "/jvms/abc/sequence/" + clientSequence);
        CmdChannelClientSocket clientSocket = new CmdChannelClientSocket(
                 noMatter /* doesn't matter */);
        ClientUpgradeRequest clientRequest = new ClientUpgradeRequest();
        if (clientUser != null) {
            clientRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                    getBasicAuthHeaderValue(clientUser.username, clientUser.password));
        }
        client.connect(clientSocket, clientUri, clientRequest);

        // wait for client connection to get closed (by the server)
        clientSocket.awaitClose();

        WebSocketResponse resp = clientSocket.getResponse();
        // Sometimes the client response is not sent due to EOF (short reads?) on the
        // underlying socket. In order to avoid false test failures use assumeTrue.
        // Symptoms:
        //  "Connection closed: 1006 - EOF: Broken pipe" or
        //  "Connection closed: 1006 - WebSocket Read EOF"
        assumeTrue(resp != null);
        assertNotNull(resp);
        assertEquals(WebSocketResponse.ResponseType.AUTH_FAIL,
                resp.getResponseType());
        assertEquals(clientSequence, resp.getSequenceId());
    }

    private void doNoAuthTestAgent(TestUser agentUser) throws Exception {
        String agentId = "testAgent";
        URI agentUri = new URI(baseUrl + "systems/foo/agents/" + agentId);
        final WebSocketResponse[] agentResponse = new WebSocketResponse[1];
        final CountDownLatch agentResponseReady = new CountDownLatch(1);
        CmdChannelAgentSocket agentSocket = new CmdChannelAgentSocket(
                new CmdChannelAgentSocket.OnMessageCallBack() {

                    @Override
                    public void run(Session session, Message msg) {
                        if (msg.getMessageType() != MessageType.RESPONSE) {
                            throw new AssertionError("Wrong message type. Got: " + msg.getClass().getName());
                        }
                        agentResponse[0] = (WebSocketResponse)msg;
                        agentResponseReady.countDown();
                    }
                }, agentResponseReady, gson);
        ClientUpgradeRequest agentRequest = new ClientUpgradeRequest();
        if (agentUser != null) {
            agentRequest.setHeader(HttpHeader.AUTHORIZATION.asString(),
                    getBasicAuthHeaderValue(agentUser.username, agentUser.password));
        }
        client.connect(agentSocket, agentUri, agentRequest);

        boolean isAgentResponseReady = agentResponseReady.await(1, TimeUnit.SECONDS);
        // Sometimes the agent response is not sent due to EOF (short reads?) on the
        // underlying socket. In order to avoid false test failures use assumeTrue.
        // Symptoms:
        //  "Connection closed: 1006 - EOF: Broken pipe" or
        //  "Connection closed: 1006 - WebSocket Read EOF"
        assumeTrue(isAgentResponseReady);

        // wait for the agent connection to get closed (by the server)
        agentSocket.awaitClose();

        assertEquals("There is no sequence for agent connections",
                     Message.UNKNOWN_SEQUENCE, agentResponse[0].getSequenceId());
    }

    private String getBasicAuthHeaderValue(String testUser, String password) {
        String userpassword = testUser + ":" + password;
        @SuppressWarnings("restriction")
        String encodedAuthorization = new sun.misc.BASE64Encoder()
                .encode(userpassword.getBytes());
        return "Basic " + encodedAuthorization;
    }

    private static class TestUser {
        String password;
        String username;
    }
}
