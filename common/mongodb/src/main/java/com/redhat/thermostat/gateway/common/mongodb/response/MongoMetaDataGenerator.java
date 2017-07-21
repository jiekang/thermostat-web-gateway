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

import javax.servlet.http.HttpServletRequest;

import com.redhat.thermostat.gateway.common.mongodb.servlet.RequestParameters;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoDataResultContainer;

public class MongoMetaDataGenerator {

    private final Integer limit;
    private final Integer offset;
    private final String sort;
    private final String queries;
    private final String includes;
    private final String excludes;
    private final MongoDataResultContainer execResult;
    private final HttpServletRequest requestInfo;
    private final String baseURL;

    public MongoMetaDataGenerator(Integer limit, Integer offset, String sort, String queries, String includes, String excludes,
                                  HttpServletRequest requestInfo, MongoDataResultContainer execResult) {
        this.limit = limit;
        this.offset = offset;
        this.sort = sort;
        this.queries = queries;
        this.includes = includes;
        this.excludes = excludes;
        this.execResult = execResult;
        this.requestInfo = requestInfo;
        this.baseURL = requestInfo.getRequestURL().toString();
    }

    public void setDocAndPayloadCount(MongoMetaDataResponseBuilder.MetaBuilder response) {
        if (!"".equals(queries)) {
            Integer docCount = (int) execResult.getGetReqCount();
            Integer payloadCount = (limit > 1) ? 0 : docCount;
            response.count(docCount).payloadCount(payloadCount);
        }
    }

    public void setPrev(MongoMetaDataResponseBuilder.MetaBuilder response) {
        if (!Integer.valueOf(0).equals(offset)) {
            StringBuilder prev = new StringBuilder();
            String[] arguments = requestInfo.getQueryString().split("&");
            prev.append(baseURL).append("?").append(response.getQueryArgumentsNoOffsetLimit(arguments));

            if (limit > 1) {
                int newLim = (offset >= limit) ? limit : offset;
                int newOff = (offset >= limit) ? (offset - limit) : 0;
                prev.append("&").append(RequestParameters.LIMIT + '=').append(newLim).append("&").append(RequestParameters.OFFSET + '=').append(newOff);
            } else {
                prev.append("&").append(RequestParameters.LIMIT + '=').append(offset);
            }

            response.prev(prev.toString());
        }
    }

    public void setNext(MongoMetaDataResponseBuilder.MetaBuilder response) {
        if (!"".equals(queries)) {
            int remaining = execResult.getRemainingNumQueryDocuments();
            if (!limit.equals(0) && remaining != 0) {
                StringBuilder next = new StringBuilder();
                int nextLimit = (remaining > limit) ? limit : remaining;
                next.append(baseURL).append('?' + RequestParameters.OFFSET + '=').append(offset + limit).append('&' + RequestParameters.LIMIT + '=').append(nextLimit).append("&");

                String[] arguments = requestInfo.getQueryString().split("&");
                next.append(response.getQueryArgumentsNoOffsetLimit(arguments));

                response.payloadCount(nextLimit).next(next.toString());
            }
        }
    }

}
