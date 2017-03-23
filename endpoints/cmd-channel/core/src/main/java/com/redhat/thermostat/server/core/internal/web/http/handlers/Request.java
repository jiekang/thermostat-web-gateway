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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Request {

    private static final String AID_PARAM = "__agentId__";
    private static final Pattern REGEX_PATTERN = Pattern.compile(AID_PARAM + "=([^\\n]+)\\n\\n(.*)");
    private final String agentId;
    private final Map<String, String> params;

    private Request(String agentId, Map<String, String> params) {
        this.agentId = agentId;
        this.params = params;
    }

    Request(String agentId) {
        this.agentId = agentId;
        this.params = new HashMap<>();
    }

    String getAgentId() {
        return agentId;
    }

    static Request fromMessage(String msg) {
        Matcher m = REGEX_PATTERN.matcher(msg);
        if (!m.matches()) {
            throw new AssertionError("Illegal protocol! Got: " + msg);
        }
        String aId = m.group(1);
        String paramStr = m.group(2);
        Map<String, String> paramMap = parseParams(paramStr);
        return new Request(aId, paramMap);
    }

    private static Map<String, String> parseParams(String rawString) {
        if (rawString.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> paramMap = new HashMap<>();
        String[] paramTokens = rawString.split(",");
        for (String sParam : paramTokens) {
            String[] keyVal = sParam.split("=");
            paramMap.put(keyVal[0], keyVal[1]);
        }
        return paramMap;
    }

    String asStringMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(AID_PARAM);
        builder.append("=");
        builder.append(this.agentId);
        builder.append("\n\n");
        for (String key : params.keySet()) {
            builder.append(key);
            builder.append("=");
            builder.append(params.get(key));
            builder.append(",");
        }
        if (!params.isEmpty()) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    String getParam(String name) {
        return params.get(name);
    }

    void setParam(String key, String value) {
        params.put(key, value);
    }
}
