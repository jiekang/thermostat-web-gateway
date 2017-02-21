package io.swagger.api.impl;

import io.swagger.api.ApiResponseMessage;
import io.swagger.api.DefaultApiService;
import io.swagger.api.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-21T16:01:56.111Z")
public class DefaultApiServiceImpl extends DefaultApiService {

    @Override
    public Response rootGet(SecurityContext securityContext) throws NotFoundException {

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
