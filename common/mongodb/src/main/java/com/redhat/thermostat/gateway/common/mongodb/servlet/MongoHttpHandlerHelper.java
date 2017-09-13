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

import com.redhat.thermostat.gateway.common.core.auth.RealmAuthorizer;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatFields;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoDataResultContainer;
import com.redhat.thermostat.gateway.common.mongodb.executor.MongoExecutor;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataGenerator;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoMetaDataResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;
import com.redhat.thermostat.gateway.common.util.HttpResponseExceptionHandler;
import org.bson.json.JsonParseException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.LinkedHashMap;

import static com.redhat.thermostat.gateway.common.util.ServiceException.CANNOT_QUERY_REALMS_PROPERTY;
import static com.redhat.thermostat.gateway.common.util.ServiceException.DATABASE_UNAVAILABLE;
import static com.redhat.thermostat.gateway.common.util.ServiceException.EXPECTED_JSON_ARRAY;
import static com.redhat.thermostat.gateway.common.util.ServiceException.MALFORMED_CLIENT_REQUEST;
import static com.redhat.thermostat.gateway.common.util.ServiceException.UNEXPECTED_ERROR;

public class MongoHttpHandlerHelper {

    private final String collectionName;
    private final MongoExecutor mongoExecutor = new MongoExecutor();
    private final HttpResponseExceptionHandler exceptionHandler = new HttpResponseExceptionHandler();

    public MongoHttpHandlerHelper(String collectionName) {
        this.collectionName = collectionName;
        exceptionHandler.add(MongoWriteException.class, MALFORMED_CLIENT_REQUEST)
                .add(JsonParseException.class, MALFORMED_CLIENT_REQUEST)
                .add(UnsupportedOperationException.class, MALFORMED_CLIENT_REQUEST)
                .add(ClassCastException.class, EXPECTED_JSON_ARRAY)
                .add(MongoTimeoutException.class, DATABASE_UNAVAILABLE)
                .add(IOException.class, CANNOT_QUERY_REALMS_PROPERTY)
                .add(NullPointerException.class, UNEXPECTED_ERROR);
    }

    /*
     *  HTTP GET handling
     */

    public Response handleGetWithSystemID(HttpServletRequest httpServletRequest, ServletContext context,
                                          String systemId, int limit, int offset, String sort, String queries,
                                          String includes, String excludes, boolean returnMetadata) {
        return handleGet(httpServletRequest, context, limit, offset, sort,
                andSystemIdQuery(queries, systemId), includes, excludes, returnMetadata, queries);
    }

    public Response handleGetWithJvmID(HttpServletRequest httpServletRequest, ServletContext context, String systemId,
                                       String jvmId, int limit, int offset, String sort, String queries,
                                       String includes, String excludes, boolean returnMetadata) {
        return handleGet(httpServletRequest, context, limit, offset, sort,
                andSystemIdJvmIdQuery(queries, systemId, jvmId), includes, excludes, returnMetadata, queries);
    }

    public Response handleGet(HttpServletRequest httpServletRequest, ServletContext context, int limit, int offset,
                              String sort, String queries, String includes, String excludes, boolean returnMetadata) {
        return handleGet(httpServletRequest, context, limit, offset, sort, queries, includes, excludes, returnMetadata, "");
    }

    /*
     * originalQueries contains only query info from the client's original request argument. queries contains this info,
     * as well as added JVM/SYS ids built by andSystemIdJvmIdQuery(...). 
     */
    public Response handleGet(HttpServletRequest httpServletRequest, ServletContext context, int limit, int offset,
                              String sort, String queries, String includes, String excludes, boolean returnMetadata,
                              String originalQueries) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(
                    RealmAuthorizer.class.getName());

