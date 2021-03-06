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

package com.redhat.thermostat.gateway.service.jvm.compiler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.redhat.thermostat.gateway.common.core.model.LimitParameter;
import com.redhat.thermostat.gateway.common.core.model.OffsetParameter;
import com.redhat.thermostat.gateway.common.core.servlet.CommonQueryParams;
import com.redhat.thermostat.gateway.common.core.servlet.RequestParameters;
import com.redhat.thermostat.gateway.common.mongodb.servlet.MongoHttpHandlerHelper;

@Path("/")
public class JvmCompilerHttpHandler {
    private static final String collectionName = "jvm-compiler";

    private final MongoHttpHandlerHelper serviceHelper = new MongoHttpHandlerHelper(collectionName);

    @GET
    @Path("/jvms/{" + RequestParameters.JVM_ID + "}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getJvmCompiler(@PathParam(RequestParameters.JVM_ID) String jvmId,
                              @QueryParam(RequestParameters.LIMIT) @DefaultValue("1") LimitParameter limit,
                              @QueryParam(RequestParameters.OFFSET) @DefaultValue("0") OffsetParameter offset,
                              @QueryParam(RequestParameters.SORT) String sort,
                              @QueryParam(RequestParameters.QUERY) String queries,
                              @QueryParam(RequestParameters.INCLUDE) String includes,
                              @QueryParam(RequestParameters.EXCLUDE) String excludes,
                              @QueryParam(RequestParameters.METADATA) @DefaultValue("false") Boolean metadata,
                              @Context HttpServletRequest httpServletRequest,
                              @Context ServletContext context) {
        return serviceHelper.handleGetWithJvmID(httpServletRequest, context, null, jvmId,  new CommonQueryParams(limit, offset, sort, queries, includes, excludes, metadata));
    }

    @GET
    @Path("/systems/{" + RequestParameters.SYSTEM_ID + "}/jvms/{" + RequestParameters.JVM_ID + "}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getJvmCompiler(@PathParam(RequestParameters.SYSTEM_ID) String systemId,
                              @PathParam(RequestParameters.JVM_ID) String jvmId,
                              @QueryParam(RequestParameters.LIMIT) @DefaultValue("1") LimitParameter limit,
                              @QueryParam(RequestParameters.OFFSET) @DefaultValue("0") OffsetParameter offset,
                              @QueryParam(RequestParameters.SORT) String sort,
                              @QueryParam(RequestParameters.QUERY) String queries,
                              @QueryParam(RequestParameters.INCLUDE) String includes,
                              @QueryParam(RequestParameters.EXCLUDE) String excludes,
                              @QueryParam(RequestParameters.METADATA) @DefaultValue("false") Boolean metadata,
                              @Context HttpServletRequest httpServletRequest,
                              @Context ServletContext context) {
        return serviceHelper.handleGetWithJvmID(httpServletRequest, context, systemId, jvmId,  new CommonQueryParams(limit, offset, sort, queries, includes, excludes, metadata));
    }

    @POST
    @Path("/systems/{" + RequestParameters.SYSTEM_ID + "}/jvms/{" + RequestParameters.JVM_ID + "}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response postJvmCompiler(String body,
                               @PathParam(RequestParameters.SYSTEM_ID) String systemId,
                               @PathParam(RequestParameters.JVM_ID) String jvmId,
                               @QueryParam(RequestParameters.METADATA) @DefaultValue("false") Boolean metadata,
                               @Context ServletContext context,
                               @Context HttpServletRequest httpServletRequest) {
        return serviceHelper.handlePostWithJvmID(httpServletRequest, context, systemId, jvmId, metadata, body);
    }

    @DELETE
    @Path("/systems/{" + RequestParameters.SYSTEM_ID + "}/jvms/{" + RequestParameters.JVM_ID + "}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response deleteJvmCompiler(@PathParam(RequestParameters.SYSTEM_ID) String systemId,
                                 @PathParam(RequestParameters.JVM_ID) String jvmId,
                                 @QueryParam(RequestParameters.QUERY) String queries,
                                 @QueryParam(RequestParameters.METADATA) @DefaultValue("false") Boolean metadata,
                                 @Context ServletContext context,
                                 @Context HttpServletRequest httpServletRequest) {
        return serviceHelper.handleDeleteWithJvmID(httpServletRequest, context, systemId, jvmId, queries, metadata);
    }
}
