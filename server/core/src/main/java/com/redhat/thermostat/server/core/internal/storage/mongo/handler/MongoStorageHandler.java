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

package com.redhat.thermostat.server.core.internal.storage.mongo.handler;

import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.glassfish.jersey.server.ChunkedOutput;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;
import com.redhat.thermostat.server.core.internal.storage.mongo.ThermostatMongoStorage;
import com.redhat.thermostat.server.core.internal.storage.mongo.request.TimedRequest;
import com.redhat.thermostat.server.core.internal.web.http.StorageHandler;
import com.redhat.thermostat.server.core.internal.storage.mongo.filters.MongoRequestFilters;
import com.redhat.thermostat.server.core.internal.storage.mongo.response.MongoResponseBuilder;
import com.redhat.thermostat.server.core.internal.storage.mongo.response.DocumentBuilder;

public class MongoStorageHandler implements StorageHandler {

    private final int BASE_OFFSET = 0;
    private final int MAX_MONGO_DOCUMENTS = 5000;

    private final String systemCollectionSuffix = "";
    private final String agentCollectionSuffix = "-agents";
    private final String jvmCollectionSuffix = "-jvms";

    @Override
    public void getSystems(final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String offset, final String limit, final String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                getAll(offset, limit, context, systemId, null, null, namespace, sort, asyncResponse, systemCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void putSystems(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        if (systemId.equals("all")) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                putAll(body, context, systemId, null, null, namespace, asyncResponse, systemCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void postSystems(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String offset, final String limit, final String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                postAll(body, offset, limit, context, systemId, null, null, namespace, sort, asyncResponse, agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void deleteSystems(String body, SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAll(systemId, null, null, namespace, asyncResponse, agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void getAgents(final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId, final String offset, final String limit, final String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                getAll(offset,limit,context,systemId,agentId,null,namespace,sort,asyncResponse,agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void putAgents(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }

        if (agentId.equals("all")) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                putAll(body, context, systemId, agentId, null, namespace, asyncResponse, agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void postAgents(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId, final String offset, final String limit, final String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
               postAll(body, offset, limit, context, systemId, agentId, null, namespace, sort, asyncResponse, agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void deleteAgents(String body, SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAll(systemId, agentId, null, namespace, asyncResponse, agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void getJvms(final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId, final String jvmId, final String offset, final String limit, final String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                getAll(offset, limit, context, systemId, agentId, jvmId, namespace, sort, asyncResponse, jvmCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void putJvms(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId, final String jvmId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }

        if (jvmId.equals("all")) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                putAll(body, context, systemId, agentId, jvmId, namespace, asyncResponse, jvmCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void postJvms(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId, final String jvmId, final String offset, final String limit, final String sort) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                postAll(body, offset, limit, context, systemId, agentId, jvmId, namespace, sort, asyncResponse, agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void deleteJvms(String body, SecurityContext context, final AsyncResponse asyncResponse, final String namespace, final String systemId, final String agentId, final String jvmId) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteAll(systemId, agentId, jvmId, namespace, asyncResponse, agentCollectionSuffix);
            }
        }).start();
    }

    @Override
    public void getNamespaces(final SecurityContext context, final AsyncResponse asyncResponse, final String offset, final String limit) {
        if (!isMongoConnected(asyncResponse)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int o = Math.min(Integer.valueOf(offset), BASE_OFFSET);
                        final int c = Math.min(Integer.valueOf(limit), MAX_MONGO_DOCUMENTS);

                        TimedRequest<String> timedRequest = new TimedRequest<>();

                        try {
                            String documents = timedRequest.run(new TimedRequest.TimedRunnable<String>() {
                                @Override
                                public String run() {
                                    FindIterable<Document> documents = ThermostatMongoStorage.getDatabase().getCollection("namespaces").find().limit(c).skip(o);
                                    return MongoResponseBuilder.buildJsonDocuments(documents);
                                }
                            });

                            asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponseWithTime(documents, timedRequest.getElapsed())).build());
                        } catch (Exception e) {
                            asyncResponse.resume(Response.status(Response.Status.OK).entity("Unable to access Backing Storage").build());
                        }
                    }
                }).start();
            }
        }).start();

    }


    @Override
    public ChunkedOutput<String> streamAgents(SecurityContext context, final String namespace, String systemId, final String limit) {
        final ChunkedOutput<String> output = new ChunkedOutput<>(String.class, "\r\n");

        new Thread() {
            public void run() {
                try {
                    final int l = Math.min(Integer.valueOf(limit), MAX_MONGO_DOCUMENTS);

                    while (true) {

                        TimedRequest<String> request = new TimedRequest<>();
                        String documents = request.run(new TimedRequest.TimedRunnable<String>() {
                            @Override
                            public String run() {
                                FindIterable<Document> documents = ThermostatMongoStorage.getDatabase().getCollection(namespace + agentCollectionSuffix).find().sort(new BasicDBObject("_id", -1)).limit(l);
                                return MongoResponseBuilder.buildJsonDocuments(documents);
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

        asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Unable to access Backing Storage").build());
        return false;
    }


    private Bson createSortObject(String sort) {
        BasicDBObject sortObject = new BasicDBObject();
        if (sort != null) {
            String[] items = sort.split(",");
            for (String item : items) {
                if (item.charAt(0) == '+') {
                    sortObject.append("obj." + item.substring(1), 1);
                } else if (item.charAt(0) == '-') {
                    sortObject.append("obj." + item.substring(1), -1);
                }
            }
        }
        return sortObject;
    }

    private void getAll(String offset, String limit, SecurityContext context, String systemId, String agentId, String jvmId, final String namespace, final String sort, AsyncResponse asyncResponse, final String collectionSuffix) {
        try {
            final int o = Math.min(Integer.valueOf(offset), BASE_OFFSET);
            final int l = Math.min(Integer.valueOf(limit), MAX_MONGO_DOCUMENTS);
            final String userName = context.getUserPrincipal().getName();
            final Bson filter = MongoRequestFilters.buildGetFilter(systemId, agentId, jvmId, Collections.singletonList(userName));

            TimedRequest<String> timedRequest = new TimedRequest<>();

            String documents = timedRequest.run(new TimedRequest.TimedRunnable<String>() {
                @Override
                public String run() {
                    FindIterable<Document> documents = ThermostatMongoStorage.getDatabase().getCollection(namespace + collectionSuffix).find(filter).projection(fields(include("obj"), excludeId())).sort(createSortObject(sort)).limit(l).skip(o);
                    return MongoResponseBuilder.buildJsonDocuments(documents);
                }
            });

            asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponseWithTime(documents, timedRequest.getElapsed())).build());
        } catch (Exception e) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
    }

    private void putAll(String body, SecurityContext context, String systemId, String agentId, String jvmId, final String namespace, AsyncResponse asyncResponse, final String collectionSuffix) {
        try {
            BasicDBList inputList = (BasicDBList) JSON.parse(body);

            final List<Document> items = new ArrayList<>();
            for (Object item : inputList) {
                items.add(Document.parse(new DocumentBuilder(item.toString()).addTags(context.getUserPrincipal().getName()).addId("systemId", systemId).addId("agentId", agentId).addId("jvmId", jvmId).build()));
            }

            TimedRequest<Boolean> timedRequest = new TimedRequest<>();

            Boolean response = timedRequest.run(new TimedRequest.TimedRunnable<Boolean>() {
                @Override
                public Boolean run() {
                    try {
                        ThermostatMongoStorage.getDatabase().getCollection(namespace + collectionSuffix).insertMany(items);
                    } catch (Exception e) {
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                }
            });

            asyncResponse.resume(Response.status(Response.Status.OK).entity("PUT: " + response.toString()).build());
        } catch (Exception e) {
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
    }

    private void postAll(String body, String offset, String limit, SecurityContext context, String systemId, String agentId, String jvmId, final String namespace, final String sort, AsyncResponse asyncResponse, final String collectionSuffix) {
        try {
            BasicDBList queries = (BasicDBList) JSON.parse(body);

            final int o = Math.min(Integer.valueOf(offset), BASE_OFFSET);
            final int l = Math.min(Integer.valueOf(limit), MAX_MONGO_DOCUMENTS);
            final String userName = context.getUserPrincipal().getName();
            final Bson filter = MongoRequestFilters.buildPostFilter(queries, systemId, agentId, jvmId, Collections.singletonList(userName));

            TimedRequest<String> timedRequest = new TimedRequest<>();

            String documents = timedRequest.run(new TimedRequest.TimedRunnable<String>() {
                @Override
                public String run() {
                    FindIterable<Document> documents = ThermostatMongoStorage.getDatabase().getCollection(namespace + collectionSuffix).find(filter).projection(fields(exclude("tags"), excludeId())).sort(createSortObject(sort)).limit(l).skip(o);
                    return MongoResponseBuilder.buildJsonDocuments(documents);
                }
            });

            asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponseWithTime(documents, timedRequest.getElapsed())).build());
        } catch (Exception e) {
            e.printStackTrace();
            asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST).build());
        }
    }

    private void deleteAll(final String systemId, final String agentId, final String jvmId, final String namespace, AsyncResponse asyncResponse, final String collectionSuffix) {
        TimedRequest<Boolean> timedRequest = new TimedRequest<>();

        Boolean response = timedRequest.run(new TimedRequest.TimedRunnable<Boolean>() {
            @Override
            public Boolean run() {
                try {
                    ThermostatMongoStorage.getDatabase().getCollection(namespace + collectionSuffix).deleteMany(MongoRequestFilters.buildDeleteFilter(systemId, agentId, jvmId));
                } catch (Exception e) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        asyncResponse.resume(Response.status(Response.Status.OK).entity("DELETE: " + response.toString()).build());
    }

}
