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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

public class CommandChannelWebSocketImpl implements CommandChannelWebSocket {

    private static final Map<String, CommandChannelWebSocketImpl> clientSockets = new ConcurrentHashMap<>();
    private static final Map<String, CommandChannelWebSocketImpl> agentSockets = new ConcurrentHashMap<>();
    // agentId = > client communication mapping
    private static final Map<String, ClientCommunication> inFlightComms = new ConcurrentHashMap<>();
    private final String user;
    private final WebSocketType type;
    private final String id;
    private final Session session;
    private ClientCommunication communication;

    public CommandChannelWebSocketImpl(String user, WebSocketType type, Session session) {
        this(user, type, user, session);
    }

    public CommandChannelWebSocketImpl(String user, WebSocketType type, String id, Session session) {
        this.user = user;
        this.type = type;
        this.id = id;
        this.session = session;
    }

    // package-private for testing
    String getUser() {
        return user;
    }

    // package-private for testing
    WebSocketType getType() {
        return type;
    }

    // package-private for testing
    String getId() {
        return id;
    }

    @Override
    public void onConnect() {
        addSocket();
        System.out.println(toString() + " connected.");
    }

    private void addSocket() {
        switch (type) {
        case AGENT:
            System.out.println("Adding agent socket: " + id);
            agentSockets.put(id, this);
            break;
        case CLIENT:
            System.out.println("Adding client socket: " + user);
            clientSockets.put(user, this);
            break;
        default: // ignore cases we don't know
        }
    }

    @Override
    public void onClose(int closeCode, String reason) {
        System.out.println(toString() + "' closed session.");
        removeSocket();
    }

    private void removeSocket() {
        switch (type) {
        case AGENT:
            agentSockets.remove(id);
            break;
        case CLIENT:
            clientSockets.remove(user);
            break;
        default: // ignore cases we don't know how to handle
        }

    }

    @Override
    public void onSocketMessage(String msg) {
        System.err.println(toString() + " got message: '" + msg + "'");
        try {
            performCommunication(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performCommunication(String msg) throws IOException {
        switch (type) {
        case AGENT:
            communication = inFlightComms.get(id);
            Response resp = Response.fromMessage(msg);
            communication.setAgentResponse(resp);
            communication.perform();
            break;
        case CLIENT:
            Request req = Request.fromMessage(msg);
            String agentId = req.getAgentId();
            if (!agentId.equals(id)) {
                throw new AssertionError("Invalid agentId, expected: " + id);
            }
            System.out.println("target agentId = " + id);
            ClientCommunication other = inFlightComms.get(id);
            if (other != null) {
                throw new AssertionError(
                        "Other agent communication in progress: " + other);
            }
            communication = new ClientCommunication(user, id, req);
            inFlightComms.put(id, communication);
            communication.perform();
            break;
        default:
            // ignore unknown communication
        }

    }

    @Override
    public void onError(Throwable cause) {
        cause.printStackTrace();
        Throwable realCause = cause.getCause();
        while (realCause != null) {
            realCause.printStackTrace();
            realCause = realCause.getCause();
        }
    }

    @Override
    public String toString() {
        return CommandChannelWebSocketImpl.class.getSimpleName() + "["
                + type.name() + "," + user + "," + id + "]";
    }

    private static class ClientCommunication {
        private final String to;
        private final String from;
        private final Request request;
        private CommunicationState state;
        private Response agentResponse;

        private ClientCommunication(String from, String to, Request request) {
            this.from = Objects.requireNonNull(from);
            this.to = Objects.requireNonNull(to);
            this.request = Objects.requireNonNull(request);
            this.state = CommunicationState.INITIAL;
        }

        public void perform() {
            try {
                // Perform the communication
                // 1. web => agent: initial
                // 2. web => client: web_to_agent
                switch (state) {
                case INITIAL:
                    doRelay();
                    break;
                case WEB_TO_AGENT:
                    doFinal();
                    break;
                default:
                    throw new AssertionError("Did not expect this!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void doFinal() throws IOException {
            if (state != CommunicationState.WEB_TO_AGENT) {
                throw new AssertionError("Illegal state: " + state);
            }
            CommandChannelWebSocketImpl clientSocket = clientSockets.get(from);
            state = CommunicationState.WEB_TO_CLIENT;
            if (agentResponse == null) {
                sendResponse(clientSocket, Response.ResponseType.ERROR);
            }
            sendResponse(clientSocket, agentResponse.getType());
            System.out.println("Removing in-flight comm: " + to);
            inFlightComms.remove(to);
        }

        private void doRelay() throws IOException {
            if (state != CommunicationState.INITIAL) {
                throw new AssertionError("Illegal state: " + state);
            }
            CommandChannelWebSocketImpl agentSocket = agentSockets.get(to);
            System.out.println("agent relay: " + agentSocket);
            if (agentSocket == null) {
                state = CommunicationState.WEB_TO_CLIENT;
                CommandChannelWebSocketImpl clientSocket = clientSockets
                        .get(from);
                sendResponse(clientSocket, Response.ResponseType.ERROR);
                return;
            }
            state = CommunicationState.WEB_TO_AGENT;
            relayRequest(agentSocket, request);
        }

        private void relayRequest(CommandChannelWebSocketImpl agentSocket,
                Request req) throws IOException {
            synchronized (agentSocket) {
                Basic remote = agentSocket.session.getBasicRemote();
                remote.sendText(req.asStringMessage());
            }
        }

        private void sendResponse(CommandChannelWebSocketImpl socket,
                Response.ResponseType response) {
            if (state != CommunicationState.WEB_TO_CLIENT) {
                throw new AssertionError("Expected Web => Client");
            }
            if (socket == null) {
                // Socket not found, nothing to do
                return;
            }
            try {
                synchronized (socket) {
                    socket.session.getBasicRemote().sendText(response.name());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            state = CommunicationState.DONE;
        }

        public void setAgentResponse(Response resp) {
            this.agentResponse = resp;
        }

        @Override
        public String toString() {
            return ClientCommunication.class.getSimpleName() + "[from=" + from + ",to=" + to + "]";
        }
    }

    private enum CommunicationState {
        INITIAL,
        WEB_TO_AGENT,
        WEB_TO_CLIENT,
        DONE
    }
}
