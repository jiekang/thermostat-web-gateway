package com.redhat.thermostat.server.core.internal.web.handler.http;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.redhat.thermostat.server.core.internal.web.handler.storage.StorageHandler;

@Path("/api/v100/{namespace}")
public class NamespaceHttpHandler {
    private final StorageHandler handler;

    public NamespaceHttpHandler(StorageHandler handler) {
        this.handler = handler;
    }

    private static final String OFFSET = "0";
    private static final String LIMIT = "20";

    @GET
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void getSystems(@Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @QueryParam("offset") @DefaultValue(OFFSET) String offset,
                           @QueryParam("limit") @DefaultValue(LIMIT) String limit,
                           @QueryParam("sort") String sort) {
        handler.getSystems(context, asyncResponse, namespace, offset, limit, sort);
    }

    @PUT
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void putSystems(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace) {
        handler.putSystems(body, context, asyncResponse, namespace);
    }

    @POST
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void postSystems(String body,
                            @Context SecurityContext context,
                            @Suspended final AsyncResponse asyncResponse,
                            @PathParam("namespace") String namespace,
                            @QueryParam("offset") @DefaultValue(OFFSET) String offset,
                            @QueryParam("limit") @DefaultValue(LIMIT) String limit,
                            @QueryParam("sort") String sort) {
        handler.postSystems(body, context, asyncResponse, namespace, offset, limit, sort);
    }

    @DELETE
    @Path("systems")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSystems(String body,
                              @Context SecurityContext context,
                              @Suspended final AsyncResponse asyncResponse,
                              @PathParam("namespace") String namespace) {
        handler.deleteSystems(body, context, asyncResponse, namespace);
    }

    @GET
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getSystem(@Context SecurityContext securityContext,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @PathParam("systemId") String systemId) {
        handler.getSystem(securityContext, asyncResponse, namespace, systemId);
    }

    @PUT
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putSystem(String body,
                          @Context SecurityContext context,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace) {
        handler.putSystem(body, context, asyncResponse, namespace);
    }

    @DELETE
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSystem(@Context SecurityContext context,
                             @Suspended final AsyncResponse asyncResponse,
                             @PathParam("namespace") String namespace) {
        handler.deleteSystem(context, asyncResponse, namespace);
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
        handler.getAgents(securityContext, asyncResponse, namespace, systemId, offset, limit, sort);
    }

    @PUT
    @Path("systems/{systemId}/agents")
    @Produces(MediaType.APPLICATION_JSON)
    public void putAgents(String body,
                          @Context SecurityContext context,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @PathParam("systemId") String systemId) {
        handler.putAgents(body, context, asyncResponse, namespace, systemId);
    }

    @POST
    @Path("systems/{systemId}/agents")
    @Produces(MediaType.APPLICATION_JSON)
    public void postAgents(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @PathParam("systemId") String systemId,
                           @QueryParam("offset") @DefaultValue(OFFSET) String offset,
                           @QueryParam("limit") @DefaultValue(LIMIT) String limit,
                           @QueryParam("sort") String sort) {
        handler.postAgents(body, context, asyncResponse, namespace, systemId, offset, limit, sort);
    }

    @DELETE
    @Path("systems/{systemId}/agents")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAgents(String body,
                             @Context SecurityContext context,
                             @Suspended final AsyncResponse asyncResponse,
                             @PathParam("namespace") String namespace,
                             @PathParam("systemId") String systemId) {
        handler.deleteAgents(body, context, asyncResponse, namespace, systemId);
    }

    @GET
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAgent(@Context SecurityContext securityContext,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("namespace") String namespace,
                         @PathParam("systemId") String systemId,
                         @PathParam("agentId") String agentId) {
        handler.getAgent(securityContext, asyncResponse, namespace, systemId, agentId);
    }

    @PUT
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putAgent(String body,
                         @Context SecurityContext context,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("namespace") String namespace,
                         @PathParam("systemId") String systemId,
                         @PathParam("agentId") String agentId) {
        handler.putAgent(body, context, asyncResponse, namespace, systemId, agentId);
    }

    @DELETE
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAgent(String body,
                            @Context SecurityContext context,
                            @Suspended final AsyncResponse asyncResponse,
                            @PathParam("namespace") String namespace,
                            @PathParam("systemId") String systemId,
                            @PathParam("agentId") String agentId) {
        handler.deleteAgent(body, context, asyncResponse, namespace, systemId, agentId);
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
        handler.getJvms(securityContext, asyncResponse, namespace, systemId, agentId, vmId, offset, limit, sort);
    }

    @PUT
    @Path("systems/{systemId}/agents/{agentId}/jvms")
    @Produces(MediaType.APPLICATION_JSON)
    public void putJvms(String body,
                        @Context SecurityContext context,
                        @Suspended final AsyncResponse asyncResponse,
                        @PathParam("namespace") String namespace,
                        @PathParam("systemId") String systemId,
                        @PathParam("agentId") String agentId) {
        handler.putJvms(body, context, asyncResponse, namespace, systemId, agentId);
    }

    @POST
    @Path("systems/{systemId}/agents/{agentId}/jvms")
    @Produces(MediaType.APPLICATION_JSON)
    public void postJvms(String body,
                         @Context SecurityContext context,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("namespace") String namespace,
                         @PathParam("systemId") String systemId,
                         @PathParam("agentId") String agentId,
                         @QueryParam("offset") @DefaultValue(OFFSET) String offset,
                         @QueryParam("limit") @DefaultValue(LIMIT) String limit,
                         @QueryParam("sort") String sort) {
        handler.postJvms(body, context, asyncResponse, namespace, systemId, agentId, offset, limit, sort);
    }

    @DELETE
    @Path("systems/{systemId}/agents/{agentId}/jvms")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteJvms(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @PathParam("systemId") String systemId,
                           @PathParam("agentId") String agentId) {
        handler.deleteJvms(body, context, asyncResponse, namespace, systemId, agentId);
    }

    @GET
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getJvm(@Context SecurityContext securityContext,
                       @Suspended final AsyncResponse asyncResponse,
                       @PathParam("namespace") String namespace,
                       @PathParam("systemId") String systemId,
                       @PathParam("agentId") String agentId,
                       @PathParam("jvmId") String jvmId) {
        handler.getJvm(securityContext, asyncResponse, namespace, systemId, agentId, jvmId);
    }

    @PUT
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putJvm(String body,
                       @Context SecurityContext context,
                       @Suspended final AsyncResponse asyncResponse,
                       @PathParam("namespace") String namespace,
                       @PathParam("systemId") String systemId,
                       @PathParam("agentId") String agentId,
                       @PathParam("jvmId") String jvmId) {
        handler.putJvm(body, context, asyncResponse, namespace, systemId, agentId, jvmId);
    }


    @DELETE
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteJvm(String body,
                          @Context SecurityContext context,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @PathParam("systemId") String systemId,
                          @PathParam("agentId") String agentId,
                          @PathParam("jvmId") String jvmId) {
        handler.deleteJvm(body, context, asyncResponse, namespace, systemId, agentId, jvmId);
    }
}