            if (realmAuthorizer.readable()) {
                ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(
                        ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

                MongoDataResultContainer execResult = mongoExecutor.execGetRequest(
                        storage.getDatabase().getCollection(collectionName), limit, offset, sort, queries, includes,
                        excludes, realmAuthorizer.getReadableRealms());

                MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();
                response.addQueryDocuments(execResult.getQueryDataResult());

                if (returnMetadata) {
                    // Test suites expect a consistent order of next and prev links, hence LinkedHashMap
                    LinkedHashMap<String, String> paramArgs = new LinkedHashMap<>();
                    paramArgs.put(RequestParameters.SORT, sort);
                    paramArgs.put(RequestParameters.QUERY, originalQueries);
                    paramArgs.put(RequestParameters.INCLUDE, includes);
                    paramArgs.put(RequestParameters.EXCLUDE, excludes);
                    paramArgs.put(RequestParameters.METADATA, Boolean.toString(returnMetadata));
                    paramArgs.put(RequestParameters.LIMIT, String.valueOf(limit));
                    paramArgs.put(RequestParameters.OFFSET, String.valueOf(offset));

                    String baseUrl = httpServletRequest.getRequestURL().toString();
                    MongoMetaDataResponseBuilder.MetaBuilder metaDataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();
                    MongoMetaDataGenerator metaDataGenerator = new MongoMetaDataGenerator(limit, offset, sort, queries,
                            includes, excludes, paramArgs, execResult, baseUrl);

                    metaDataGenerator.setDocAndPayloadCount(metaDataResponse);
                    metaDataGenerator.setPrev(metaDataResponse);
                    metaDataGenerator.setNext(metaDataResponse);

                    response.addMetaData(metaDataResponse.build());
                }
                return Response.status(Response.Status.OK).entity(response.build()).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    /*
     *  HTTP PUT handling
     */

    public Response handlePutWithSystemId(HttpServletRequest httpServletRequest, ServletContext context,
                                          String systemId, String queries, boolean returnMetadata, String body) {
        return handlePut(httpServletRequest, context, systemId, null, queries, returnMetadata, body);
    }

    public Response handlePutWithJvmId(HttpServletRequest httpServletRequest, ServletContext context,
                                       String systemId, String jvmId, String queries, boolean returnMetadata, String body) {
        return handlePut(httpServletRequest, context, systemId, jvmId, queries, returnMetadata, body);
    }

    public Response handlePut(HttpServletRequest httpServletRequest, ServletContext context, String queries,
                              boolean returnMetadata, String body) {
        return handlePut(httpServletRequest, context, null, null, queries, returnMetadata, body);
    }

    public Response handlePut(HttpServletRequest httpServletRequest, ServletContext context, String systemId,
                              String jvmId, String queries, boolean returnMetadata, String body) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(
                    RealmAuthorizer.class.getName());

            if (realmAuthorizer.updatable()) {
                ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(
                        ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

                MongoDataResultContainer execResult = mongoExecutor.execPutRequest(
                        storage.getDatabase().getCollection(collectionName), body, queries,
                        realmAuthorizer.getUpdatableRealms(), systemId, jvmId);

                MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();
                if (returnMetadata) {
                    MongoMetaDataResponseBuilder.MetaBuilder metadataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();
                    metadataResponse.matchCount(execResult.getPutReqMatches());

                    response.addMetaData(metadataResponse.build());
                }

                return Response.status(Response.Status.OK).entity(response.build()).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    /*
     *  HTTP POST handling
     */

    public Response handlePostWithSystemID(HttpServletRequest httpServletRequest, ServletContext context,
                                           String systemId, boolean returnMetadata, String body) {
        return handlePost(httpServletRequest, context, systemId, null, returnMetadata, body);
    }

    public Response handlePostWithJvmID(HttpServletRequest httpServletRequest, ServletContext context,
                                        String systemId, String jvmId, boolean returnMetadata, String body) {
        return handlePost(httpServletRequest, context, systemId, jvmId, returnMetadata, body);
    }

    public Response handlePost(HttpServletRequest httpServletRequest, ServletContext context,
                               boolean returnMetadata, String body) {
        return handlePost(httpServletRequest, context, null, null, returnMetadata, body);
    }

    public Response handlePost(HttpServletRequest httpServletRequest, ServletContext context, String systemId,
                               String jvmId, boolean returnMetadata, String body) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(RealmAuthorizer.class.getName());

            if (realmAuthorizer.writable()) {
                ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(
                        ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

                MongoDataResultContainer execResult = mongoExecutor.execPostRequest(
                        storage.getDatabase().getCollection(collectionName, DBObject.class), body,
                        realmAuthorizer.getWritableRealms(), systemId, jvmId);

                MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();

                if (returnMetadata) {
                    MongoMetaDataResponseBuilder.MetaBuilder metadataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();
                    metadataResponse.insertCount(execResult.getPostReqInsertions());

                    response.addMetaData(metadataResponse.build());
                }

                return Response.status(Response.Status.OK).entity(response.build()).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    /*
     *  HTTP DELETE handling
     */

    public Response handleDeleteWithSystemID(HttpServletRequest httpServletRequest, ServletContext context,
                                             String systemId, String queries, boolean returnMetadata) {
        return handleDelete(httpServletRequest, context, andSystemIdQuery(queries, systemId), returnMetadata);
    }

    public Response handleDeleteWithJvmID(HttpServletRequest httpServletRequest, ServletContext context,
                                          String systemId, String jvmId, String queries, boolean returnMetadata) {
        return handleDelete(httpServletRequest, context, andSystemIdJvmIdQuery(queries, systemId, jvmId), returnMetadata);
    }

    public Response handleDelete(HttpServletRequest httpServletRequest, ServletContext context, String queries, boolean returnMetadata) {
        try {
            RealmAuthorizer realmAuthorizer = (RealmAuthorizer) httpServletRequest.getAttribute(RealmAuthorizer.class.getName());
            if (realmAuthorizer.deletable()) {
                ThermostatMongoStorage storage = (ThermostatMongoStorage) context.getAttribute(
                        ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);

                MongoDataResultContainer execResult = mongoExecutor.execDeleteRequest(
                        storage.getDatabase().getCollection(collectionName), queries, realmAuthorizer.getDeletableRealms());

                MongoResponseBuilder.Builder response = new MongoResponseBuilder.Builder();
                if (returnMetadata) {
                    MongoMetaDataResponseBuilder.MetaBuilder metadataResponse = new MongoMetaDataResponseBuilder.MetaBuilder();
                    metadataResponse.matchCount(execResult.getDeleteReqMatches());

                    response.addMetaData(metadataResponse.build());
                }

                return Response.status(Response.Status.OK).entity(response.build()).build();
            } else {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        } catch (Exception e) {
            return exceptionHandler.generateResponseForException(e);
        }
    }

    /*
     * Utility code
     */

    private String andSystemIdQuery(final String originalQuery, final String systemId) {
        final String sysQuery = ThermostatFields.SYSTEM_ID + "==\"" + systemId + '"';
        return isNullOrEmpty(originalQuery) ? sysQuery : sysQuery + ',' + originalQuery;
    }

    private String andSystemIdJvmIdQuery(final String originalQuery, final String systemId, final String jvmId) {
        final String jvmQuery = ThermostatFields.JVM_ID + "==\"" + jvmId + '"';
        final String sysJvmQuery = jvmQuery + (isNullOrEmpty(systemId) ? "" : ',' + ThermostatFields.SYSTEM_ID + "==\"" + systemId + '"');
        return isNullOrEmpty(originalQuery) ? sysJvmQuery : sysJvmQuery + ',' + originalQuery;
    }

    private final boolean isNullOrEmpty(final String s) {
        return s == null || s.isEmpty();
    }

}
