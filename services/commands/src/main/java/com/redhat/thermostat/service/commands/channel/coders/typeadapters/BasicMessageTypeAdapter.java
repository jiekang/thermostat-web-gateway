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

package com.redhat.thermostat.service.commands.channel.coders.typeadapters;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.redhat.thermostat.service.commands.channel.model.AgentRequest;
import com.redhat.thermostat.service.commands.channel.model.ClientRequest;
import com.redhat.thermostat.service.commands.channel.model.Message;
import com.redhat.thermostat.service.commands.channel.model.Message.MessageType;
import com.redhat.thermostat.service.commands.channel.model.WebSocketResponse;
import com.redhat.thermostat.service.commands.channel.model.WebSocketResponse.ResponseType;

abstract class BasicMessageTypeAdapter<T extends Message> extends TypeAdapter<T> {

    private static final String RESPONSE_TYPE_KEY = "respType";
    protected final Gson gson;

    BasicMessageTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    protected JsonObject getEnvelopeWithType(Message message) throws IllegalStateException {
        JsonObject object = new JsonObject();
        MessageType msgType = message.getMessageType();
        if (msgType == null) {
            throw new IllegalStateException("Message type must not be null!");
        }
        JsonElement typeElem = gson.toJsonTree(message.getMessageType().intValue());
        object.add(Message.TYPE_KEY, typeElem);
        return object;
    }

    protected JsonObject getEnvelopeWithTypeAndSequence(Message message, long sequence) throws IllegalStateException {
        JsonObject object = getEnvelopeWithType(message);
        JsonElement sequenceElem = gson.toJsonTree(sequence);
        object.add(Message.SEQUENCE_KEY, sequenceElem);
        return object;
    }

    protected Message parseJsonMessage(JsonReader in) throws IOException {
        RawMessage raw = getRawMessageFromReader(in);
        switch (raw.getMessageType()) {
        case AGENT_REQUEST: {
            requireSequenceNonNull(raw.getSequenceElement(),
                                   "Agent request without a sequence!");
            long sequence = raw.getSequenceElement().getAsLong();
            SortedMap<String, String> params = decodePayloadAsParamMap(raw.getPayloadElement());
            return new AgentRequest(sequence, params);
        }
        case CLIENT_REQUEST: {
            SortedMap<String, String> params = decodePayloadAsParamMap(raw.getPayloadElement());
            return new ClientRequest(params);
        }
        case RESPONSE: {
            requireSequenceNonNull(raw.getSequenceElement(),
                                   "Response message without a sequence!");
            long sequence = raw.getSequenceElement().getAsLong();
            ResponseType respType = decodePayloadAsResponseType(raw.getPayloadElement());
            return new WebSocketResponse(sequence, respType);
        }
        default:
            throw new IllegalStateException("Unknown message type: " + raw.getMessageType());
        }
    }

    private SortedMap<String, String> decodePayloadAsParamMap(JsonObject payloadElem) {
        SortedMap<String, String> paramMap = new TreeMap<>();
        if (payloadElem == null) {
            return paramMap; // no params
        }
        for (Entry<String,JsonElement> entry: payloadElem.entrySet()) {
            JsonElement value = entry.getValue();
            String strValue = null;
            if (value != null && !value.isJsonNull()) {
                strValue = value.getAsString();
            }
            paramMap.put(entry.getKey(), strValue);
        }
        return paramMap;
    }

    private ResponseType decodePayloadAsResponseType(JsonObject payloadElem) throws IllegalStateException {
        JsonElement type = payloadElem.get(RESPONSE_TYPE_KEY);
        if (type == null) {
            throw new IllegalStateException("Invalid response payload");
        }
        String typeStr = type.getAsString();
        return ResponseType.valueOf(typeStr);
    }

    private void requireSequenceNonNull(JsonElement sequenceElem, String msg) throws IllegalStateException {
        if (sequenceElem == null) {
            throw new IllegalStateException(msg);
        }
    }

    private RawMessage getRawMessageFromReader(JsonReader in) throws IOException {
        JsonParser parser = new JsonParser();
        JsonObject object = (JsonObject)parser.parse(in);
        return new RawMessage(object);
    }

    static class RawMessage {

        private final JsonObject root;
        private final JsonElement typeElem;
        private final JsonElement sequenceElem;
        private final JsonObject payloadElem;

        private RawMessage(JsonObject object) {
            Objects.requireNonNull(object);
            root = object;
            typeElem = object.get(Message.TYPE_KEY);
            sequenceElem = object.get(Message.SEQUENCE_KEY);
            payloadElem = (JsonObject)object.get(Message.PAYLOAD_KEY);
        }

        MessageType getMessageType() {
            Objects.requireNonNull(typeElem);
            int type = typeElem.getAsInt();
            return MessageType.fromInt(type);
        }

        JsonElement getSequenceElement() {
            return sequenceElem;
        }

        JsonObject getPayloadElement() {
            return payloadElem;
        }

        JsonObject getRootObject() {
            return root;
        }
    }
}
