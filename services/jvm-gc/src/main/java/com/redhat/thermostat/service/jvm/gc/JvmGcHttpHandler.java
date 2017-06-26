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

package com.redhat.thermostat.service.jvm.gc;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.mongodb.DBObject;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoDataResultContainer;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoExecutor;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataGenerator;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.servlet.ServletContextConstants;

@Path("/")
public class JvmGcHttpHandler {
    private final MongoExecutor mongoExecutor = new MongoExecutor();
    private final String collectionName = "jvm-gc";

    @GET
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getJvmGc(@QueryParam("l") @DefaultValue("1") Integer limit,
                             @QueryParam("o") @DefaultValue("0") Integer offset,
                             @QueryParam("s") String sort,
                             @QueryParam("q") String queries,
                             @QueryParam("p") String projections,
                             @QueryParam("m") @DefaultValue("false") Boolean metadata,
                             @Context HttpServletRequest requestInfo,
                             @Context ServletContext context) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            MongoDataResultContainer execResult = mongoExecutor.execGetRequest(
                    storage.getDatabase().getCollection(collectionName), limit, offset, sort, queries, projections);

            MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();
            response.queryDocuments(execResult.getQueryDataResult());

            if (metadata) {
                MongoMetaDataResponseBuilder.MetaBuilder metaDataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();
                MongoMetaDataGenerator metaDataGenerator = new MongoMetaDataGenerator(limit, offset, sort, queries,
                        projections, requestInfo, execResult);

                metaDataGenerator.setDocAndPayloadCount(metaDataResponse);
                metaDataGenerator.setPrev(metaDataResponse);
                metaDataGenerator.setNext(metaDataResponse);

                response.metaData(metaDataResponse.build());
            }
            return Response.status(Response.Status.OK).entity(response.build()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getStackTrace()).build();
        }
    }

    @PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response putJvmGc(String body,
                             @QueryParam("q") String queries,
                             @QueryParam("m") @DefaultValue("false") String metadata,
                             @Context ServletContext context) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoExecutor.execPutRequest(storage.getDatabase().getCollection(collectionName), body, queries);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response postJvmGc(String body,
                              @QueryParam("m") @DefaultValue("false") String metadata,
                              @Context ServletContext context) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoExecutor.execPostRequest(storage.getDatabase().getCollection(collectionName, DBObject.class), body);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response deleteJvmGc(@QueryParam("q") String queries,
                                @QueryParam("m") @DefaultValue("false") String metadata,
                                @Context ServletContext context) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoExecutor.execDeleteRequest(storage.getDatabase().getCollection(collectionName), queries);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
