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

package com.redhat.thermostat.gateway.common.mongodb.servlet;

import com.mongodb.DBObject;
import com.mongodb.MongoTimeoutException;
import com.mongodb.MongoWriteException;
import com.redhat.thermostat.gateway.common.core.auth.keycloak.RealmAuthorizer;
import com.redhat.thermostat.gateway.common.mongodb.MongoStorageHandler;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatFields;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoDataResultContainer;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoExecutor;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoQuery;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataGenerator;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;
import com.redhat.thermostat.gateway.common.util.HttpResponseExceptionHandler;
import org.bson.json.JsonParseException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static com.redhat.thermostat.gateway.common.util.ServiceException.CANNOT_QUERY_REALMS_PROPERTY;
import static com.redhat.thermostat.gateway.common.util.ServiceException.DATABASE_UNAVAILABLE;
import static com.redhat.thermostat.gateway.common.util.ServiceException.EXPECTED_JSON_ARRAY;
import static com.redhat.thermostat.gateway.common.util.ServiceException.MALFORMED_CLIENT_REQUEST;

public class MongoHttpHandlerHelper {

    private final String collectionName;
    private final MongoExecutor mongoExecutor = new MongoExecutor();
    private final MongoStorageHandler mongoStorageHandler = new MongoStorageHandler();
    private final HttpResponseExceptionHandler exceptionHandler = new HttpResponseExceptionHandler();

    public MongoHttpHandlerHelper(String collectionName) {
        this.collectionName = collectionName;
        exceptionHandler.add(MongoWriteException.class, MALFORMED_CLIENT_REQUEST)
                .add(JsonParseException.class, MALFORMED_CLIENT_REQUEST)
                .add(UnsupportedOperationException.class, MALFORMED_CLIENT_REQUEST)
                .add(ClassCastException.class, EXPECTED_JSON_ARRAY)
                .add(MongoTimeoutException.class, DATABASE_UNAVAILABLE)
                .add(IOException.class, CANNOT_QUERY_REALMS_PROPERTY);
    }

    /*
     *  HTTP GET handling
     */

    public Response handleGetWithJvmID(HttpServletRequest httpServletRequest, ServletContext context, String systemId, String jvmId, Integer limit, Integer offset, String sort, String queries, String includes, String excludes, String returnMetadata) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            final MongoQuery querySystemId = new MongoQuery(MongoRequestFilters.buildEq(ThermostatFields.SYSTEM_ID, systemId));
            final MongoQuery queryJvmId = new MongoQuery(MongoRequestFilters.buildEq(ThermostatFields.JVM_ID, jvmId));
            String message = mongoStorageHandler.getMany(storage.getDatabase().getCollection(collectionName),
                    MongoRequestFilters.buildAnd(querySystemId, queryJvmId), 1, 0, null, includes, excludes);
            return Response.status(Response.Status.OK).entity(message).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    public Response handleGetWithSystemID(HttpServletRequest httpServletRequest, ServletContext context, String systemId, Integer limit, Integer offset, String sort, String queries, String includes, String excludes, String returnMetadata) {
        try {
            final ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            final MongoQuery systemIdQuery = new MongoQuery(MongoRequestFilters.buildEq(ThermostatFields.SYSTEM_ID, systemId));
            final MongoQuery query = new MongoQuery(MongoRequestFilters.buildAnd(systemIdQuery, MongoRequestFilters.buildQueriesFilter(queries)));
            final String message = mongoStorageHandler.getMany(storage.getDatabase().getCollection(collectionName), query, limit, offset, sort, includes, excludes);
            return Response.status(Response.Status.OK).entity(message).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    public Response handleGet(HttpServletRequest httpServletRequest, ServletContext context, Integer limit, Integer offset, String sort, String queries, String includes, String excludes, String returnMetadata) {
        try {
            boolean metadata = Boolean.valueOf(returnMetadata);
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(RealmAuthorizer.class.getName());
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

            MongoDataResultContainer execResult;

            if (realmAuthorizer != null) {
                if (realmAuthorizer.readable()) {
                    execResult = mongoExecutor.execGetRequest(
                            storage.getDatabase().getCollection(collectionName), limit, offset, sort, queries, includes, excludes, realmAuthorizer.getReadableRealms());
                } else {
                    return Response.status(Response.Status.FORBIDDEN).build();
                }
            } else {
                execResult = mongoExecutor.execGetRequest(
                        storage.getDatabase().getCollection(collectionName), limit, offset, sort, queries, includes, excludes, null);
            }

            MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();
            response.addQueryDocuments(execResult.getQueryDataResult());
            if (metadata) {
                MongoMetaDataResponseBuilder.MetaBuilder metaDataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();
                MongoMetaDataGenerator metaDataGenerator = new MongoMetaDataGenerator(limit, offset, sort, queries,
                        includes, excludes, httpServletRequest, execResult);

                metaDataGenerator.setDocAndPayloadCount(metaDataResponse);
                metaDataGenerator.setPrev(metaDataResponse);
                metaDataGenerator.setNext(metaDataResponse);

                response.addMetaData(metaDataResponse.build());
            }
            return Response.status(Response.Status.OK).entity(response.build()).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    /*
     *  HTTP PUT handling
     */

    public Response handlePutWithSystemId(HttpServletRequest httpServletRequest, ServletContext context, String systemId, String queries, String metadata, String body) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoStorageHandler.updateOneSystemObject(storage.getDatabase().getCollection(collectionName), systemId, queries, body);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    public Response handlePutWithJvmId(HttpServletRequest httpServletRequest, ServletContext context, String systemId, String jvmId, String queries, String metadata, String body) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoStorageHandler.updateOneJvmObject(storage.getDatabase().getCollection(collectionName), systemId, jvmId, queries, body);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    public Response handlePut(HttpServletRequest httpServletRequest, ServletContext context, String queries, String metadata, String body) {
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

    /*
     *  HTTP POST handling
     */

    public Response handlePostWithSystemID(HttpServletRequest httpServletRequest, ServletContext context, String systemId, String returnMetadata, String body) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            mongoStorageHandler.addSystemObjects(storage.getDatabase().getCollection(collectionName, DBObject.class), systemId, body);
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    public Response handlePost(HttpServletRequest httpServletRequest, ServletContext context, String metadata, String body) {
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

    /*
     *  HTTP DELETE handling
     */

    public Response handleDeleteWithSystemID(HttpServletRequest httpServletRequest, ServletContext context, String systemId, String queries, String metadata) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            final MongoQuery query = new MongoQuery(MongoRequestFilters.buildEq(ThermostatFields.SYSTEM_ID, systemId));
            mongoStorageHandler.deleteMany(storage.getDatabase().getCollection(collectionName), query);
            return Response.status(Response.Status.OK).build();
        } catch(Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    public Response handleDeleteWithJvmID(HttpServletRequest httpServletRequest, ServletContext context, String systemId, String jvmId, String queries, String metadata) {
        try {
            ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
            final MongoQuery querySystemId = new MongoQuery(MongoRequestFilters.buildEq(ThermostatFields.SYSTEM_ID, systemId));
            final MongoQuery queryJvmId = new MongoQuery(MongoRequestFilters.buildEq(ThermostatFields.JVM_ID, jvmId));
            mongoStorageHandler.deleteMany(storage.getDatabase().getCollection(collectionName), MongoRequestFilters.buildAnd(querySystemId, queryJvmId));
            return Response.status(Response.Status.OK).build();
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    public Response handleDelete(HttpServletRequest httpServletRequest, ServletContext context, String queries, String metadata) {
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
