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

package com.redhat.thermostat.gateway.service.jvm.gc.mongo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatFields;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoDataResultContainer;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoExecutor;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;
import com.redhat.thermostat.gateway.common.mongodb.response.ArgumentRunnable;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataGenerator;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.servlet.RequestParameters;

public class JvmGcMongoStorageHandler {

    private static final String LONG_KEY = "$numberLong";
    private final MongoExecutor mongoExecutor = new MongoExecutor();

    public String getJvmGcDelta(MongoCollection<Document> collection,
                                String jvmId, int limit, int offset,
                                Long afterTimeStamp, Long beforeTimeStamp, boolean metadata,
                                HttpServletRequest httpServletRequest, Set<String> realms) throws IOException {
        final String descendingSort = MongoSortFilters.DESCENDING_SORT + ThermostatFields.TIMESTAMP;

        List<String> queries = new ArrayList<>();
        queries.add(ThermostatFields.JVM_ID + "==" + jvmId);

        if (afterTimeStamp != null) {
            queries.add(ThermostatFields.TIMESTAMP + ">" + afterTimeStamp);
        }
        if (beforeTimeStamp != null) {
            queries.add(ThermostatFields.TIMESTAMP + "<" + beforeTimeStamp);
        }

        MongoDataResultContainer execResult = mongoExecutor.execGetRequest(
                collection, limit, offset, descendingSort, queries, null, null, realms);

        MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();

        final Map<String, Document> previous = new HashMap<>();
        ArgumentRunnable<Document> runnable = new ArgumentRunnable<Document>() {
            @Override
            public void run(Document current) {
                if (current.containsKey(Fields.WALL_TIME) && current.containsKey(Fields.COLLECTOR_NAME)) {
                    String collectorName = current.getString(Fields.COLLECTOR_NAME);
                    if (previous.containsKey(collectorName)) {
                        Document pre = previous.get(collectorName);
                        long prevWall = pre.get(Fields.WALL_TIME, Long.class);
                        long thisWall = current.get(Fields.WALL_TIME, Long.class);
                        long wallTimeDelta = prevWall - thisWall;
                        setWallTimeDelta(pre, wallTimeDelta);
                        previous.put(collectorName, current);
                    } else {
                        previous.put(collectorName, current);
                    }
                }
            }
        };
        response.addQueryDocuments(execResult.getQueryDataResult(), runnable);

        // At this point, previous contains the last Documents for each collector
        // in the list, which is sorted by timeStamp descending. Find the next
        // Document in storage with timeStamp less than this last Document to calculate
        // delta for the last Document. Set delta to 0 if there is no Document

        for (Map.Entry<String, Document> item : previous.entrySet()) {
            List<String> nextQuery = new ArrayList<>();

            nextQuery.add(ThermostatFields.TIMESTAMP + "<" +
                    item.getValue().get(ThermostatFields.TIMESTAMP, Long.class));
            nextQuery.add(Fields.COLLECTOR_NAME + "==" + item.getKey());
            nextQuery.add(ThermostatFields.JVM_ID + "==" + jvmId);

            MongoDataResultContainer execLastResult = mongoExecutor.execGetRequest(
                    collection, 1, 0, descendingSort, nextQuery, null, null, realms);

            Document first = execLastResult.getQueryDataResult().first();

            long wallTimeDelta = 0;
            if (first != null) {
                long prevWall = item.getValue().get(Fields.WALL_TIME, Long.class);
                long lastWall = first.get(Fields.WALL_TIME, Long.class);
                wallTimeDelta = prevWall - lastWall;
            }
            setWallTimeDelta(item.getValue(), wallTimeDelta);
        }

        if (metadata) {
            setMetadata(limit, offset, descendingSort, null, null, null, httpServletRequest, execResult, response);
        }

        return response.build();
    }

    private void setMetadata(int limit, int offset, String sort, String queries, String includes, String excludes, HttpServletRequest httpServletRequest,
                             MongoDataResultContainer execResult, MongoResponseBuilder.Builder response) {
        MongoMetaDataResponseBuilder.MetaBuilder metaDataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();

        LinkedHashMap<String, String> paramArgs = new LinkedHashMap<>();
        paramArgs.put(RequestParameters.SORT, sort);
        paramArgs.put(RequestParameters.QUERY, queries);
        paramArgs.put(RequestParameters.INCLUDE, includes);
        paramArgs.put(RequestParameters.EXCLUDE, excludes);
        paramArgs.put(RequestParameters.METADATA, "true");
        paramArgs.put(RequestParameters.LIMIT, String.valueOf(limit));
        paramArgs.put(RequestParameters.OFFSET, String.valueOf(offset));
        String baseUrl = httpServletRequest.getRequestURL().toString();

        MongoMetaDataGenerator metaDataGenerator = new MongoMetaDataGenerator(limit, offset, sort, queries, includes, excludes, paramArgs, execResult, baseUrl);

        metaDataGenerator.setDocAndPayloadCount(metaDataResponse);
        metaDataGenerator.setPrev(metaDataResponse);
        metaDataGenerator.setNext(metaDataResponse);

        response.addMetaData(metaDataResponse.build());
    }

    private void setWallTimeDelta(Document toSet, long delta) {
        Document d = new Document();
        d.put(LONG_KEY, delta);
        toSet.put(Fields.WALL_TIME_DELTA, d);
    }
}
