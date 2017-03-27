package com.redhat.thermostat.service.jvm.gc;


import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.CursorType;
import com.mongodb.client.FindIterable;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoRequestFilters;
import com.redhat.thermostat.gateway.common.mongodb.filters.MongoSortFilters;
import com.redhat.thermostat.gateway.common.mongodb.response.MongoResponseBuilder;

@Path("/")
public class JvmGcHttpHandler {
    static {
        Map<String, String> mongoConfig = new HashMap<>();
        mongoConfig.put("MONGO_URL", "mongodb://127.0.0.1:27518");
        mongoConfig.put("MONGO_DB", "thermostat");
        mongoConfig.put("MONGO_USERNAME", "mongodevuser");
        mongoConfig.put("MONGO_PASSWORD", "mongodevpassword");


        ThermostatMongoStorage.start(mongoConfig);
    }
    private final String collectionName = "vm-gc-stats";

    @GET
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    public Response getJvmMemory(@QueryParam("l") @DefaultValue("1") Integer limit,
                                 @QueryParam("o") @DefaultValue("1") Integer offset,
                                 @QueryParam("s") String sort,
                                 @QueryParam("q") String queries,
                                 @QueryParam("p") String projections
    ) {
        List<String> queriesList;
        if (queries != null) {
            queriesList = Arrays.asList(queries.split(","));
        } else {
            queriesList = Collections.EMPTY_LIST;
        }

        List<String> projectionsList;
        if (projections != null) {
            projectionsList = Arrays.asList(projections.split(","));
        } else {
            projectionsList = Collections.EMPTY_LIST;
        }

        final Bson query = MongoRequestFilters.buildFilter(queriesList, Collections.EMPTY_LIST);

        final Bson sortObject = MongoSortFilters.createSortObject(sort);

        FindIterable<Document> documents = ThermostatMongoStorage.getDatabase().getCollection(collectionName).find(query).projection(fields(include(projectionsList), excludeId())).sort(sortObject).limit(limit).skip(offset).batchSize(limit).cursorType(CursorType.NonTailable);

        String message = MongoResponseBuilder.buildJsonResponse(MongoResponseBuilder.buildJsonDocuments(documents));

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
