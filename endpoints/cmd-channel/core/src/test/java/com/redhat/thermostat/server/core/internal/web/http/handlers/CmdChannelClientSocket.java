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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * Handles the client initiated actions. E.g. triggering a cmd channel request
 * to some agent.
 *
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class CmdChannelClientSocket {
    private final CountDownLatch closeLatch;
    private final String agentId;
    private Response resp;

    public CmdChannelClientSocket(String agentId) {
        this.closeLatch = new CountDownLatch(1);
        this.agentId = agentId;
    }

    public boolean awaitClose(int duration, TimeUnit unit)
            throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.closeLatch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
        try {
            Request req = new Request(agentId);
            req.setParam("vmId", "someval");
            Future<Void> fut = session.getRemote()
                    .sendStringByFuture(req.asStringMessage());
            fut.get(2, TimeUnit.SECONDS); // wait for send to complete.
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        if (cause instanceof UpgradeException) {
            UpgradeException exptn = (UpgradeException) cause;
            int statusCode = exptn.getResponseStatusCode();
            System.err.println("Expected " + HttpServletResponse.SC_OK
                    + " but got " + statusCode);
            System.err.println("Request URI was: " + exptn.getRequestURI());
            cause.printStackTrace();
        }
        Throwable realCause = cause.getCause();
        while (realCause != null) {
            realCause.printStackTrace();
            realCause = realCause.getCause();
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        System.out.printf("Got msg: %s%n", msg);
        this.resp = Response.fromMessage(msg);
        System.out.println("Bye now.");
        session.close();
    }

    public Response getResponse() {
        return resp;
    }
}