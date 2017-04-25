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
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

class AgentSocketsRegistry {

    private static final double PERCENT_85 = 0.85;
    private static final Map<String, AgentSessionHolder> agentSockets = new ConcurrentHashMap<>();
    private static final String TIMER_NAME = "com.redhat.thermostat.service.commands.ReceiverPingTimer";
    private static final long PING_BOUND_BELOW_SECONDS = 3;
    private static AgentSocketsRegistry INSTANCE;
    private final long pingInterval;
    private final TimerTask pingTask;
    private final Timer pingTimer;
    private boolean isTimerStarted = false;

    AgentSocketsRegistry(long socketTimeOut) {
       this(new Timer(TIMER_NAME), new PingTimerTask(), socketTimeOut);
    }

    AgentSocketsRegistry(Timer timer, TimerTask pingTask, long socketTimeOut) {
        this.pingTimer = timer;
        this.pingTask = pingTask;
        this.pingInterval = calculatePeriod(socketTimeOut);
    }

    public static synchronized AgentSocketsRegistry getInstance(long socketTimeout) {
        if (INSTANCE == null) {
            INSTANCE = new AgentSocketsRegistry(socketTimeout);
        }
        return INSTANCE;
    }

    public void addSocket(String id, Session session) {
        AgentPingSequence sequence = new AgentPingSequence(id);
        AgentSessionHolder holder = new AgentSessionHolder(session, sequence);
        agentSockets.put(id, holder);
        sendPing(holder);
        if (agentSockets.size() == 1) {
            startPingTimer();
        }
    }

    public Session getSession(String id) {
        AgentSessionHolder holder = agentSockets.get(id);
        if (holder == null) {
            return null;
        }
        return holder.session;
    }

    public void removeSocket(String id) {
        agentSockets.remove(id);
    }

    private static void sendPing(AgentSessionHolder holder) {
        try {
            Session session = holder.session;
            String pingPayload = holder.sequence.getNextPingPayload();
            synchronized (session) {
                if (session.isOpen()) {
                    ByteBuffer payload = ByteBuffer.wrap(pingPayload.getBytes("UTF-8"));
                    if (Debug.isOn()) {
                        System.err.println("Server: sending ping msg <<" + pingPayload + ">>");
                    }
                    session.getBasicRemote().sendPing(payload);
                }
            }
        } catch (IOException e) {
            // ignore
        }
    }

    static long calculatePeriod(long sockTimeOut) {
        // Heuristic: Use 85% of the socket timeout for the ping timer period
        long periodCandidate = (long)(sockTimeOut * PERCENT_85);
        if (periodCandidate < TimeUnit.SECONDS.toMillis(PING_BOUND_BELOW_SECONDS)) {
            long socketTimeOutSecs = TimeUnit.MILLISECONDS.toSeconds(sockTimeOut);
            throw new IllegalStateException("Agent socket timeout (" + socketTimeOutSecs + "s) too short."+
                                            " Is the socket timeout configured correctly?");
        }
        return periodCandidate;
    }

    private synchronized void startPingTimer() {
        if (!isTimerStarted) {
            pingTimer.scheduleAtFixedRate(pingTask, pingInterval, pingInterval);
            isTimerStarted = true;
        }
    }

    private static class PingTimerTask extends TimerTask {

        @Override
        public void run() {
            for (AgentSessionHolder holder: agentSockets.values()) {
                sendPing(holder);
            }
        }

    }

    private static class AgentSessionHolder {
        private final Session session;
        private final AgentPingSequence sequence;

        AgentSessionHolder(Session session, AgentPingSequence sequence) {
            this.session = session;
            this.sequence = sequence;
        }
    }
}
