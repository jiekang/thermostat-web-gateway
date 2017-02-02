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

package com.redhat.thermostat.server.core.internal.web.handler.http;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ChunkedOutput;

import com.redhat.thermostat.server.core.internal.web.handler.storage.StorageHandler;

@Path("/api")
@RolesAllowed("user")
public class HttpHandler {

    private final StorageHandler handler;

    public HttpHandler(StorageHandler handler) {
        this.handler = handler;
    }

    @GET
    @Path("agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getAgent(@Context SecurityContext securityContext,
                         @Suspended final AsyncResponse asyncResponse,
                         @PathParam("agentId") String agentId,
                         @QueryParam("size") @DefaultValue("1") String count,
                         @QueryParam("sort") @DefaultValue("-1") String sort) {
        handler.getAgent(securityContext, asyncResponse, agentId, count, sort);
    }

    @PUT
    @Path("agents/{agentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response putAgent(String body,
                             @Context SecurityContext context) {
        return handler.putAgent(body, context);
    }

    @GET
    @Path("agents/{agentId}/host/cpu")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHostCpuInfo(@Context SecurityContext securityContext,
                                   @PathParam("agentId") String agentId,
                                   @QueryParam("size") @DefaultValue("1") String count,
                                   @QueryParam("sort") @DefaultValue("-1") String sort,
                                   @QueryParam("maxTimestamp") String maxTimestamp,
                                   @QueryParam("minTimestamp") String minTimestamp) {
        return handler.getHostCpuInfo(securityContext, agentId, count, sort, maxTimestamp, minTimestamp);
    }

    @GET
    @Path("stream/agents/{agentId}/host/cpu")
    @Produces(MediaType.APPLICATION_JSON)
    public ChunkedOutput<String> streamHostCpuInfo(@Context SecurityContext securityContext,
                                                   @PathParam("agentId") String agentId) {
        return handler.streamHostCpuInfo(securityContext, agentId);
    }
}