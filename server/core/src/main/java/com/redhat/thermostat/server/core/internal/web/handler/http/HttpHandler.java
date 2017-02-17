package com.redhat.thermostat.server.core.internal.web.handler.http;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.redhat.thermostat.server.core.internal.web.handler.storage.StorageHandler;

@Path("/api/v1/{namespace}")
public class HttpHandler {
    private final StorageHandler handler;

    public HttpHandler(StorageHandler handler) {
        this.handler = handler;
    }

    private static final String OFFSET = "0";
    private static final String LIMIT = "20";

    @GET
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void getSystems(@Context SecurityContext securityContext,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @QueryParam("offset") @DefaultValue(OFFSET) String offset,
                           @QueryParam("limit") @DefaultValue(LIMIT) String limit,
                           @QueryParam("sort") String sort) {
    }
    @PUT
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void putSystems(String body,
                          @Context SecurityContext context,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace) {
    }

    @POST
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void postSystems(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace) {
    }

    @DELETE
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSystems(String body,
                              @Context SecurityContext context,
                              @Suspended final AsyncResponse asyncResponse,
                              @PathParam("namespace") String namespace) {
    }

    @GET
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getSystem(@Context SecurityContext securityContext,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @PathParam("systemId") String systemId) {
    }

    @PUT
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putSystem(String body,
                          @Context SecurityContext context,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace) {
    }

    @POST
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void postSystem(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace) {
    }

    @DELETE
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSystem(@Context SecurityContext context,
                             @Suspended final AsyncResponse asyncResponse,
                             @PathParam("namespace") String namespace) {
    }

    @GET
    @Path("systems/{systemId}/agents")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAgents(@Context SecurityContext securityContext,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @PathParam("systemId") String systemId,
                          @QueryParam("offset") @DefaultValue(OFFSET) String offset,
                          @QueryParam("limit") @DefaultValue(LIMIT) String limit,
                          @QueryParam("sort") String sort) {
    }

    @GET
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAgent(@Context SecurityContext securityContext,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @PathParam("systemId") String systemId,
                          @PathParam("agentId") String agentId) {
    }

    @GET
    @Path("systems/{systemId}/agents/{agentId}/jvms")
    @Produces(MediaType.APPLICATION_JSON)
    public void getJvms(@Context SecurityContext securityContext,
                        @Suspended final AsyncResponse asyncResponse,
                        @PathParam("namespace") String namespace,
                        @PathParam("systemId") String systemId,
                        @PathParam("agentId") String agentId,
                        @PathParam("vmId") String vmId,
                        @QueryParam("offset") @DefaultValue(OFFSET) String offset,
                        @QueryParam("limit") @DefaultValue(LIMIT) String limit,
                        @QueryParam("sort") String sort) {
    }
    @GET
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getJvm(@Context SecurityContext securityContext,
                        @Suspended final AsyncResponse asyncResponse,
                        @PathParam("namespace") String namespace,
                        @PathParam("systemId") String systemId,
                        @PathParam("agentId") String agentId,
                        @PathParam("vmId") String vmId) {
    }
}
