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

package com.redhat.thermostat.server.core.internal.web.http;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Path("/api/v100/{namespace}")
public class NamespaceHttpHandler {
    private final StorageHandler handler;

    public NamespaceHttpHandler(StorageHandler handler) {
        this.handler = handler;
    }

    private static final String OFFSET = "0";
    private static final String LIMIT = "50";

    @GET
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getSystems(@Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @PathParam("systemId") String systemId,
                           @QueryParam("o") @DefaultValue(OFFSET) String offset,
                           @QueryParam("l") @DefaultValue(LIMIT) String limit,
                           @QueryParam("s") String sort,
                           @QueryParam("q") String queries,
                           @QueryParam("p") String projections) {
        handler.getSystems(context, asyncResponse, namespace, systemId, offset, limit, sort, queries, projections);
    }

    @PUT
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putSystems(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @QueryParam("q") String queries,
                           @PathParam("systemId") String systemId) {
        handler.putSystems(body, context, asyncResponse, namespace, queries, systemId);
    }

    @POST
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void postSystems(String body,
                            @Context SecurityContext context,
                            @Suspended final AsyncResponse asyncResponse,
                            @PathParam("namespace") String namespace,
                            @PathParam("systemId") String systemId) {
        handler.postSystems(body, context, asyncResponse, namespace, systemId);
    }

    @DELETE
    @Path("systems/{systemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteSystems(String body,
                              @Context SecurityContext context,
                              @Suspended final AsyncResponse asyncResponse,
                              @PathParam("namespace") String namespace,
                              @PathParam("systemId") String systemId,
                              @QueryParam("q") String queries) {
        handler.deleteSystems(body, context, asyncResponse, namespace, systemId, queries);
    }

    @GET
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAgents(@Context SecurityContext securityContext,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @PathParam("systemId") String systemId,
                          @PathParam("agentId") String agentId,
                          @QueryParam("o") @DefaultValue(OFFSET) String offset,
                          @QueryParam("l") @DefaultValue(LIMIT) String limit,
                          @QueryParam("s") String sort,
                          @QueryParam("q") String queries,
                          @QueryParam("p") String projections) {
        handler.getAgents(securityContext, asyncResponse, namespace, systemId, agentId, offset, limit, sort, queries, projections);
    }

    @PUT
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putAgents(String body,
                          @Context SecurityContext context,
                          @Suspended final AsyncResponse asyncResponse,
                          @PathParam("namespace") String namespace,
                          @QueryParam("q") String queries,
                          @PathParam("systemId") String systemId,
                          @PathParam("agentId") String agentId) {
        handler.putAgents(body, context, asyncResponse, namespace, queries, systemId, agentId);
    }

    @POST
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void postAgents(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @PathParam("systemId") String systemId,
                           @PathParam("agentId") String agentId) {
        handler.postAgents(body, context, asyncResponse, namespace, systemId, agentId);
    }

    @DELETE
    @Path("systems/{systemId}/agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteAgents(String body,
                             @Context SecurityContext context,
                             @Suspended final AsyncResponse asyncResponse,
                             @PathParam("namespace") String namespace,
                             @PathParam("systemId") String systemId,
                             @PathParam("agentId") String agentId,
                             @QueryParam("q") String queries) {
        handler.deleteAgents(body, context, asyncResponse, namespace, systemId, agentId, queries);
    }

    @GET
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getJvms(@Context SecurityContext securityContext,
                        @Suspended final AsyncResponse asyncResponse,
                        @PathParam("namespace") String namespace,
                        @PathParam("systemId") String systemId,
                        @PathParam("agentId") String agentId,
                        @PathParam("jvmId") String jvmId,
                        @QueryParam("o") @DefaultValue(OFFSET) String offset,
                        @QueryParam("l") @DefaultValue(LIMIT) String limit,
                        @QueryParam("s") String sort,
                        @QueryParam("q") String queries,
                        @QueryParam("p") String projections) {
        handler.getJvms(securityContext, asyncResponse, namespace, systemId, agentId, jvmId, offset, limit, sort, queries, projections);
    }

    @PUT
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void putJvms(String body,
                        @Context SecurityContext context,
                        @Suspended final AsyncResponse asyncResponse,
                        @PathParam("namespace") String namespace,
                        @QueryParam("q") String queries,
                        @PathParam("systemId") String systemId,
                        @PathParam("agentId") String agentId,
                        @PathParam("jvmId") String jvmId) {
        handler.putJvms(body, context, asyncResponse, namespace, queries, systemId, agentId, jvmId);
    }

    @POST
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void postJvms(String body,
                         @Context SecurityContext context,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("namespace") String namespace,
                         @PathParam("systemId") String systemId,
                         @PathParam("agentId") String agentId,
                         @PathParam("jvmId") String jvmId) {
        handler.postJvms(body, context, asyncResponse, namespace, systemId, agentId, jvmId);
    }

    @DELETE
    @Path("systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteJvms(String body,
                           @Context SecurityContext context,
                           @Suspended final AsyncResponse asyncResponse,
                           @PathParam("namespace") String namespace,
                           @PathParam("systemId") String systemId,
                           @PathParam("agentId") String agentId,
                           @PathParam("jvmId") String jvmId,
                           @QueryParam("q") String queries) {
        handler.deleteJvms(body, context, asyncResponse, namespace, systemId, agentId, jvmId, queries);
    }

}
