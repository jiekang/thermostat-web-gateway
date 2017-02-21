package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.DefaultApiService;
import io.swagger.api.factories.DefaultApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import io.swagger.model.Namespaces;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/")
@Consumes({ "application/json" })
@Produces({ "application/json", "text/html; charset=utf-8" })
@io.swagger.annotations.Api(description = "the  API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-21T16:01:56.111Z")
public class DefaultApi  {
   private final DefaultApiService delegate = DefaultApiServiceFactory.getDefaultApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get namespaces", response = Namespaces.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = Namespaces.class) })
    public Response rootGet(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.rootGet(securityContext);
    }
}
