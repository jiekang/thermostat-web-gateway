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

package com.redhat.thermostat.service.commands.channel;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketResponse implements Sequential {

    /**
     * Sequence id to use when it's not possible to determine it by other means.
     */
    public static final long UNKNOWN_SEQUENCE = -1L;

    private static final String RID_PARAM = "__rid__";
    private static final Pattern REGEX_PATTERN = Pattern.compile(RID_PARAM + "=([^\\n]+)\\n\\n(.*)");
    private final WebSocketResponse.ResponseType type;
    private final long sequence;

    public WebSocketResponse(long sequence, WebSocketResponse.ResponseType type) {
        this.type = type;
        this.sequence = sequence;
    }

    public static WebSocketResponse fromMessage(String msg) {
        Matcher m = REGEX_PATTERN.matcher(msg);
        if (!m.matches()) {
            throw new IllegalArgumentException("Not a response in properly serilized format. Got: " + msg);
        }
        String rid = m.group(1);
        long sequence;
        try {
            sequence = Long.parseLong(rid);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a response in properly serilized format. Got: " + msg, e);
        }
        String responsePart = m.group(2);
        WebSocketResponse.ResponseType t = ResponseType.valueOf(responsePart);
        return new WebSocketResponse(sequence, t);
    }

    public String asStringMesssage() {
        return RID_PARAM + "=" + sequence + "\n\n" +
               type.name();
    }

    public WebSocketResponse.ResponseType getType() {
        return type;
    }

    @Override
    public long getSequenceId() {
        return sequence;
    }

    @Override
    public String toString() {
        return WebSocketResponse.class.getSimpleName() + "[" + sequence + ", " + type + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof WebSocketResponse)) {
            return false;
        }
        WebSocketResponse o = (WebSocketResponse)other;
        return Objects.equals(sequence, o.sequence) &&
                Objects.equals(type, o.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sequence) + Objects.hashCode(type);
    }

    public enum ResponseType {
        OK,
        ERROR,
        AUTH_FAIL
    }

}
