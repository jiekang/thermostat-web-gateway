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

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
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

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;
import com.redhat.thermostat.gateway.common.mongodb.servlet.ServletContextConstants;

@Path("/")
public class JvmGcHttpHandler {
    private final String collectionName = "vm-gc-stats";

    @GET
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getJvmMemory(@QueryParam("l") @DefaultValue("1") Integer limit,
                                 @QueryParam("o") @DefaultValue("1") Integer offset,
                                 @QueryParam("s") String sort,
                                 @QueryParam("q") String queries,
                                 @QueryParam("p") String projections,
                                 @Context ServletContext context
    ) {
        List<String> queriesList;
        if (queries != null) {
            queriesList = Arrays.asList(queries.split(","));
        } else {
            queriesList = Collections.emptyList();
        }

        List<String> projectionsList;
        if (projections != null) {
            projectionsList = Arrays.asList(projections.split(","));
        } else {
            projectionsList = Collections.emptyList();
        }

        final Bson query = MongoRequestFilters.buildQueriesFilter(queriesList);

        final Bson sortObject = MongoSortFilters.createSortObject(sort);

        ThermostatMongoStorage storage;
        synchronized (context) {
            storage = (ThermostatMongoStorage)context.getAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE);
        }
        FindIterable<Document> documents = storage.getDatabase().getCollection(collectionName).find(query).projection(fields(include(projectionsList), excludeId())).sort(sortObject).limit(limit).skip(offset).batchSize(limit).cursorType(CursorType.NonTailable);

        String message = MongoResponseBuilder.buildGetResponse(documents);

        return Response.status(Response.Status.OK).entity(message).build();
    }

    @PUT
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response putJvmMemory() {
        return Response.ok().build();
    }

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response postJvmMemory() {
        return Response.ok().build();
    }

    @DELETE
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response deleteJvmMemory() {
        return Response.ok().build();
    }
}
