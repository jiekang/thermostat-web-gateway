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

package com.redhat.thermostat.gateway.service.commands.socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class AgentSocketsRegistryTest {

    private static final long SOCKET_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private AgentSocketsRegistry reg;
    private Timer timer;
    private TimerTask pingTask;

    @Before
    public void setup() {
        timer = mock(Timer.class);
        pingTask = mock(TimerTask.class);
        reg = new AgentSocketsRegistry(timer, pingTask, SOCKET_TIMEOUT);
    }

    @Test(expected = IllegalStateException.class)
    public void illegalSocketTimeout() {
        new AgentSocketsRegistry(TimeUnit.SECONDS.toMillis(3));
    }

    @Test
    public void addingSocketToRegistrySendsPing() throws IOException {
        Session session = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);
        when(session.isOpen()).thenReturn(true);

        reg.addSocket("fooAgent", session);

        ArgumentCaptor<ByteBuffer> payloadCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(basic).sendPing(payloadCaptor.capture());
        verify(session).isOpen();
        ByteBuffer payload = payloadCaptor.getValue();
        String actualPayload = extractPayload(payload);
        assertEquals("1|fooAgent", actualPayload);
    }

    private String extractPayload(ByteBuffer payload) throws UnsupportedEncodingException {
        int limit = payload.limit();
        int position = payload.position();
        int length = limit - position;
        byte[] buf = new byte[length];
        for (int i = 0; position < limit; position++, i++) {
            buf[i] = payload.get(position);
        }
        return new String(buf, "UTF-8");
    }

    @Test
    public void getSessionNonExistentReturnsNull() {
        // Must not throw exception
        assertNull(reg.getSession("do-not-exist"));
    }

    @Test
    public void addingSocketToRegistryStartsTimer() {
        reg = new AgentSocketsRegistry(timer, pingTask, SOCKET_TIMEOUT);
        long expectedPeriod = (long)(SOCKET_TIMEOUT * 0.85);
        Session session = mock(Session.class);
        Basic basic = mock(Basic.class);
        when(session.getBasicRemote()).thenReturn(basic);

        reg.addSocket("fooAgent", session);
        verify(timer).scheduleAtFixedRate(eq(pingTask), eq(expectedPeriod), eq(expectedPeriod));
    }

    @Test
    public void testIsSingleton() {
        AgentSocketsRegistry reg = AgentSocketsRegistry.getInstance(SOCKET_TIMEOUT);
        AgentSocketsRegistry other = AgentSocketsRegistry.getInstance(SOCKET_TIMEOUT);
        assertSame(reg, other);
        // differnent timeout doesn't matter for subsequent calls.
        AgentSocketsRegistry third = AgentSocketsRegistry.getInstance(SOCKET_TIMEOUT + 3);
        assertSame(reg, third);
    }

    @Test
    public void calculatesTimerPeriodCorrectly() {
        long actual = AgentSocketsRegistry.calculatePeriod(100 * 1000);
        long expectedPeriod = 85 * 1000; // 85% of socket timeout
        assertEquals(expectedPeriod, actual);
        actual = AgentSocketsRegistry.calculatePeriod(TimeUnit.MINUTES.toMillis(2));
        long expected = (long)(TimeUnit.MINUTES.toMillis(2) * 0.85);
        assertEquals(expected, actual);
    }

}
