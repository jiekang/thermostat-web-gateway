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

package com.redhat.thermostat.gateway.service.systems.http;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.redhat.thermostat.gateway.common.mongodb.servlet.RequestParameters;
import com.redhat.thermostat.gateway.common.mongodb.servlet.MongoHttpHandlerHelper;

@Path("/")
public class SystemsHttpHandler {
    private final String collectionName = "system-info";
    private final MongoHttpHandlerHelper serviceHelper = new MongoHttpHandlerHelper( collectionName );

    @GET
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getSystemInfoAll(@QueryParam(RequestParameters.LIMIT) @DefaultValue("1") Integer limit,
                                     @QueryParam(RequestParameters.OFFSET) @DefaultValue("0") Integer offset,
                                     @QueryParam(RequestParameters.SORT) String sort,
                                     @QueryParam(RequestParameters.QUERY) String queries,
                                     @QueryParam(RequestParameters.INCLUDE) String includes,
                                     @QueryParam(RequestParameters.EXCLUDE) String excludes,
                                     @QueryParam(RequestParameters.METADATA) @DefaultValue("false") String metadata,
                                     @Context ServletContext context,
                                     @Context HttpServletRequest httpServletRequest) {
        return serviceHelper.handleGet(httpServletRequest, context, limit, offset, sort, queries, includes, excludes, metadata);
    }

    @GET
    @Path("/systems/{" + RequestParameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getSystemInfo(@PathParam(RequestParameters.SYSTEM_ID) String systemId,
                                  @QueryParam(RequestParameters.LIMIT) @DefaultValue("1") Integer limit,
                                  @QueryParam(RequestParameters.OFFSET) @DefaultValue("0") Integer offset,
                                  @QueryParam(RequestParameters.SORT) String sort,
                                  @QueryParam(RequestParameters.QUERY) String queries,
                                  @QueryParam(RequestParameters.INCLUDE) String includes,
                                  @QueryParam(RequestParameters.EXCLUDE) String excludes,
                                  @QueryParam(RequestParameters.METADATA) @DefaultValue("false") String metadata,
                                  @Context ServletContext context,
                                  @Context HttpServletRequest httpServletRequest) {
        return serviceHelper.handleGetWithSystemID(httpServletRequest, context, systemId, limit, offset, sort, queries, includes, excludes, metadata);
    }

    @PUT
    @Path("/systems/{" + RequestParameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response putSystemInfo(String body,
                                  @PathParam(RequestParameters.SYSTEM_ID) String systemId,
                                  @QueryParam(RequestParameters.QUERY) String queries,
                                  @QueryParam(RequestParameters.METADATA) @DefaultValue("false") String metadata,
                                  @Context ServletContext context,
                                  @Context HttpServletRequest httpServletRequest) {
        return serviceHelper.handlePutWithSystemId(httpServletRequest, context, systemId, queries, metadata, body);
    }

    @POST
    @Path("/systems/{" + RequestParameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response postSystemInfo(String body,
                                   @PathParam(RequestParameters.SYSTEM_ID) String systemId,
                                   @QueryParam(RequestParameters.METADATA) @DefaultValue("false") String metadata,
                                   @Context ServletContext context,
                                   @Context HttpServletRequest httpServletRequest) {
        return serviceHelper.handlePostWithSystemID(httpServletRequest, context, systemId, metadata, body);
    }

    @DELETE
    @Path("/systems/{" + RequestParameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response deleteSystemInfo(@PathParam(RequestParameters.SYSTEM_ID) String systemId,
                                     @QueryParam(RequestParameters.QUERY) String queries,
                                     @QueryParam(RequestParameters.METADATA) @DefaultValue("false") String metadata,
                                     @Context ServletContext context,
                                     @Context HttpServletRequest httpServletRequest) {
        return serviceHelper.handleDeleteWithSystemID(httpServletRequest, context, systemId, queries, metadata);
    }
}
