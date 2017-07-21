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

import static com.redhat.thermostat.gateway.common.util.ServiceException.CANNOT_QUERY_REALMS_PROPERTY;
import static com.redhat.thermostat.gateway.common.util.ServiceException.DATABASE_UNAVAILABLE;
import static com.redhat.thermostat.gateway.common.util.ServiceException.EXPECTED_JSON_ARRAY;
import static com.redhat.thermostat.gateway.common.util.ServiceException.MALFORMED_CLIENT_REQUEST;

import java.io.IOException;

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
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import com.redhat.thermostat.gateway.common.core.auth.keycloak.RealmAuthorizer;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoDataResultContainer;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoExecutor;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataGenerator;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.servlet.ServletContextConstants;
import com.redhat.thermostat.gateway.common.util.HttpResponseExceptionHandler;
import org.bson.json.JsonParseException;

@Path("/")
public class JvmGcHttpHandler {
    private final MongoExecutor mongoExecutor = new MongoExecutor();
    private final String collectionName = "jvm-gc";
    private final HttpResponseExceptionHandler exceptionHandler = new HttpResponseExceptionHandler();

    public JvmGcHttpHandler() {
        exceptionHandler.add(MongoWriteException.class, MALFORMED_CLIENT_REQUEST)
                        .add(JsonParseException.class, MALFORMED_CLIENT_REQUEST)
                        .add(UnsupportedOperationException.class, MALFORMED_CLIENT_REQUEST)
                        .add(ClassCastException.class, EXPECTED_JSON_ARRAY)
                        .add(MongoTimeoutException.class, DATABASE_UNAVAILABLE)
                        .add(IOException.class, CANNOT_QUERY_REALMS_PROPERTY);
    }

    @GET
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getJvmGc(@QueryParam("l") @DefaultValue("1") Integer limit,
                             @QueryParam("o") @DefaultValue("0") Integer offset,
                             @QueryParam("s") String sort,
                             @QueryParam("q") String queries,
                             @QueryParam("p") String projections,
                             @QueryParam("m") @DefaultValue("false") Boolean metadata,
                             @Context HttpServletRequest httpServletRequest,
                             @Context ServletContext context) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(RealmAuthorizer.class.getName());
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

            MongoDataResultContainer execResult;

            if (realmAuthorizer != null) {
                if (realmAuthorizer.readable()) {
                    execResult = mongoExecutor.execGetRequest(
                            storage.getDatabase().getCollection(collectionName), limit, offset, sort, queries, projections, realmAuthorizer.getReadableRealms());
                } else {
                    return Response.status(Response.Status.FORBIDDEN).build();
                }
            } else {
                execResult = mongoExecutor.execGetRequest(
                        storage.getDatabase().getCollection(collectionName), limit, offset, sort, queries, projections, null);
            }

            MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();
            response.queryDocuments(execResult.getQueryDataResult());

            if (metadata) {
                MongoMetaDataResponseBuilder.MetaBuilder metaDataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();
                MongoMetaDataGenerator metaDataGenerator = new MongoMetaDataGenerator(limit, offset, sort, queries,
                        projections, httpServletRequest, execResult);

                metaDataGenerator.setDocAndPayloadCount(metaDataResponse);
                metaDataGenerator.setPrev(metaDataResponse);
                metaDataGenerator.setNext(metaDataResponse);

                response.metaData(metaDataResponse.build());
            }
            return Response.status(Response.Status.OK).entity(response.build()).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    @PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response putJvmGc(String body,
                             @QueryParam("q") String queries,
                             @QueryParam("m") @DefaultValue("false") String metadata,
                             @Context ServletContext context,
                             @Context HttpServletRequest httpServletRequest) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(RealmAuthorizer.class.getName());
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

            if (realmAuthorizer != null) {
                if (realmAuthorizer.updatable()) {
                    mongoExecutor.execPutRequest(storage.getDatabase().getCollection(collectionName), body, queries, realmAuthorizer.getUpdatableRealms());
                } else {
                    return Response.status(Response.Status.FORBIDDEN).build();
                }
            } else {
                mongoExecutor.execPutRequest(storage.getDatabase().getCollection(collectionName), body, queries, null);
            }

            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response postJvmGc(String body,
                              @QueryParam("m") @DefaultValue("false") String metadata,
                              @Context ServletContext context,
                              @Context HttpServletRequest httpServletRequest) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(RealmAuthorizer.class.getName());
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

            if (realmAuthorizer != null) {
                if (realmAuthorizer.writable()) {
                    mongoExecutor.execPostRequest(storage.getDatabase().getCollection(collectionName, DBObject.class), body, realmAuthorizer.getWritableRealms());
                } else {
                    return Response.status(Response.Status.FORBIDDEN).build();
                }
            } else {
                mongoExecutor.execPostRequest(storage.getDatabase().getCollection(collectionName, DBObject.class), body, null);
            }
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    @DELETE
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response deleteJvmGc(@QueryParam("q") String queries,
                                @QueryParam("m") @DefaultValue("false") String metadata,
                                @Context ServletContext context,
                                @Context HttpServletRequest httpServletRequest) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(RealmAuthorizer.class.getName());
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

            if (realmAuthorizer != null) {
                if (realmAuthorizer.deletable()) {
                    mongoExecutor.execDeleteRequest(storage.getDatabase().getCollection(collectionName), queries, realmAuthorizer.getDeletableRealms());
                } else {
                    return Response.status(Response.Status.FORBIDDEN).build();
                }
            } else {
                mongoExecutor.execDeleteRequest(storage.getDatabase().getCollection(collectionName), queries, null);
            }

            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }
}
