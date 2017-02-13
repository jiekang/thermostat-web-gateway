package com.redhat.thermostat.server.core.internal.web.handler.storage;

import java.util.Collections;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.redhat.thermostat.server.core.internal.storage.ThermostatMongoStorage;
import com.redhat.thermostat.server.core.internal.web.filters.RequestFilters;
import com.redhat.thermostat.server.core.internal.web.json.DocumentBuilder;
import com.redhat.thermostat.server.core.internal.web.request.TimedRequest;
import com.redhat.thermostat.server.core.internal.web.response.MongoResponseBuilder;

public class MongoBaseStorageHandler implements BaseStorageHandler {

    private final int MAX_MONGO_DOCUMENTS = 5000;

    @Override
    public void getAgent(final SecurityContext securityContext, final AsyncResponse asyncResponse, final String plugin, final String agentId, final String count, final String sort) {
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
                final String collectionName = plugin.isEmpty() ? "agent-config" : plugin;

                FindIterable<Document> documents = timedRequest.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
                    @Override
                    public FindIterable<Document> run() {
                        return ThermostatMongoStorage.getDatabase().getCollection(collectionName).find(filter).sort(new BasicDBObject("_id", sortOrder)).limit(limit);
                    }
                });

                asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponseWithTime(documents, timedRequest.getElapsed())).build());
            }
        }).start();
    }

    @Override
    public void putAgent(final String body, final SecurityContext context, final AsyncResponse asyncResponse, final String plugin) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ThermostatMongoStorage.isConnected()) {
                    asyncResponse.resume(Response.status(Response.Status.OK).entity("PUT " + context.getUserPrincipal().getName() + "\n\n" + body).build());
                    return;
                }

                TimedRequest<Boolean> timedRequest = new TimedRequest<>();

        /*
         * TODO: Verify body matches expected schema
         * TODO: Clean up insertion of tags into JSON body
         */
                final Document item = Document.parse(DocumentBuilder.addTags(body, context.getUserPrincipal().getName()));
                final String collectionName = plugin.isEmpty() ? "agent-config" : plugin;
                Boolean response = timedRequest.run(new TimedRequest.TimedRunnable<Boolean>() {
                    @Override
                    public Boolean run() {
                        try {
                            ThermostatMongoStorage.getDatabase().getCollection(collectionName).insertOne(item);
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
    public void getHostInfo(final SecurityContext securityContext, final AsyncResponse asyncResponse, final String plugin, final String agentId, final String count, final String sort, String maxTimestamp, String minTimestamp) {
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
                final String collectionName = plugin.isEmpty() ? "hostInfo" : plugin;

                FindIterable<Document> documents = timedRequest.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
                    @Override
                    public FindIterable<Document> run() {
                        return ThermostatMongoStorage.getDatabase().getCollection(collectionName).find(filter).sort(new BasicDBObject("_id", sortOrder)).limit(limit);
                    }
                });

                asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponseWithTime(documents, timedRequest.getElapsed())).build());
            }
        }).start();
    }

    @Override
    public void getVmInfo(final SecurityContext securityContext, final AsyncResponse asyncResponse, final String plugin, final String agentId, final String vmId, final String count, final String sort, final String maxTimestamp, final String minTimestamp) {
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
                final Bson filter = RequestFilters.buildGetFilter(agentId, vmId, Collections.singletonList(userName), minTimestamp, maxTimestamp);

                TimedRequest<FindIterable<Document>> timedRequest = new TimedRequest<>();
                final String collectionName = plugin.isEmpty() ? "vm-info" : plugin;

                FindIterable<Document> documents = timedRequest.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
                    @Override
                    public FindIterable<Document> run() {
                        return ThermostatMongoStorage.getDatabase().getCollection(collectionName).find(filter).sort(new BasicDBObject("_id", sortOrder)).limit(limit);
                    }
                });

                asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponseWithTime(documents, timedRequest.getElapsed())).build());
            }
        }).start();
    }

}
