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

package com.redhat.thermostat.gateway.common.mongodb;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters.buildAnd;
import static com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters.buildEq;

import java.util.Arrays;
import java.util.List;

import com.redhat.thermostat.gateway.common.mongodb.filters.MongoQuery;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.CursorType;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;

public class MongoStorageHandler {

    private static final String SET_FIELD_NAME = "set";

    private final MongoResponseBuilder.Builder mongoResponseBuilder = new MongoResponseBuilder.Builder();

    public String getMany(MongoCollection<Document> collection, String queries, Integer limit, Integer offset, String sort, String includes, String excludes) {
        final MongoQuery query = queries == null ? null : new MongoQuery(MongoRequestFilters.buildQueriesFilter(queries));
        return getMany(collection, query, limit, offset, sort, includes, excludes);
    }

    public String getMany(MongoCollection<Document> collection, Bson query, Integer limit, Integer offset, String sort, String includes, String excludes) {
        FindIterable<Document> documents = query == null ? collection.find() : collection.find(query);
        documents = buildProjection(documents, includes, excludes);
        final Bson sortObject = MongoSortFilters.createSortObject(sort);
        documents = documents.sort(sortObject).limit(limit).skip(offset).batchSize(limit).cursorType(CursorType.NonTailable);
        return mongoResponseBuilder.addQueryDocuments(documents).build();
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

    public void addSystemObjects(MongoCollection<DBObject> collection, String systemId, String body) {
        if (body.length() > 0) {
            List<DBObject> inputList = (List<DBObject>) JSON.parse(body);
            for (DBObject o : inputList) {
                o.put(ThermostatFields.SYSTEM_ID, systemId);
            }
            collection.insertMany(inputList);
        }
    }

    public void addJvmObjects(MongoCollection<DBObject> collection, String systemId, String jvmId, String body) {
        if (body.length() > 0) {
            List<DBObject> inputList = (List<DBObject>) JSON.parse(body);
            for (DBObject o : inputList) {
                o.put(ThermostatFields.SYSTEM_ID, systemId);
                o.put(ThermostatFields.JVM_ID, jvmId);
            }
            collection.insertMany(inputList);
        }
    }

    public void deleteMany(MongoCollection<Document> collection, Bson query) {
        collection.deleteMany(query);
    }

    public void updateOneSystemObject(MongoCollection<Document> collection, final String systemId, String queries, String body) {
        Bson sysQuery = buildEq(ThermostatFields.SYSTEM_ID, systemId);
        Bson query  = buildAnd(sysQuery, MongoRequestFilters.buildQueriesFilter(queries));

        BasicDBObject inputObject = (BasicDBObject) JSON.parse(body);
        BasicDBObject setObject = (BasicDBObject) inputObject.get(SET_FIELD_NAME);

        if (setObject.containsField(ThermostatFields.SYSTEM_ID)) {
            throw new UnsupportedOperationException("Updating " + ThermostatFields.SYSTEM_ID + " field is not allowed");
        }

        final Bson fields = new Document("$set", setObject);

        collection.updateMany(query, fields);
    }

    public void updateOneJvmObject(MongoCollection<Document> collection, final String systemId, final String jvmId, String queries, String body) {
        Bson sysQuery = buildEq(ThermostatFields.SYSTEM_ID, systemId);
        Bson jvmQuery = buildEq(ThermostatFields.JVM_ID, jvmId);
        Bson query  = buildAnd(buildAnd(sysQuery, jvmQuery), MongoRequestFilters.buildQueriesFilter(queries));

        BasicDBObject inputObject = (BasicDBObject) JSON.parse(body);
        BasicDBObject setObject = (BasicDBObject) inputObject.get(SET_FIELD_NAME);

        if (setObject.containsField(ThermostatFields.SYSTEM_ID)) {
            throw new UnsupportedOperationException("Updating " + ThermostatFields.SYSTEM_ID + " field is not allowed");
        }
        if (setObject.containsField(ThermostatFields.JVM_ID)) {
            throw new UnsupportedOperationException("Updating " + ThermostatFields.JVM_ID + " field is not allowed");
        }
        final Bson fields = new Document("$set", setObject);

        collection.updateMany(query, fields);
    }
}
