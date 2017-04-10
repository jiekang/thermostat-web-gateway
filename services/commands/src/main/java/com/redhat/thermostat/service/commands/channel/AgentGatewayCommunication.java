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

package com.redhat.thermostat.service.commands.channel;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.websocket.RemoteEndpoint.Basic;

import com.redhat.thermostat.service.commands.channel.WebSocketResponse.ResponseType;

import javax.websocket.Session;

/**
 * Gateway => Agent communication part of the Command Channel
 *
 */
class AgentGatewayCommunication implements WebSocketCommunication, AgentResponseListener {

    private final Session clientSession;
    private final Session agentSession;
    private final AgentRequest agentRequest;
    private final CountDownLatch agentResponseLatch;
    private WebSocketResponse agentResponse;

    AgentGatewayCommunication(Session clientSession, Session agentSession, AgentRequest request) {
        this.clientSession = clientSession;
        this.agentSession = agentSession;
        this.agentRequest = request;
        this.agentResponseLatch = new CountDownLatch(1);
    }

    @Override
    public WebSocketResponse perform() {
        try {
            synchronized (agentSession) {
                Basic remote = agentSession.getBasicRemote();
                remote.sendText(agentRequest.asStringMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return error();
        }
        try {
            boolean isExpired = !agentResponseLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            // Agent response timed out.
            if (isExpired) {
                return error();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return error();
        }
        return sendResponseToClient();
    }

    private WebSocketResponse error() {
        return new WebSocketResponse(agentRequest.getSequenceId(), ResponseType.ERROR);
    }

    private WebSocketResponse sendResponseToClient() {
        try {
            synchronized (clientSession) {
                Basic remote = clientSession.getBasicRemote();
                remote.sendText(agentResponse.asStringMesssage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return error();
        }
        return agentResponse;
    }

    @Override
    public void responseReceived(WebSocketResponse resp) {
        this.agentResponse = resp;
        agentResponseLatch.countDown();
    }

}
