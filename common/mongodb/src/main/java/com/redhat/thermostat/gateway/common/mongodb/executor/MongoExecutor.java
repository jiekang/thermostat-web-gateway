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

package com.redhat.thermostat.gateway.common.mongodb.executor;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

public class MongoExecutor {
    private final MongoResponseBuilder mongoResponseBuilder = new MongoResponseBuilder();

    public String buildGetResponse(MongoCollection<Document> collection, Integer limit, Integer offset, String sort, String queries, String projections) {
        FindIterable<Document> documents;
        if (queries != null) {
            List<String> queriesList = Arrays.asList(queries.split(","));
            final Bson query = MongoRequestFilters.buildQueriesFilter(queriesList);
            documents = collection.find(query);
        } else {
            documents = collection.find();
        }

        if (projections != null) {
            List<String> projectionsList = Arrays.asList(projections.split(","));
            documents = documents.projection(fields(include(projectionsList), excludeId()));
        } else {
            documents = documents.projection(excludeId());
        }

        final Bson sortObject = MongoSortFilters.createSortObject(sort);
        documents = documents.sort(sortObject).limit(limit).skip(offset).batchSize(limit).cursorType(CursorType.NonTailable);

        return mongoResponseBuilder.buildGetResponseString(documents);
    }

    public void buildPutResponse(MongoCollection<Document> collection, String body, String queries) {
        BasicDBObject inputObject = (BasicDBObject) JSON.parse(body);

        final List<String> queriesList;
        if (queries != null) {
            queriesList = Arrays.asList(queries.split(","));
        } else {
            queriesList = Collections.emptyList();
        }

        BasicDBObject setObject = (BasicDBObject) inputObject.get("set");
        final Bson fields = new Document("$set", setObject);

        collection.updateMany(MongoRequestFilters.buildQueriesFilter(queriesList), fields);
    }

    public void buildDeleteResponse(MongoCollection<Document> collection, String queries) {
        List<String> queriesList;
        if (queries != null) {
            queriesList = Arrays.asList(queries.split(","));
            collection.deleteMany(MongoRequestFilters.buildQueriesFilter(queriesList));

        } else {
            collection.drop();
        }
    }

    public void buildPost(MongoCollection<DBObject> collection, String body) {
        if (body.length() > 0) {
            List<DBObject> inputList = (List<DBObject>) JSON.parse(body);
            collection.insertMany(inputList);
        }
    }
}
