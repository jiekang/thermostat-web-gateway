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

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

// Client endpoints; Initiators
@ServerEndpoint(value = "/commands/v1/actions/{action}/systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
public class CommandChannelClientEndpointHandler
        extends CommandChannelEndpointHandler {

    @OnOpen
    public void onConnect(Session session,
                          @PathParam("action") final String action,
                          @PathParam("systemId") final String systemId,
                          @PathParam("agentId") final String agentId,
                          @PathParam("jvmId") final String jvmId) throws IOException {
        super.onConnect(WebSocketType.CLIENT, session);
    }

    @OnMessage
    public void onTextMessage(String msg) {
        super.onMessage(msg);
    }

    @OnClose
    public void onClose(CloseReason reason) {
        super.onClose(reason.getCloseCode().getCode(),
                reason.getReasonPhrase());
    }

    @OnError
    public void onErrorThrown(Session session, Throwable cause) {
        try {
            // Let the client know something failed
            session.getBasicRemote()
                    .sendText(Response.ResponseType.ERROR.name());
            session.close(new CloseReason(CloseCodes.UNEXPECTED_CONDITION,
                    "Server error"));
        } catch (IOException e) {
            // Not much we can do
            e.printStackTrace();
        }
        super.onError(cause);
    }
}
