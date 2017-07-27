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

package com.redhat.thermostat.gateway.service.commands.channel;

import java.util.Objects;

import javax.websocket.Session;

import com.redhat.thermostat.gateway.service.commands.channel.model.AgentRequest;
import com.redhat.thermostat.gateway.service.commands.channel.model.ClientRequest;

public class WebSocketCommunicationBuilder {

    private Session clientSession;
    private Session agentSession;
    private ClientRequest clientRequest;
    private String action;
    private String jvmId;
    private String systemId;

    public WebSocketCommunicationBuilder setRequest(ClientRequest request) {
        this.clientRequest = request;
        return this;
    }

    public WebSocketCommunicationBuilder setClientSession(Session session) {
        this.clientSession = session;
        return this;
    }

    public WebSocketCommunicationBuilder setAgentSession(Session session) {
        this.agentSession = session;
        return this;
    }

    public WebSocketCommunicationBuilder setAction(String action) {
        this.action = action;
        return this;
    }

    public WebSocketCommunicationBuilder setJvmId(String jvmId) {
        this.jvmId = jvmId;
        return this;
    }

    public WebSocketCommunicationBuilder setSystemId(String systemId) {
        this.systemId = systemId;
        return this;
    }

    public ClientAgentCommunication build() {
        Objects.requireNonNull(clientSession);
        Objects.requireNonNull(agentSession);
        Objects.requireNonNull(clientRequest);
        Objects.requireNonNull(action);
        Objects.requireNonNull(jvmId);
        Objects.requireNonNull(systemId);
        AgentRequest agentRequest = new AgentRequest(clientRequest.getSequenceId(), action, systemId, jvmId, clientRequest.getParams());
        AgentGatewayCommunication agent = new AgentGatewayCommunication(clientSession, agentSession, agentRequest);
       return new ClientAgentCommunication(agent, clientRequest);
    }
}
