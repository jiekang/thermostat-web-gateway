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

package com.redhat.thermostat.service.cpuinfo.http;

import javax.servlet.ServletContext;
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


import com.mongodb.DBObject;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.servlet.ServletContextConstants;
import com.redhat.thermostat.service.cpuinfo.mongo.MongoStorageHandler;

@Path("/")
public class SystemInfoCPUHttpHandler {
    private final MongoStorageHandler mongoStorageHandler = new MongoStorageHandler();
    private final String collectionName = "cpu-info";

    @GET
    @Path("/systems/{" + Parameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getCPUInfo(@PathParam(Parameters.SYSTEM_ID) String systemId,
                                  @QueryParam(Parameters.LIMIT) @DefaultValue("1") Integer limit,
                                  @QueryParam(Parameters.OFFSET) @DefaultValue("0") Integer offset,
                                  @QueryParam(Parameters.SORT) String sort,
                                  @QueryParam(Parameters.INCLUDE) String includes,
                                  @QueryParam(Parameters.EXCLUDE) String excludes,
                                  @Context ServletContext context
    ) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            String message = mongoStorageHandler.getOne(storage.getDatabase().getCollection(collectionName), systemId, limit, offset, sort, includes, excludes);
            return Response.status(Response.Status.OK).entity(message).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("/systems/{" + Parameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response putCPUInfo(String body,
                                  @PathParam(Parameters.SYSTEM_ID) String systemId,
                                  @QueryParam(Parameters.QUERY) String queries,
                                  @Context ServletContext context) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoStorageHandler.updateOne(storage.getDatabase().getCollection(collectionName), body, systemId, queries);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Path("/systems/{" + Parameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response postCPUInfo(String body,
                                   @PathParam(Parameters.SYSTEM_ID) String systemId,
                                   @Context ServletContext context) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoStorageHandler.addMany(storage.getDatabase().getCollection(collectionName, DBObject.class), body, systemId);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/systems/{" + Parameters.SYSTEM_ID +"}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response deleteCPUInfo(@PathParam(Parameters.SYSTEM_ID) String systemId,
                                     @QueryParam(Parameters.QUERY) String queries,
                                     @Context ServletContext context) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoStorageHandler.delete(storage.getDatabase().getCollection(collectionName), systemId);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
