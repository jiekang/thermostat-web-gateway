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

import com.mongodb.CursorType;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;

public class MongoExecutor {
    public MongoDataResultContainer execGetRequest(MongoCollection<Document> collection, Integer limit,
                                                   Integer offset, String sort, String queries, String projections) {
        return execGetRequest(collection, limit, offset, sort, buildQueries(queries), projections);
    }

    public MongoDataResultContainer execGetRequest(MongoCollection<Document> collection, Integer limit,
                                                   Integer offset, String sort, List<String> queries, String projections) {
        FindIterable<Document> documents = collection.find();
        MongoDataResultContainer queryDataContainer = new MongoDataResultContainer();

        if (queries != null && !queries.isEmpty()) {
            final Bson query = MongoRequestFilters.buildQueriesFilter(queries);
            documents = documents.filter(query);
            queryDataContainer.setGetReqCount(collection.count(query));
            queryDataContainer.setRemainingNumQueryDocuments((int) (collection.count(query) - (limit + offset)));
        } else {
            queryDataContainer.setGetReqCount(collection.count());
            queryDataContainer.setRemainingNumQueryDocuments((int) (collection.count() - (limit + offset)));
        }

        if (projections != null) {
            List<String> projectionsList = Arrays.asList(projections.split(","));
            documents = documents.projection(fields(include(projectionsList), excludeId()));
        } else {
            documents = documents.projection(excludeId());
        }

        final Bson sortObject = MongoSortFilters.createSortObject(sort);
        documents = documents.sort(sortObject).limit(limit).skip(offset).batchSize(limit).cursorType(CursorType.NonTailable);
        queryDataContainer.setQueryDataResult(documents);

        return queryDataContainer;
    }

    public MongoDataResultContainer execPutRequest(MongoCollection<Document> collection, String body, String queries) {
        return execPutRequest(collection, body, buildQueries(queries));
    }

    public MongoDataResultContainer execPutRequest(MongoCollection<Document> collection, String body, List<String> queries) {
        Document inputDocument = Document.parse(body);
        MongoDataResultContainer metaDataContainer = new MongoDataResultContainer();

        Document setDocument = inputDocument.get("set", Document.class);
        final Bson fields = new Document("$set", setDocument);

        Bson bsonQueries;
        if (queries != null && !queries.isEmpty()) {
            bsonQueries = MongoRequestFilters.buildQueriesFilter(queries);
        } else {
            bsonQueries = new Document();
        }

        collection.updateMany(bsonQueries, fields);

        metaDataContainer.setPutReqMatches(collection.count(bsonQueries));

        return metaDataContainer;
    }


    public MongoDataResultContainer execDeleteRequest(MongoCollection<Document> collection, String queries) {
        return execDeleteRequest(collection, buildQueries(queries));
    }

    public MongoDataResultContainer execDeleteRequest(MongoCollection<Document> collection, List<String> queries) {
        MongoDataResultContainer metaDataContainer = new MongoDataResultContainer();
        if (queries != null && !queries.isEmpty()) {
            Bson bsonQueries = MongoRequestFilters.buildQueriesFilter(queries);
            collection.deleteMany(bsonQueries);

            metaDataContainer.setDeleteReqMatches(collection.count(bsonQueries));
        } else {
            metaDataContainer.setDeleteReqMatches(collection.count());
            collection.drop();
        }

        return metaDataContainer;
    }

    public MongoDataResultContainer execPostRequest(MongoCollection<DBObject> collection, String body) {
        MongoDataResultContainer metaDataContainer = new MongoDataResultContainer();

        if (body.length() > 0) {
            List<DBObject> inputList = (List<DBObject>) JSON.parse(body);
            collection.insertMany(inputList);
        }

        return metaDataContainer;
    }


    private List<String> buildQueries(String queries) {
        if (queries != null) {
            return Arrays.asList(queries.split(","));
        } else {
            return Collections.emptyList();
        }
    }
}