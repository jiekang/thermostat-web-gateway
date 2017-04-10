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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.redhat.thermostat.service.commands.channel.WebSocketResponse.ResponseType;

/**
 * Full abstracted channel between client and agent.
 *
 */
public class ClientAgentCommunication implements WebSocketCommunication, AgentResponseListener {

    private final AgentGatewayCommunication agentGateway;
    private final ClientRequest clientRequest;
    private final CountDownLatch agentResponseLatch;

    ClientAgentCommunication(AgentGatewayCommunication agentGateway, ClientRequest request) {
        this.agentGateway = agentGateway;
        this.clientRequest = request;
        this.agentResponseLatch = new CountDownLatch(1);
    }

    @Override
    public WebSocketResponse perform() {
        PerformThread thread = new PerformThread(agentGateway);
        thread.start();
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
        return thread.getResponse();
    }

    private WebSocketResponse error() {
        return new WebSocketResponse(clientRequest.getSequenceId(), ResponseType.ERROR);
    }

    @Override
    public void responseReceived(WebSocketResponse resp) {
        agentGateway.responseReceived(resp);
        agentResponseLatch.countDown();
    }

    private static class PerformThread extends Thread {

        private final CountDownLatch latch = new CountDownLatch(1);
        private final AgentGatewayCommunication delegate;
        private WebSocketResponse resp;

        private PerformThread(AgentGatewayCommunication delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            resp = delegate.perform();
            latch.countDown();
        }

        public WebSocketResponse getResponse() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return resp;
        }
    }

}
