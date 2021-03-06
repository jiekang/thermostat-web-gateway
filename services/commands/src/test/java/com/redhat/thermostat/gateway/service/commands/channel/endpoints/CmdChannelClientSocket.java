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

package com.redhat.thermostat.gateway.service.commands.channel.endpoints;

import java.util.concurrent.CountDownLatch;

import com.redhat.thermostat.gateway.service.commands.channel.coders.typeadapters.MessageTypeAdapterFactory;
import com.redhat.thermostat.gateway.service.commands.channel.model.ClientRequest;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.thermostat.gateway.service.commands.channel.model.Message;
import com.redhat.thermostat.gateway.service.commands.channel.model.Message.MessageType;
import com.redhat.thermostat.gateway.service.commands.channel.model.WebSocketResponse;

/**
 * Handles the client initiated actions. E.g. triggering a cmd channel request
 * to some agent.
 *
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class CmdChannelClientSocket {

    private final Gson gson;
    private final CountDownLatch closeLatch;
    private final CountDownLatch messageSentLatch;
    private final ClientRequest request;
    private WebSocketResponse resp;

    public CmdChannelClientSocket(ClientRequest request, CountDownLatch messageSentLatch) {
        this.closeLatch = new CountDownLatch(1);
        this.request = request;
        this.messageSentLatch = messageSentLatch;
        this.gson = new GsonBuilder()
                    .registerTypeAdapterFactory(new MessageTypeAdapterFactory())
                    .serializeNulls()
                    .disableHtmlEscaping()
                    .create();
    }

    public CmdChannelClientSocket(ClientRequest request) {
        this(request, new CountDownLatch(1));
    }

    public void awaitClose() throws InterruptedException {
        this.closeLatch.await();
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.closeLatch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        try {
            String serializedMsg = gson.toJson(request);
            session.getRemote().sendString(serializedMsg);
            session.getRemote().flush();
            messageSentLatch.countDown();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        Throwable realCause = cause.getCause();
        while (realCause != null) {
            realCause.printStackTrace();
            realCause = realCause.getCause();
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String msg) {
        Message message = gson.fromJson(msg, Message.class);
        if (message.getMessageType() != MessageType.RESPONSE) {
            throw new AssertionError("Illegal type. Got " + message.getMessageType());
        }
        WebSocketResponse r = (WebSocketResponse)message;
        if (r.getSequenceId() == request.getSequenceId()) {
            this.resp = r;
        }
        session.close();
    }

    public WebSocketResponse getResponse() {
        return resp;
    }
}