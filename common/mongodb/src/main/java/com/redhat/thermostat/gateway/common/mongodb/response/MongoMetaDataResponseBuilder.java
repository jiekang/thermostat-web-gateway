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

package com.redhat.thermostat.gateway.common.mongodb.response;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.thermostat.gateway.common.core.servlet.RequestParameters;

/*
 *  Builds the appropriate metadata for the response after executing the request's MongoDB Query.
 *
 *  NOTE: Builder fields that aren't explicitly set - and therefore null - are omitted in the
 *        serialized JSON.
 */
public class MongoMetaDataResponseBuilder {

    private final transient Gson gson = new GsonBuilder().create();
    private final Integer payloadCount;
    private final Integer count;
    private final String prev;
    private final String next;
    private final String first;
    private final String last;
    private final Long insertCount;
    private final Long matchCount;
    private final Integer elapsed;

    public static class MetaBuilder {

        private Integer payloadCount;
        private Integer count;
        private String prev;
        private String next;
        private String first;
        private String last;
        private Long insertCount;
        private Long matchCount;
        private Integer elapsed;

        public MetaBuilder payloadCount(Integer payload) {
            this.payloadCount = payload;
            return this;
        }

        public MetaBuilder count(Integer count) {
            this.count = count;
            return this;
        }

        public MetaBuilder prev(String prevUrl) {
            this.prev = prevUrl;
            return this;
        }

        public MetaBuilder next(String nextUrl) {
            this.next = nextUrl;
            return this;
        }

        public MetaBuilder first(String firstUrl) {
            this.first = firstUrl;
            return this;
        }

        public MetaBuilder last(String lastUrl) {
            this.last = lastUrl;
            return this;
        }

        public MetaBuilder insertCount(Long count) {
            this.insertCount = count;
            return this;
        }

        public MetaBuilder matchCount(Long count) {
            this.matchCount = count;
            return this;
        }

        public MetaBuilder elapsed(Integer elapsed) {
            this.elapsed = elapsed;
            return this;
        }

        public MongoMetaDataResponseBuilder build() {
            return new MongoMetaDataResponseBuilder(this);
        }

        public String getQueryArgumentsNoOffsetLimit(Map<String, String> requestParamArguments) {
            StringBuilder queryString = new StringBuilder();
            String sep = "";
            for (String param : requestParamArguments.keySet()) {
                String val = requestParamArguments.get(param);
                if (!(RequestParameters.LIMIT.equals(param) || RequestParameters.OFFSET.equals(param) || (val == null))) {
                    queryString.append(sep).append(param).append("=").append(val);
                    sep = "&";
                }
            }
            return queryString.toString();
        }
    }

    private MongoMetaDataResponseBuilder(MetaBuilder builder) {
        payloadCount = builder.payloadCount;
        count = builder.count;
        prev = builder.prev;
        next = builder.next;
        first = builder.first;
        last = builder.last;
        insertCount = builder.insertCount;
        matchCount = builder.matchCount;
        elapsed = builder.elapsed;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}