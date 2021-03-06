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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;

import com.google.gson.Gson;
import com.redhat.thermostat.gateway.service.commands.channel.model.Message;

/**
 * Handles the agent connections from the Web endpoint.
 *
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class CmdChannelAgentSocket {

    private final Gson gson;
    private final CountDownLatch closeLatch;
    private final CountDownLatch connectLatch;
    private final CountDownLatch pongReceived;
    private final CountDownLatch pingReceived;
    private final OnMessageCallBack onMessage;
    private Session session;
    private String pongMsg;
    private String pingMsg;

    public CmdChannelAgentSocket(OnMessageCallBack onMessage, CountDownLatch connect, Gson gson) {
        this(onMessage, connect, new CountDownLatch(1), new CountDownLatch(1), gson);
    }

    public CmdChannelAgentSocket(OnMessageCallBack onMessage, Gson gson) {
        this(onMessage, new CountDownLatch(1), gson);
    }

    public CmdChannelAgentSocket(CountDownLatch connect, CountDownLatch pingPongSignal, boolean isPing) {
        this(new NoOpMsgCallback(), connect, isPing ? new CountDownLatch(1) : pingPongSignal, isPing ? pingPongSignal : new CountDownLatch(1), null);
    }

    private CmdChannelAgentSocket(OnMessageCallBack onMessage, CountDownLatch connect, CountDownLatch pongReceived, CountDownLatch pingReceived, Gson gson) {
        this.closeLatch = new CountDownLatch(1);
        this.onMessage = onMessage;
        this.connectLatch = connect;
        this.gson = gson;
        this.pongReceived = pongReceived;
        this.pingReceived = pingReceived;
    }


    public void awaitClose() throws InterruptedException {
        this.closeLatch.await();
    }

    @OnWebSocketFrame
    public void onFrame(Frame frame) {
        switch (frame.getType()) {
        case PONG:
            handlePong(frame.getPayload());
            break;
        case PING:
            ByteBuffer payload = null;
            if (frame.hasPayload()) {
                payload = frame.getPayload();
            }
            handlePing(payload);
            break;
        default:
            // nothing to do
        }
    }

    private void handlePing(ByteBuffer payload) {
        RemoteEndpoint endPoint = session.getRemote();
        try {
            // Note: sendPong() will change the position of the payload
            //       so the string needs to get retrieved before sendPong()
            //       is called.
            pingMsg = getStringFromPayload(payload);
            endPoint.sendPong(payload);
            pingReceived.countDown();
        } catch (IOException e) {
            System.err.println("Failed to send pong response!");
            e.printStackTrace();
        }
    }

    private void handlePong(ByteBuffer payload) {
        pongMsg = getStringFromPayload(payload);
        pongReceived.countDown();
    }

    private String getStringFromPayload(ByteBuffer payload) {
        int limit = payload.limit();
        int position = payload.position();
        int length = limit - position;
        byte[] buf = new byte[length];
        for (int i = 0; position < limit; position++, i++) {
            buf[i] = payload.get(position);
        }
        return new String(buf);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.closeLatch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        this.connectLatch.countDown();
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
    public void onMessage(final Session session, final String msg) {
        final Message message = gson.fromJson(msg, Message.class);
        onMessage.run(session, message);
    }

    public void closeSession() {
        if (session != null) {
            this.session.close();
        }
    }

    public void sendPingToServer(String msgPayload) throws IOException {
        if (this.session == null) {
            throw new NullPointerException("Session null. Agent not connected?");
        }
        RemoteEndpoint endpoint = session.getRemote();
        ByteBuffer pingPayload = ByteBuffer.wrap(msgPayload.getBytes());
        endpoint.sendPing(pingPayload);
        System.err.println("Client: Ping msg sent <<" + msgPayload + ">>");
    }

    public String getPongMsg() {
        return pongMsg;
    }

    public String getPingMsg() {
        return pingMsg;
    }

    public interface OnMessageCallBack {
        public void run(Session session, Message msg);
    }
}
