package com.redhat.thermostat.server.core.internal.web.handler.storage;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ChunkedOutput;

public class MongoPluginStorageHandler implements PluginStorageHandler {
    @Override
    public void getPath(SecurityContext securityContext, AsyncResponse asyncResponse, String path) {

    }

    @Override
    public void putPath(SecurityContext securityContext, AsyncResponse asyncResponse, String path, String body) {

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
