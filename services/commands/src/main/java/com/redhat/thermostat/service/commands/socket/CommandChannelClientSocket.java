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

package com.redhat.thermostat.service.commands.socket;

import java.io.IOException;

import javax.websocket.EncodeException;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import com.redhat.thermostat.gateway.common.core.auth.basic.RoleAwareUser;
import com.redhat.thermostat.service.commands.channel.ClientAgentCommunication;
import com.redhat.thermostat.service.commands.channel.CommunicationsRegistry;
import com.redhat.thermostat.service.commands.channel.WebSocketCommunicationBuilder;
import com.redhat.thermostat.service.commands.channel.model.ClientRequest;
import com.redhat.thermostat.service.commands.channel.model.Message;
import com.redhat.thermostat.service.commands.channel.model.WebSocketResponse;
import com.redhat.thermostat.service.commands.channel.model.WebSocketResponse.ResponseType;

class CommandChannelClientSocket extends CommandChannelSocket {

    private static final String CLIENT_GRANT_ACTION_PREFIX = "thermostat-commands-grant-";
    private static final String CLIENT_GRANT_JVM_PREFIX = "thermostat-commands-grant-jvm-";
    private static final String PATH_PARAM_ACTION = "action";
    private static final String PATH_PARAM_JVM = "jvmId";
    private static final String PATH_PARAM_SEQ_ID = "seqId";
    private final long socketTimeout;

    CommandChannelClientSocket(String id, Session session) {
        super(id, session);
        this.socketTimeout = session.getMaxIdleTimeout();
    }

    @Override
    public void performCommunication(Message msg) throws IOException {
        // We take the sequence id from the path parameter. It's the only
        // place where this is available for client sockets.
        String seqPathParam = session.getPathParameters().get(PATH_PARAM_SEQ_ID);
        long sequence = Message.UNKNOWN_SEQUENCE;
        try {
            sequence = Long.parseLong(seqPathParam);
        } catch (NumberFormatException e) {
            sendErrorResponse(sequence /* will be unknown */);
        }
        // Note: agent/client session will have the same default timeout.
        //       Thus, it's fine to use the client's set timeout value for
        //       retrieving the agent registry.
        AgentSocketsRegistry agentRegistry = AgentSocketsRegistry.getInstance(socketTimeout);
        Session agentSession = agentRegistry.getSession(agentId);
        if (agentSession == null) {
            // the agent the client wants to talk to has not connected yet.
            sendErrorResponse(sequence);
            return;
        }
        if (!(msg instanceof ClientRequest)) {
            throw new IllegalStateException("Received message of unknown type: " + msg.getClass().getName());
        }
        ClientRequest req = (ClientRequest)msg;
        req.setSequenceId(sequence);
        ClientAgentCommunication clientAgentComm = new WebSocketCommunicationBuilder()
                            .setRequest(req)
                            .setAgentSession(agentSession)
                            .setClientSession(this.session)
                            .build();
        String commId = agentId + req.getSequenceId();
        CommunicationsRegistry.add(commId, clientAgentComm);
        clientAgentComm.perform();
    }

    private void sendErrorResponse(long sequence) {
        WebSocketResponse resp = new WebSocketResponse(sequence, ResponseType.ERROR);
        try {
            synchronized(session) {
                Basic remote = session.getBasicRemote();
                remote.sendObject(resp);
            }
        } catch (IOException|EncodeException e) {
            e.printStackTrace(); // cannot really do more. We've lost client connectivity
        }
    }

    @Override
    protected String getSequence() {
        return session.getPathParameters().get(PATH_PARAM_SEQ_ID);
    }

    @Override
    protected boolean checkRoles() {
        // FIXME: relies on RoleAwareUser - i.e. specific auth scheme.
        RoleAwareUser user = (RoleAwareUser) session.getUserPrincipal();
        String action = session.getPathParameters().get(PATH_PARAM_ACTION);
        String actionAllowedRole = CLIENT_GRANT_ACTION_PREFIX + action;
        if (!user.isUserInRole(actionAllowedRole)) {
            return false;
        }
        String jvm = session.getPathParameters().get(PATH_PARAM_JVM);
        String jvmAllowedRole = CLIENT_GRANT_JVM_PREFIX + jvm;
        if (!user.isUserInRole(jvmAllowedRole)) {
            return false;
        }
        return true;
    }
}
