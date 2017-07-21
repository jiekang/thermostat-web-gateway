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

import com.redhat.thermostat.gateway.common.mongodb.MongoStorageHandler;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;

public class JvmInfoMongoStorageHandler extends MongoStorageHandler {

    private static final String SET_KEY = "$set";

    public void updateTimestamps(MongoCollection<Document> collection, String body, String systemId, Long timeStamp) {
        final Bson filter;
        if (body != null && body.length() > 0) {
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
