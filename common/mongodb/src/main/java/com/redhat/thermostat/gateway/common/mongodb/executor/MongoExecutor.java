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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.CursorType;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;
import com.redhat.thermostat.gateway.common.mongodb.keycloak.KeycloakFields;

public class MongoExecutor {
    public MongoDataResultContainer execGetRequest(MongoCollection<Document> collection, Integer limit, Integer offset,
                                                   String sort, String queries, String projections,
                                                   Set<String> realms) throws IOException {
        return execGetRequest(collection, limit, offset, sort, buildClientQueries(queries), projections, realms);
    }

    public MongoDataResultContainer execGetRequest(MongoCollection<Document> collection, Integer limit, Integer offset,
                                                   String sort, List<String> queries, String projections,
                                                   Set<String> realms) {
        FindIterable<Document> documents = collection.find();
        MongoDataResultContainer queryDataContainer = new MongoDataResultContainer();

        Bson query = MongoRequestFilters.buildQuery(queries, realms);
        documents = documents.filter(query);

        long count = collection.count(query);
        queryDataContainer.setGetReqCount(count);
        queryDataContainer.setRemainingNumQueryDocuments((int) (count - (limit + offset)));

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

    public MongoDataResultContainer execPutRequest(MongoCollection<Document> collection, String body,
                                                   String queries, Set<String> realms) throws IOException {
        return execPutRequest(collection, body, buildClientQueries(queries), realms);
    }

    public MongoDataResultContainer execPutRequest(MongoCollection<Document> collection, String body,
                                                   List<String> queries, Set<String> realms) {
        Document inputDocument = Document.parse(body);
        MongoDataResultContainer metaDataContainer = new MongoDataResultContainer();

        Document setDocument = inputDocument.get("set", Document.class);
        setDocument.remove(KeycloakFields.REALMS_KEY);
        final Bson fields = new Document("$set", setDocument);

        final Bson bsonQueries = MongoRequestFilters.buildQuery(queries, realms);

        collection.updateMany(bsonQueries, fields);

        metaDataContainer.setPutReqMatches(collection.count(bsonQueries));

        return metaDataContainer;
    }


    public MongoDataResultContainer execDeleteRequest(MongoCollection<Document> collection, String queries,
                                                      Set<String> realms) throws IOException {
        return execDeleteRequest(collection, buildClientQueries(queries), realms);
    }

    public MongoDataResultContainer execDeleteRequest(MongoCollection<Document> collection, List<String> queries,
                                                      Set<String> realms) {
        MongoDataResultContainer metaDataContainer = new MongoDataResultContainer();
        if (queries != null && !queries.isEmpty() || realms != null && !realms.isEmpty()) {
            Bson bsonQueries = MongoRequestFilters.buildQuery(queries, realms);
            metaDataContainer.setDeleteReqMatches(collection.count(bsonQueries));
            collection.deleteMany(bsonQueries);
        } else {
            metaDataContainer.setDeleteReqMatches(collection.count());
            collection.drop();
        }

        return metaDataContainer;
    }

    public MongoDataResultContainer execPostRequest(MongoCollection<DBObject> collection, String body,
                                                    Set<String> realms) {
        MongoDataResultContainer metaDataContainer = new MongoDataResultContainer();

        if (body.length() > 0) {
            List<DBObject> inputList = (List<DBObject>) JSON.parse(body);

            for (DBObject object : inputList) {
                object.removeField(KeycloakFields.REALMS_KEY);
                if (realms != null && !realms.isEmpty())  {
                    object.put(KeycloakFields.REALMS_KEY, realms);
                }

            }

            collection.insertMany(inputList);
        }

        return metaDataContainer;
    }


    private List<String> buildClientQueries(String queries) throws IOException {
        if (queries != null) {
            List<String> queriesList = Arrays.asList(queries.split(","));
            for (String query : queriesList) {
                if (query.startsWith(KeycloakFields.REALMS_KEY)) {
                    throw new IOException("Cannot query realms property");
                }
            }

            return queriesList;
        } else {
            return Collections.emptyList();
        }
    }
}