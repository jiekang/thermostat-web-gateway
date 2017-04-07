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

package com.redhat.thermostat.service.commands.channel.coders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.websocket.DecodeException;

import org.junit.Test;

import com.redhat.thermostat.service.commands.channel.model.AgentRequest;
import com.redhat.thermostat.service.commands.channel.model.ClientRequest;
import com.redhat.thermostat.service.commands.channel.model.Message;
import com.redhat.thermostat.service.commands.channel.model.WebSocketResponse;
import com.redhat.thermostat.service.commands.channel.model.WebSocketResponse.ResponseType;

public class MessageDecoderTest {

    @Test
    public void canDecodeAgentRequestMessage() throws DecodeException {
        String json = "{" +
                      "   \"type\": 1," +
                      "   \"sequence\": 2312," +
                      "   \"payload\": {" +
                      "       \"param1\": \"value1\"," +
                      "       \"param2\": \"value2\"" +
                      "   }" +
                      "}";
        MessageDecoder decoder = new MessageDecoder();
        Message actual = decoder.decode(json);
        assertNotNull(actual);
        assertTrue(actual instanceof AgentRequest);
        AgentRequest request = (AgentRequest) actual;
        assertEquals(2312, request.getSequenceId());
        assertEquals("value1", request.getParam("param1"));
        assertEquals("value2", request.getParam("param2"));
    }

    @Test
    public void canDecodeAgentRequestMessageWithFunnyChars() throws DecodeException {
        String json = "{" +
                      "   \"type\": 1," +
                      "   \"sequence\": 2312," +
                      "   \"payload\": {" +
                      "       \"param1\": \"value1 } {, RULE foo\nparam()\"," +
                      "       \"param2\": \"value2\"" +
                      "   }" +
                      "}";
        MessageDecoder decoder = new MessageDecoder();
        Message actual = decoder.decode(json);
        assertNotNull(actual);
        assertTrue(actual instanceof AgentRequest);
        AgentRequest request = (AgentRequest) actual;
        assertEquals(2312, request.getSequenceId());
        assertEquals("value1 } {, RULE foo\nparam()", request.getParam("param1"));
        assertEquals("value2", request.getParam("param2"));
    }

    @Test
    public void canDecodeAgentRequestMessageNullParam() throws DecodeException {
        String json = "{" +
                      "   \"type\": 1," +
                      "   \"sequence\": 2332," +
                      "   \"payload\": {" +
                      "       \"param1\": null," +
                      "       \"param2\": \"value2\"" +
                      "   }" +
                      "}";
        MessageDecoder decoder = new MessageDecoder();
        Message actual = decoder.decode(json);
        assertNotNull(actual);
        assertTrue(actual instanceof AgentRequest);
        AgentRequest request = (AgentRequest) actual;
        assertEquals(2332, request.getSequenceId());
        assertEquals(null, request.getParam("param1"));
        assertEquals("value2", request.getParam("param2"));
    }

    @Test
    public void canDecodeClientRequestMessage() throws DecodeException {
        String json = "{" +
                      "   \"type\": 2," +
                      "   \"payload\": {" +
                      "       \"param1\": \"value1\"," +
                      "       \"param2\": \"value2\"" +
                      "   }" +
                      "}";
        MessageDecoder decoder = new MessageDecoder();
        Message actual = decoder.decode(json);
        assertNotNull(actual);
        assertTrue(actual instanceof ClientRequest);
        ClientRequest request = (ClientRequest) actual;
        assertEquals("value1", request.getParam("param1"));
        assertEquals("value2", request.getParam("param2"));
    }

    @Test
    public void canDecodeResponseMessage() throws DecodeException {
        String json = "{" +
                      "   \"type\": 100," +
                      "   \"sequence\": 323," +
                      "   \"payload\": {" +
                      "       \"respType\": \"OK\"" +
                      "   }" +
                      "}";
        MessageDecoder decoder = new MessageDecoder();
        Message actual = decoder.decode(json);
        assertNotNull(actual);
        assertTrue(actual instanceof WebSocketResponse);
        WebSocketResponse response = (WebSocketResponse) actual;
        assertEquals(ResponseType.OK, response.getResponseType());
    }

    @Test(expected = DecodeException.class)
    public void failsDecodeTypeMissing() throws DecodeException {
        String json = "{" +
                "   \"payload\": {" +
                "       \"respType\": \"OK\"" +
                "   }" +
                "}";
        MessageDecoder decoder = new MessageDecoder();
        decoder.decode(json);
    }

    @Test(expected = DecodeException.class)
    public void failsDecodeResponseSequenceMissing() throws DecodeException {
        String json = "{" +
                "   \"type\": 100," +
                "   \"payload\": {" +
                "       \"respType\": \"OK\"" +
                "   }" +
                "}";
        MessageDecoder decoder = new MessageDecoder();
        decoder.decode(json);
    }

    @Test(expected = DecodeException.class)
    public void failsDecodeAgentRequestSequenceMissing() throws DecodeException {
        String json = "{" +
                "   \"type\": 1," +
                "   \"payload\": {" +
                "       \"param1\": null," +
                "       \"param2\": \"value2\"" +
                "   }" +
                "}";
        MessageDecoder decoder = new MessageDecoder();
        decoder.decode(json);
    }
}
