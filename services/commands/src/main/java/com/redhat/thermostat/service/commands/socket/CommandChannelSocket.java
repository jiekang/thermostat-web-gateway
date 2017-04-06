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
import java.security.Principal;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import com.redhat.thermostat.service.commands.channel.WebSocketResponse;
import com.redhat.thermostat.service.commands.channel.WebSocketResponse.ResponseType;

import javax.websocket.Session;

abstract class CommandChannelSocket implements CommandChannelWebSocket {

    protected final String agentId;
    protected final Session session;

    protected CommandChannelSocket(String id, Session session) {
        this.agentId = id;
        this.session = session;
    }

    @Override
    public void onConnect() throws IOException {
        System.err.println(toString() + " connected.");
        if (session.getUserPrincipal() == null) {
            sendAuthFail(getSequence());
            synchronized (session) {
                session.close(new CloseReason(CloseCodes.VIOLATED_POLICY,
                                              "Not authenticated!"));
            }
            return;
        }
        System.out.println("User principal: " + session.getUserPrincipal());
        System.out.println("Checking roles for " + session.getUserPrincipal().getName() + "... ");
        if (!checkRoles()) {
            sendAuthFail(getSequence());
            synchronized (session) {
                session.close(new CloseReason(CloseCodes.VIOLATED_POLICY,
                                              "Not authorized!"));
            }
            return;
        }
    }

    @Override
    public void onClose(int closeCode, String reason) {
        System.err.println(toString() + " closed session.");
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
        Principal p = session.getUserPrincipal();
        String user = p == null ? null : p.getName();
        return getClass().getSimpleName() + "[" + user + "," + agentId + "]";
    }

    protected void sendAuthFail(String sequenceIdStr) throws IOException {
        long sequenceId = WebSocketResponse.UNKNOWN_SEQUENCE;
        if (sequenceIdStr != null) {
            try {
                sequenceId = Long.parseLong(sequenceIdStr);
            } catch (NumberFormatException e) {
                // fall-through
            }
        }
        WebSocketResponse resp = new WebSocketResponse(sequenceId, ResponseType.AUTH_FAIL);
        synchronized (session) {
            session.getBasicRemote().sendText(resp.asStringMesssage());
        }
    }

    /**
     * Performs the actual socket-level communication
     * @param msg The received message.
     * @throws IOException
     */
    protected abstract void performCommunication(String msg) throws IOException;

    /**
     * Performs role permission checks relevant for this socket
     *
     * @return {@code true} if and only if role checks passed.
     */
    protected abstract boolean checkRoles();

    /**
     * The (optional) sequence number passed in via requests.
     *
     * @return The sequence number sent from the client or {@code null}
     */
    protected abstract String getSequence();
}
