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

package com.redhat.thermostat.gateway.service.commands.channel.coders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import com.redhat.thermostat.gateway.service.commands.channel.model.AgentRequest;
import com.redhat.thermostat.gateway.service.commands.channel.model.ClientRequest;
import com.redhat.thermostat.gateway.service.commands.channel.model.Message;
import com.redhat.thermostat.gateway.service.commands.channel.model.WebSocketResponse;
import com.redhat.thermostat.gateway.service.commands.channel.model.WebSocketResponse.ResponseType;

/**
 * Tests message decoding and encoding full-circle.
 *
 */
public class EncodeDecodeTest {

    @Test
    public void canEncodeDecodeAgentRequest() throws Exception {
        long sequence = 392;
        SortedMap<String, String> params = new TreeMap<>();
        params.put("foo", "bar");
        AgentRequest request = new AgentRequest(sequence, params);
        String encoded = new AgentRequestEncoder().encode(request);
        Message result = new MessageDecoder().decode(encoded);
        assertNotNull(result);
        assertTrue(result instanceof AgentRequest);
        AgentRequest actual = (AgentRequest)result;
        assertEquals(sequence, actual.getSequenceId());
        assertEquals(params, actual.getParams());
    }

    @Test
    public void canEncodeDecodeClientRequest() throws Exception {
        SortedMap<String, String> params = new TreeMap<>();
        params.put("test", "test ,| R \"val");

        ClientRequest request = new ClientRequest(params);
        String encoded = new ClientRequestEncoder().encode(request);
        Message result = new MessageDecoder().decode(encoded);
        assertNotNull(result);
        assertTrue(result instanceof ClientRequest);
        ClientRequest actual = (ClientRequest)result;
        assertEquals(params, actual.getParams());
        assertEquals(Message.UNKNOWN_SEQUENCE, actual.getSequenceId());
    }

    @Test
    public void canEncodeDecodeResponse() throws Exception {
        ResponseType type = ResponseType.AUTH_FAIL;
        long sequence = 9932;
        WebSocketResponse response = new WebSocketResponse(sequence, type);
        String encoded = new WebSocketResponseEncoder().encode(response);
        Message result = new MessageDecoder().decode(encoded);
        assertNotNull(result);
        assertTrue(result instanceof WebSocketResponse);
        WebSocketResponse actual = (WebSocketResponse)result;
        assertEquals(sequence, actual.getSequenceId());
        assertEquals(type, actual.getResponseType());
    }
}
