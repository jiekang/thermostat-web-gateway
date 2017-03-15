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
import javax.websocket.Session;

import com.redhat.thermostat.server.core.internal.web.http.handlers.Response.ResponseType;
import com.redhat.thermostat.server.core.internal.web.security.authentication.basic.BasicWebUser;

public class CommandChannelEndpointHandler {

    private static final String CLIENT_GRANT_ACTION_PREFIX = "thermostat-commands-grant-";
    private static final String CLIENT_GRANT_JVM_PREFIX = "thermostat-commands-grant-jvm-";
    private static final String AGENT_PROVIDER_PREFIX = "thermostat-commands-provider-";
    private static final String PATH_PARAM_AGENT_ID = "agentId";
    private static final String PATH_PARAM_ACTION = "action";
    private static final String PATH_PARAM_JVM = "jvmId";

    static final String HANDSHAKE_REQUEST_KEY = "handshake-request";
    static final String TYPE_PARAM_NAME = "type";

    private CommandChannelWebSocket socket;

    protected void onConnect(WebSocketType type, Session session)
            throws IOException {
        if (session.getUserPrincipal() == null) {
            session.getBasicRemote().sendText(ResponseType.AUTH_FAIL.name());
            session.close(
                    new CloseReason(CloseCodes.VIOLATED_POLICY,
                    "Not authenticated!"));
            return;
        }
        String user = session.getUserPrincipal().getName();
        System.out.println("User principal: " + session.getUserPrincipal());
        String agentId = session.getPathParameters().get(PATH_PARAM_AGENT_ID);
        System.out.println("Checking roles for "
                + session.getUserPrincipal().getName() + "... ");
        if (!checkRoles(type, session, agentId)) {
            session.getBasicRemote().sendText(ResponseType.AUTH_FAIL.name());
            session.close(new CloseReason(CloseCodes.VIOLATED_POLICY,
                    "Not authorized!"));
            return;
        }
        socket = new CommandChannelWebSocketImpl(user, type, agentId, session);
        socket.onConnect();
    }

    private boolean checkRoles(WebSocketType type, Session session,
            String agentId) throws IOException {
        // FIXME: relies on BasicWebUser - i.e. specific auth scheme.
        BasicWebUser user = (BasicWebUser) session.getUserPrincipal();
        switch (type) {
        case AGENT:
            String roleToCheck = AGENT_PROVIDER_PREFIX + agentId;
            if (!user.isUserInRole(roleToCheck)) {
                return false;
            }
            return true;
        case CLIENT:
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
        default:
            throw new AssertionError("Must not happen. Code changed?");
        }
    }

    protected void onMessage(String msg) {
        socket.onSocketMessage(msg);
    }

    protected void onError(Throwable cause) {
        socket.onError(cause);
    }

    protected void onClose(int code, String reason) {
        socket.onClose(code, reason);
    }
}
