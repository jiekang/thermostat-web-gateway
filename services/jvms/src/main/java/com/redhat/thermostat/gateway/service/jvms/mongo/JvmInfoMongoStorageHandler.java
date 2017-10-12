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

package com.redhat.thermostat.gateway.service.jvms.mongo;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.thermostat.gateway.common.core.model.LimitParameter;
import com.redhat.thermostat.gateway.common.core.model.OffsetParameter;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;
import com.redhat.thermostat.gateway.common.mongodb.keycloak.KeycloakFields;
import com.redhat.thermostat.gateway.common.mongodb.response.ArgumentRunnable;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;

public class JvmInfoMongoStorageHandler  {

    private static final String SET_KEY = "$set";
    private final MongoResponseBuilder.Builder mongoResponseBuilder = new MongoResponseBuilder.Builder();

    public void updateTimestamps(MongoCollection<Document> collection, String body, String systemId, Long timeStamp) {
        final Bson filter;
        if (body != null && body.length() > 0) {
            @SuppressWarnings("unchecked")
            List<String> jvms = (List<String>) JSON.parse(body);
            List<Bson> jvmFilters = new ArrayList<>();
            for (String id : jvms) {
                jvmFilters.add(eq(StorageFields.JVM_ID, id));
            }
            filter = and(eq(StorageFields.SYSTEM_ID, systemId), or(jvmFilters));
        } else {
            filter = eq(StorageFields.SYSTEM_ID, systemId);
        }

        final Bson lastUpdated = new Document(StorageFields.LAST_UPDATED, timeStamp);
        final Bson update = new Document(SET_KEY, lastUpdated);
        collection.updateMany(filter, update);
    }

    private void setIsAlive(Document document) {
        if(document.getLong(StorageFields.STOP_TIME) != null && document.getLong(StorageFields.STOP_TIME) > 0) {
            document.append(StorageFields.IS_ALIVE, false);
        } else {
            document.append(StorageFields.IS_ALIVE, true);
        }
    }

    public String getJvmInfo(MongoCollection<Document> collection, String systemId, String jvmId, String includes, String excludes) {
        Bson baseQuery = and(eq(StorageFields.SYSTEM_ID, systemId), eq(StorageFields.JVM_ID, jvmId));

        FindIterable<Document> documents = collection.find(baseQuery).limit(1).skip(0);

        if (excludes != null) {
            List<String> excludesList = Arrays.asList(excludes.split(","));
            documents = documents.projection(fields(exclude(excludesList), excludeId()));
        } else if (includes != null) {
            List<String> includesList = Arrays.asList(includes.split(","));
            documents = documents.projection(fields(include(includesList), excludeId()));
        } else {
            documents = documents.projection(excludeId());
        }

        ArgumentRunnable<Document> runnable = new ArgumentRunnable<Document>() {
            @Override
            public void run(Document arg) {
                setIsAlive(arg);
            }
        };

        return mongoResponseBuilder.addQueryDocuments(documents, runnable).build();
    }

    public String getJvmInfos(MongoCollection<Document> collection, String systemId, LimitParameter limit, OffsetParameter offset, String sort, String queries, String includes, String excludes) {
        final Bson baseQuery;
        baseQuery = eq(StorageFields.SYSTEM_ID, systemId);
        FindIterable<Document> documents;

        if (queries != null) {
            List<String> queriesList = Arrays.asList(queries.split(","));
            final Bson query = MongoRequestFilters.buildQueriesFilter(queriesList);
            documents = collection.find(and(baseQuery, query));
        } else {
            documents = collection.find(baseQuery);
        }

        ArgumentRunnable<Document> runnable = new ArgumentRunnable<Document>() {
            @Override
            public void run(Document arg) {
                setIsAlive(arg);
            }
        };

        if (excludes != null) {
            List<String> excludesList = Arrays.asList(excludes.split(","));
            documents = documents.projection(fields(exclude(excludesList), excludeId()));
        } else if (includes != null) {
            List<String> includesList = Arrays.asList(includes.split(","));
            documents = documents.projection(fields(include(includesList), excludeId()));
        } else {
            documents = documents.projection(excludeId());
        }

        final Bson sortObject = MongoSortFilters.createSortObject(sort);
        documents = documents.sort(sortObject).limit(limit.getValue()).skip(offset.getValue()).batchSize(limit.getValue()).cursorType(CursorType.NonTailable);

        return mongoResponseBuilder.addQueryDocuments(documents, runnable).build();
    }

    public String getJvmsTree(MongoCollection<Document> collection, boolean aliveOnly, String excludes, String includes, int limit, int offset) {
        FindIterable<Document> documents;

        if (aliveOnly) {
            documents = collection.find(lt(StorageFields.STOP_TIME, 0));
        } else {
            documents = collection.find();
        }

        documents = documents.limit(limit).skip(offset);

        boolean includeSystemId = true;
        if (excludes != null) {
            List<String> excludesList = new ArrayList<>(Arrays.asList(excludes.split(",")));
            if (excludesList.contains(StorageFields.SYSTEM_ID)) {
                excludesList.remove(StorageFields.SYSTEM_ID);
                includeSystemId = false;
            }
            if (excludesList.size() > 0) {
                documents = documents.projection(fields(exclude(excludesList), excludeId()));
            } else {
                documents = documents.projection(excludeId());
            }
        } else if (includes != null) {
            List<String> includesList = new ArrayList<>(Arrays.asList(includes.split(",")));
            if (!includesList.contains(StorageFields.SYSTEM_ID)) {
                includesList.add(StorageFields.SYSTEM_ID);
                includeSystemId = false;
            }
            documents = documents.projection(fields(include(includesList), excludeId()));
        } else {
            documents = documents.projection(excludeId());
        }

        final Map<String, StringBuilder> map = new HashMap<>();

        final boolean finalIncludeSystemId = includeSystemId;
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                document.remove(KeycloakFields.REALMS_KEY);
                setIsAlive(document);

                String systemId = document.getString(StorageFields.SYSTEM_ID);
                if (!finalIncludeSystemId) {
                    document.remove(StorageFields.SYSTEM_ID);
                }

                if (!map.containsKey(systemId)) {
                    map.put(systemId, new StringBuilder().append("{\"" + StorageFields.SYSTEM_ID + "\":\"" + systemId + "\", \"" + StorageFields.JVMS + "\":["));
                }

                map.get(systemId).append(document.toJson()).append(",");
            }
        });

        StringBuilder responseBuilder = new StringBuilder().append("{ \"" + StorageFields.RESPONSE + "\" : [");
        if (map.size() > 0) {
            for (StringBuilder systemBuilder : map.values()) {
                responseBuilder.append(systemBuilder.deleteCharAt(systemBuilder.length() - 1).toString());
                responseBuilder.append("]},");
            }
            responseBuilder.deleteCharAt(responseBuilder.length() - 1);
        }
        responseBuilder.append("]}");

        return responseBuilder.toString();
    }
}
