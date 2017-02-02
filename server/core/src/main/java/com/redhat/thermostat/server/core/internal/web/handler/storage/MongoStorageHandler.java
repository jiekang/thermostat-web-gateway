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

package com.redhat.thermostat.server.core.internal.web.handler.storage;

import java.io.IOException;
import java.util.Collections;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.glassfish.jersey.server.ChunkedOutput;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.redhat.thermostat.server.core.internal.storage.ThermostatMongoStorage;
import com.redhat.thermostat.server.core.internal.web.filters.RequestFilters;
import com.redhat.thermostat.server.core.internal.web.json.DocumentBuilder;
import com.redhat.thermostat.server.core.internal.web.request.TimedRequest;
import com.redhat.thermostat.server.core.internal.web.response.MongoResponseBuilder;

public class MongoStorageHandler implements StorageHandler {

    private final int MAX_MONGO_DOCUMENTS = 5000;

    @Override
    public void getAgent(final SecurityContext securityContext,
                         final AsyncResponse asyncResponse,
                         final String agentId,
                         final String count,
                         final String sort) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ThermostatMongoStorage.isConnected()) {
                    asyncResponse.resume(Response.status(Response.Status.OK).entity("GET " + agentId + " " + count + " " + sort + " " + securityContext.getUserPrincipal().getName()).build());
                    return;
                }

                final int limit = Math.min(Integer.valueOf(count), MAX_MONGO_DOCUMENTS);
                final int sortOrder = Integer.valueOf(sort);
                final String userName = securityContext.getUserPrincipal().getName();
                final Bson filter = RequestFilters.buildGetFilter(agentId, Collections.singletonList(userName));

                TimedRequest<FindIterable<Document>> timedRequest = new TimedRequest<>();

                FindIterable<Document> documents = timedRequest.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
                    @Override
                    public FindIterable<Document> run() {
                        return ThermostatMongoStorage.getDatabase().getCollection("agents").find(filter).sort(new BasicDBObject("_id", sortOrder)).limit(limit);
                    }
                });

                asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponse(documents, timedRequest.getElapsed())).build());
            }
        }).start();
    }

    @Override
    public Response putAgent(String body,
                             @Context SecurityContext context) {
        if (!ThermostatMongoStorage.isConnected()) {
            return Response.status(Response.Status.OK).entity("PUT " + context.getUserPrincipal().getName() + "\n\n" + body).build();
        }

        TimedRequest<FindIterable<Document>> timedRequest = new TimedRequest<>();

        /*
         * TODO: Verify body matches expected schema
         * TODO: Clean up insertion of tags into JSON body
         */
        final Document item = Document.parse(DocumentBuilder.addTags(body, context.getUserPrincipal().getName()));

        timedRequest.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
            @Override
            public FindIterable<Document> run() {
                ThermostatMongoStorage.getDatabase().getCollection("agents").insertOne(item);
                return null;
            }
        });

        return Response.status(Response.Status.OK).entity("PUT successful").build();
    }

    @Override
    public Response getHostCpuInfo(SecurityContext securityContext,
                                   String agentId,
                                   String count,
                                   String sort,
                                   String maxTimestamp,
                                   String minTimestamp) {
        if (!ThermostatMongoStorage.isConnected()) {
            return Response.status(Response.Status.OK).entity(agentId + count + sort + maxTimestamp + minTimestamp).build();
        }

        final int size = Integer.valueOf(count);

        final String userName = securityContext.getUserPrincipal().getName();
        final Bson filter = RequestFilters.buildGetFilter(agentId, Collections.singletonList(userName), maxTimestamp, minTimestamp);

        final int sortOrder = Integer.valueOf(sort);

        TimedRequest<FindIterable<Document>> request = new TimedRequest<>();
        FindIterable<Document> documents = request.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
            @Override
            public FindIterable<Document> run() {
                return ThermostatMongoStorage.getDatabase().getCollection("cpu-stats").find(filter).sort(new BasicDBObject("_id", sortOrder)).limit(size);
            }
        });

        return Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponse(documents, request.getElapsed())).build();
    }

    @Override
    public ChunkedOutput<String> streamHostCpuInfo(SecurityContext securityContext, String agentId) {
        final ChunkedOutput<String> output = new ChunkedOutput<>(String.class, "\r\n");

        new Thread() {
            public void run() {
                try {

                    while (true) {
                        TimedRequest<FindIterable<Document>> request = new TimedRequest<>();
                        FindIterable<Document> documents = request.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
                            @Override
                            public FindIterable<Document> run() {
                                return ThermostatMongoStorage.getDatabase().getCollection("cpu-stats").find().sort(new BasicDBObject("_id", -1)).limit(1);
                            }
                        });
                        output.write(MongoResponseBuilder.buildJsonResponse(documents, request.getElapsed()));

                        Thread.sleep(1000L);
                    }
                } catch (IOException | InterruptedException e) {
                    // An IOException occurs when reader closes the connection
                } finally {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        return output;
    }
}
