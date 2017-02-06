package com.redhat.thermostat.server.core.internal.web.handler.http;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ChunkedOutput;

import com.redhat.thermostat.server.core.internal.web.handler.storage.PluginStorageHandler;

@Path("/plugin")
@RolesAllowed("user")
public class PluginHttpHandler {
    private final PluginStorageHandler handler;

    public PluginHttpHandler(PluginStorageHandler handler) {
        this.handler = handler;
    }

    /**
     * Get information about the path.
     */
    @GET
    @Path("/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPath(@Context SecurityContext securityContext,
                        @Suspended final AsyncResponse asyncResponse,
                        @PathParam("path") String path) {
        handler.getPath(securityContext, asyncResponse, path);
    }

    /**
     * Add one or more items to the path
     */
    @PUT
    @Path("/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putPath(@Context SecurityContext securityContext,
                        @Suspended final AsyncResponse asyncResponse,
                        @PathParam("path") String path,
                        String body) {
        handler.putPath(securityContext, asyncResponse, path, body);
    }

    /**
     * Query the path for items
     */
    @POST
    @Path("/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public void postPath(@Context SecurityContext securityContext,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("path") String path,
                         String body) {
        handler.postPath(securityContext, asyncResponse, path, body);
    }

    /**
     * Delete the entire path
     */
    @DELETE
    @Path("/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deletePath(@Context SecurityContext securityContext,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("path") String path) {
        handler.deletePath(securityContext, asyncResponse, path);
    }

    /**
     * Stream items from the path
     */
    @GET
    @Path("stream/{path}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChunkedOutput<String> streamPath(@Context SecurityContext securityContext,
                                    @Suspended final AsyncResponse asyncResponse,
                                    @PathParam("path") String path) {
        return handler.streamPath(securityContext, asyncResponse, path);
    }

    /**
     * Get the item
     */
    @GET
    @Path("/{path}/{item}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPathItem(@Context SecurityContext securityContext,
                            @Suspended final AsyncResponse asyncResponse,
                            @PathParam("path") String path,
                            @PathParam("item") String item) {
        handler.getPathItem(securityContext, asyncResponse, path, item);
    }

    /**
     * Create or replace the item
     */
    @PUT
    @Path("/{path}/{item}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putPathItem(@Context SecurityContext securityContext,
                            @Suspended final AsyncResponse asyncResponse,
                            @PathParam("path") String path,
                            @PathParam("item") String item,
                            String body) {
        handler.putPathItem(securityContext, asyncResponse, path, item, body);
    }

    /**
     * Query the item
     */
    @POST
    @Path("/{path}/{item}")
    @Produces(MediaType.APPLICATION_JSON)
    public void postPathItem(@Context SecurityContext securityContext,
                             @Suspended final AsyncResponse asyncResponse,
                             @PathParam("path") String path,
                             @PathParam("item") String item,
                             String body) {
        handler.postPathItem(securityContext, asyncResponse, path, item, body);
    }

    /**
     * Delete the item
     */
    @DELETE
    @Path("/{path}/{item}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deletePathItem(@Context SecurityContext securityContext,
                               @Suspended final AsyncResponse asyncResponse,
                               @PathParam("path") String path,
                               @PathParam("item") String item) {
        handler.deletePathItem(securityContext, asyncResponse, path, item);
    }
}
