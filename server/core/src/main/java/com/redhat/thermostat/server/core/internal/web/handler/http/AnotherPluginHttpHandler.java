package com.redhat.thermostat.server.core.internal.web.handler.http;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.redhat.thermostat.server.core.internal.web.handler.storage.BaseStorageHandler;

@Path("/plugins/{plugin}")
public class AnotherPluginHttpHandler {
    private final BaseStorageHandler handler;

    public AnotherPluginHttpHandler(BaseStorageHandler handler) {
        this.handler = handler;
    }

    @GET
    @Path("agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAgent(@Context SecurityContext securityContext,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("plugin") String plugin,
                         @PathParam("agentId") String agentId,
                         @QueryParam("size") @DefaultValue("1") String count,
                         @QueryParam("sort") @DefaultValue("-1") String sort) {
        handler.getAgent(securityContext, asyncResponse, plugin, agentId, count, sort);
    }

    @PUT
    @Path("agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putAgent(String body,
                             @Context SecurityContext context,
                             @Suspended final AsyncResponse asyncResponse,
                             @PathParam("plugin") String plugin) {
        handler.putAgent(body, context, asyncResponse, plugin);
    }

    @GET
    @Path("agents/{agentId}/host/")
    @Produces(MediaType.APPLICATION_JSON)
    public void getHostInfo(@Context SecurityContext securityContext,
                                @Suspended final AsyncResponse asyncResponse,
                                @PathParam("plugin") String plugin,
                                @PathParam("agentId") String agentId,
                                @QueryParam("size") @DefaultValue("1") String count,
                                @QueryParam("sort") @DefaultValue("-1") String sort,
                                @QueryParam("maxTimestamp") String maxTimestamp,
                                @QueryParam("minTimestamp") String minTimestamp) {
        handler.getHostInfo(securityContext, asyncResponse, plugin, agentId, count, sort, maxTimestamp, minTimestamp);
    }

    @GET
    @Path("agents/{agentId}/vms/{vmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getVmInfo(@Context SecurityContext securityContext,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("plugin") String plugin,
                          @PathParam("agentId") String agentId,
                          @PathParam("vmId") String vmId,
                          @QueryParam("size") @DefaultValue("1") String count,
                          @QueryParam("sort") @DefaultValue("-1") String sort,
                          @QueryParam("maxTimestamp") String maxTimestamp,
                          @QueryParam("minTimestamp") String minTimestamp) {
        handler.getVmInfo(securityContext, asyncResponse, plugin, agentId, vmId, count, sort, maxTimestamp, minTimestamp);
    }
}
