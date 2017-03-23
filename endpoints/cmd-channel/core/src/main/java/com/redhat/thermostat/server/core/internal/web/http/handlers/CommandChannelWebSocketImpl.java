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

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import com.redhat.thermostat.server.core.internal.web.cmdchannel.AgentSocketsRegistry;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.ClientAgentCommunication;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.ClientRequest;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.ClientRequestFactory;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.CommunicationsRegistry;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.Response;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.Response.ResponseType;
import com.redhat.thermostat.server.core.internal.web.cmdchannel.WebSocketCommunicationBuilder;

public class CommandChannelWebSocketImpl implements CommandChannelWebSocket {

    private static final String SEQ_ID_PATH_PARAM_NAME = "seqId";
    private final String user;
    private final WebSocketType type;
    private final String id;
    private final Session session;

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
        System.err.println(toString() + " connected.");
    }

    private void addSocket() {
        switch (type) {
        case AGENT:
            System.err.println("Adding agent socket: " + id);
            AgentSocketsRegistry.addSocket(id, this.session);
            break;
        default: // ignore cases we don't need/know
        }
    }

    @Override
    public void onClose(int closeCode, String reason) {
        System.err.println(toString() + "' closed session.");
        removeSocket();
    }

    private void removeSocket() {
        switch (type) {
        case AGENT:
            AgentSocketsRegistry.removeSocket(id);
            break;
        default: // ignore cases we don't need/know
        }

    }

    @Override
    public void onSocketMessage(String msg) {
        System.err.println(toString() + " got message: '" + msg + "'");
        try {
            performCommunication(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performCommunication(String msg) throws IOException {
        switch (type) {
        case AGENT: {
            Response resp = Response.fromMessage(msg);
            String commId = id + resp.getSequenceId();
            ClientAgentCommunication comm = CommunicationsRegistry.get(commId);
            comm.responseReceived(resp);
            break;
        }
        case CLIENT: {
            // We take the sequence id from the path parameter since we potentially
            // used it when checking relevant roles
            String seqPathParam = session.getPathParameters().get(SEQ_ID_PATH_PARAM_NAME);
            long sequence = Response.UNKNOWN_SEQUENCE;
            try {
                sequence = Long.parseLong(seqPathParam);
            } catch (NumberFormatException e) {
                sendErrorResponse(sequence /* will be unknown */, this.session);
            }
            Session agentSession = AgentSocketsRegistry.getSession(id);
            if (agentSession == null) {
                // the agent the client wants to talk to has not connected yet.
                sendErrorResponse(sequence, this.session);
                return;
            }
            ClientRequest req = ClientRequestFactory.fromMessage(msg, sequence);
            ClientAgentCommunication clientAgentComm = new WebSocketCommunicationBuilder()
                                .setRequest(req)
                                .setAgentSession(agentSession)
                                .setClientSession(this.session)
                                .build();
            String commId = id + req.getSequenceId();
            CommunicationsRegistry.add(commId, clientAgentComm);
            Response resp = clientAgentComm.perform();
            System.out.println("Server: sent response: " + resp);
        }
        default:
            // ignore unknown communication
        }
    }

    private void sendErrorResponse(long sequence, Session clientSession) {
        Response resp = new Response(sequence, ResponseType.ERROR);
        try {
            synchronized(clientSession) {
                Basic remote = clientSession.getBasicRemote();
                remote.sendText(resp.asStringMesssage());
            }
        } catch (IOException e) {
            e.printStackTrace(); // cannot really do more. We've lost client connectivity
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
}
