package com.redhat.thermostat.server.core.internal.web.handler.storage;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.bson.Document;
import org.glassfish.jersey.server.ChunkedOutput;

import com.mongodb.client.FindIterable;
import com.redhat.thermostat.server.core.internal.storage.ThermostatMongoStorage;
import com.redhat.thermostat.server.core.internal.web.request.TimedRequest;
import com.redhat.thermostat.server.core.internal.web.response.MongoResponseBuilder;

public class MongoPathItemStorageHandler implements PathItemStorageHandler {
    @Override
    public void getPath(SecurityContext securityContext, final AsyncResponse asyncResponse, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ThermostatMongoStorage.isConnected()) {
                    asyncResponse.resume(Response.status(Response.Status.OK).entity("GET " + path).build());
                    return;
                }
                TimedRequest<FindIterable<Document>> timedRequest = new TimedRequest<>();
                FindIterable<Document> documents = timedRequest.run(new TimedRequest.TimedRunnable<FindIterable<Document>>() {
                    @Override
                    public FindIterable<Document> run() {
                        return ThermostatMongoStorage.getDatabase().getCollection(path).find();
                    }
                });
                asyncResponse.resume(Response.status(Response.Status.OK).entity(MongoResponseBuilder.buildJsonResponse(documents)).build());
            }
        }).start();
    }

    @Override
    public void putPath(SecurityContext securityContext, final AsyncResponse asyncResponse, final String path, String body) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!ThermostatMongoStorage.isConnected()) {
                    asyncResponse.resume(Response.status(Response.Status.OK).entity("PUT " + path).build());
                    return;
                }
                TimedRequest<Boolean> timedRequest = new TimedRequest<>();

                asyncResponse.resume(Response.status(Response.Status.OK).entity("PUT " + path).build());
            }
        }).start();
    }

    @Override
    public void postPath(SecurityContext securityContext, AsyncResponse asyncResponse, String path, String body) {

    }

    @Override
    public void deletePath(SecurityContext securityContext, AsyncResponse asyncResponse, String path) {

    }

    @Override
    public ChunkedOutput<String> streamPath(SecurityContext securityContext, AsyncResponse asyncResponse, String path) {
        return null;
    }

    @Override
    public void getPathItem(SecurityContext securityContext, AsyncResponse asyncResponse, String path, String item) {

    }

    @Override
    public void putPathItem(SecurityContext securityContext, AsyncResponse asyncResponse, String path, String item, String body) {

    }

    @Override
    public void postPathItem(SecurityContext securityContext, AsyncResponse asyncResponse, String path, String item, String body) {

    }

    @Override
    public void deletePathItem(SecurityContext securityContext, AsyncResponse asyncResponse, String path, String item) {

    }
}
