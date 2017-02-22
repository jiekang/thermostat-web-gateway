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

package com.redhat.thermostat.server.core.internal.web.handler.storage.mongo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
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
import com.redhat.thermostat.server.core.internal.web.handler.storage.StorageHandler;
import com.redhat.thermostat.server.core.internal.web.json.DocumentBuilder;
import com.redhat.thermostat.server.core.internal.web.request.TimedRequest;
import com.redhat.thermostat.server.core.internal.web.response.MongoResponseBuilder;

public class MongoStorageHandler implements StorageHandler {

    private final int BASE_OFFSET = 0;
    private final int MAX_MONGO_DOCUMENTS = 5000;

    @Override
    public void getSystems(SecurityContext context, AsyncResponse asyncResponse, String namespace, String offset, String limit, String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void putSystems(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void postSystems(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void deleteSystems(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void getSystem(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void putSystem(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void deleteSystem(SecurityContext context, AsyncResponse asyncResponse, String namespace) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void getAgents(final SecurityContext securityContext, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String offset, final String limit, final String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final int o = Math.min(Integer.valueOf(offset), BASE_OFFSET);
                final int c = Math.min(Integer.valueOf(limit), MAX_MONGO_DOCUMENTS);
                final String userName = securityContext.getUserPrincipal().getName();
                final Bson filter = RequestFilters.buildGetFilter(systemId, Collections.singletonList(userName));

                TimedRequest<FindIterable<Document>> timedRequest = new TimedRequest<>();

                FindIterable<Document> documents = timedRequest.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
                    @Override
                    public FindIterable<Document> run() {
                        return ThermostatMongoStorage.getDatabase().getCollection(namespace + "-agent").find(filter).sort(createSortObject(sort)).limit(c).skip(o);
                    }
                });

                asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponseWithTime(documents, timedRequest.getElapsed())).build());
            }
        }).start();
    }

    @Override
    public void putAgents(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, String systemId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonReader reader = Json.createReader(new StringReader(body));
                JsonArray array = reader.readArray();
                reader.close();

                final List<Document> items = new ArrayList<>();
                for (JsonValue value : array) {
                    items.add(Document.parse(DocumentBuilder.addTags(value.toString(), context.getUserPrincipal().getName())));
                }

                TimedRequest<Boolean> timedRequest = new TimedRequest<>();

                Boolean response = timedRequest.run(new TimedRequest.TimedRunnable<Boolean>() {
                    @Override
                    public Boolean run() {
                        try {
                            ThermostatMongoStorage.getDatabase().getCollection(namespace + "-agents").insertMany(items);
                        } catch (Exception e) {
                            return Boolean.FALSE;
                        }
                        return Boolean.TRUE;
                    }
                });

                asyncResponse.resume(Response.status(Response.Status.OK).entity("PUT: " + response.toString()).build());
            }
        }).start();
    }

    @Override
    public void postAgents(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void deleteAgents(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void getAgent(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId, String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void putAgent(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void deleteAgent(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void getJvms(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String vmId, String offset, String limit, String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void putJvms(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void postJvms(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void deleteJvms(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void getJvm(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String jvmId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void putJvm(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String jvmId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    @Override
    public void deleteJvm(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String jvmId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }

    public Response putAgent(String body,
                             @Context SecurityContext context) {
        if (!ThermostatMongoStorage.isConnected()) {
            return Response.status(Response.Status.OK).entity("PUT " + context.getUserPrincipal().getName() + "\n\n" + body).build();
        }

        TimedRequest<Boolean> timedRequest = new TimedRequest<>();

        /*
         * TODO: Verify body matches expected schema
         * TODO: Clean up insertion of tags into JSON body
         */
        final Document item = Document.parse(DocumentBuilder.addTags(body, context.getUserPrincipal().getName()));

        Boolean response = timedRequest.run(new TimedRequest.TimedRunnable<Boolean>() {
            @Override
            public Boolean run() {
                try {
                    ThermostatMongoStorage.getDatabase().getCollection("agents").insertOne(item);
                } catch (Exception e) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });

        return Response.status(Response.Status.OK).entity("PUT: " + response.toString()).build();
    }

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
                        output.write(MongoResponseBuilder.buildJsonResponseWithTime(documents, request.getElapsed()));

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

    private boolean isMongoConnected(final AsyncResponse asyncResponse) {
        if (ThermostatMongoStorage.isConnected()) {
            return true;
        }

        asyncResponse.resume(Response.status(Response.Status.OK).entity("Unable to access Backing Storage").build());
        return false;
    }


    private Bson createSortObject(String sort) {
        String[] items = sort.split(",");
        BasicDBObject sortObject = new BasicDBObject();
        for (String item : items) {
            sortObject.append(item.substring(1), item.charAt(0));
        }
        return sortObject;
    }

}
