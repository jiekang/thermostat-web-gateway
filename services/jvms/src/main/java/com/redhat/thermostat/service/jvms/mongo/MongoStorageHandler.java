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

package com.redhat.thermostat.service.jvms.mongo;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;

public class MongoStorageHandler {

    private static final String SET_KEY = "$set";
    private final MongoResponseBuilder.Builder mongoResponseBuilder = new MongoResponseBuilder.Builder();

    public String getJvmInfos(MongoCollection<Document> collection, String systemId, Integer limit, Integer offset, String sort, String queries, String includes, String excludes) {
        Bson baseQuery = eq(Fields.SYSTEM_ID, systemId);
        FindIterable<Document> documents;
        if (queries != null) {
            List<String> queriesList = Arrays.asList(queries.split(","));
            final Bson query = MongoRequestFilters.buildQueriesFilter(queriesList);
            documents = collection.find(and(baseQuery, query));
        } else {
            documents = collection.find(baseQuery);
        }

        documents = buildProjection(documents, includes, excludes);

        final Bson sortObject = MongoSortFilters.createSortObject(sort);
        documents = documents.sort(sortObject).limit(limit).skip(offset).batchSize(limit).cursorType(CursorType.NonTailable);

        return mongoResponseBuilder.queryDocuments(documents).build();
    }

    public String getJvmInfo(MongoCollection<Document> collection, String systemId, String jvmId, String includes, String excludes) {
        Bson query = and(eq(Fields.JVM_ID, jvmId), eq(Fields.SYSTEM_ID, systemId));
        FindIterable<Document> documents = collection.find(query);

        documents = buildProjection(documents, includes, excludes);

        documents = documents.limit(1).skip(0).batchSize(1).cursorType(CursorType.NonTailable);

        return mongoResponseBuilder.queryDocuments(documents).build();
    }

    private FindIterable<Document> buildProjection(FindIterable<Document> documents, String includes, String excludes) {
        if (excludes != null) {
            List<String> excludesList = Arrays.asList(excludes.split(","));
            documents = documents.projection(fields(exclude(excludesList), excludeId()));
        } else if (includes != null) {
            List<String> includesList = Arrays.asList(includes.split(","));
            documents = documents.projection(fields(include(includesList), excludeId()));
        } else {
            documents = documents.projection(excludeId());
        }

        return documents;
    }

    public void addJvmInfos(MongoCollection<DBObject> collection, String body, String systemId) {
        if (body.length() > 0) {
            List<DBObject> inputList = (List<DBObject>) JSON.parse(body);
            for (DBObject o : inputList) {
                o.put(Fields.SYSTEM_ID, systemId);
            }
            collection.insertMany(inputList);
        }
    }

    public void deleteJvmInfos(MongoCollection<Document> collection, String systemId) {
        Bson query = eq(Fields.SYSTEM_ID, systemId);
        deleteDocuments(collection, query);
    }
    public void deleteJvmInfo(MongoCollection<Document> collection, String systemId, String jvmId) {
        Bson query = and(eq(Fields.JVM_ID, jvmId), eq(Fields.SYSTEM_ID, systemId));
        deleteDocuments(collection, query);
    }

    private void deleteDocuments(MongoCollection<Document> collection, Bson query) {
        collection.deleteMany(query);
    }

    public void updateJvmInfo(MongoCollection<Document> collection, String body, String systemId, String jvmId, String queries) {
        Bson baseQuery = and(eq(Fields.JVM_ID, jvmId), eq(Fields.SYSTEM_ID, systemId));

        BasicDBObject inputObject = (BasicDBObject) JSON.parse(body);
        BasicDBObject setObject = (BasicDBObject) inputObject.get(Fields.SET);
        if (setObject.containsField(Fields.JVM_ID) || setObject.containsField(Fields.SYSTEM_ID)) {
            throw new UnsupportedOperationException("Updating " +  Fields.SYSTEM_ID + " or " + Fields.JVM_ID + " fields is not allowed");
        }

        final List<String> queriesList;
        if (queries != null) {
            queriesList = Arrays.asList(queries.split(","));
        } else {
            queriesList = Collections.emptyList();
        }

        final Bson fields = new Document(SET_KEY, setObject);

        collection.updateMany(and(baseQuery, MongoRequestFilters.buildQueriesFilter(queriesList)), fields);
    }

    public void updateTimestamps(MongoCollection<Document> collection, String body, String systemId, Long timeStamp) {
        final Bson filter;
        if (body != null && body.length() > 0) {
            List<String> jvms = (List<String>) JSON.parse(body);
            List<Bson> jvmFilters = new ArrayList<>();
            for (String id : jvms) {
                jvmFilters.add(eq(Fields.JVM_ID, id));
            }
            filter = and(eq(Fields.SYSTEM_ID, systemId), or(jvmFilters));
        } else {
            filter = eq(Fields.SYSTEM_ID, systemId);
        }

        final Bson lastUpdated = new Document(Fields.LAST_UPDATED, timeStamp);
        final Bson update = new Document(SET_KEY, lastUpdated);
        collection.updateMany(filter, update);
    }

    public String getJvmsTree(MongoCollection<Document> collection, boolean aliveOnly, String excludes, String includes, int limit, int offset) {
        FindIterable<Document> documents;

        if (aliveOnly) {
            documents = collection.find(lt(Fields.STOP_TIME, 0));
        } else {
            documents = collection.find();
        }

        documents = documents.limit(limit).skip(offset);

        boolean includeSystemId = true;
        if (excludes != null) {
            List<String> excludesList = new ArrayList<>(Arrays.asList(excludes.split(",")));
            if (excludesList.contains(Fields.SYSTEM_ID)) {
                excludesList.remove(Fields.SYSTEM_ID);
                includeSystemId = false;
            }
            if (excludesList.size() > 0) {
                documents = documents.projection(fields(exclude(excludesList), excludeId()));
            } else {
                documents = documents.projection(excludeId());
            }
        } else if (includes != null) {
            List<String> includesList = new ArrayList<>(Arrays.asList(includes.split(",")));
            if (!includesList.contains(Fields.SYSTEM_ID)) {
                includesList.add(Fields.SYSTEM_ID);
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
                String systemId = document.getString(Fields.SYSTEM_ID);
                if (!finalIncludeSystemId) {
                    document.remove(Fields.SYSTEM_ID);
                }

                if (!map.containsKey(systemId)) {
                    map.put(systemId, new StringBuilder().append("{\"" + Fields.SYSTEM_ID + "\":\"" + systemId + "\", \"" + Fields.JVMS + "\":["));
                }

                map.get(systemId).append(document.toJson()).append(",");
            }
        });

        StringBuilder responseBuilder = new StringBuilder().append("{ \"" + Fields.RESPONSE + "\" : [");
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
