package io.swagger.api;

import io.swagger.model.*;
import io.swagger.api.NamespaceApiService;
import io.swagger.api.factories.NamespaceApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import io.swagger.model.AgentInfo;
import io.swagger.model.Filters;
import io.swagger.model.JvmInfo;
import io.swagger.model.SystemInfo;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/{namespace}")
@Consumes({ "application/json" })
@Produces({ "application/json", "text/html; charset=utf-8" })
@io.swagger.annotations.Api(description = "the {namespace} API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-21T16:01:56.111Z")
public class NamespaceApi  {
   private final NamespaceApiService delegate = NamespaceApiServiceFactory.getNamespaceApi();

    @DELETE
    @Path("/systems")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete system information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsDelete(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "A list of system identities",required=true) @QueryParam("systemIds") String systemIds
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsDelete(namespace,systemIds,securityContext);
    }
    @GET
    @Path("/systems")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get system information", response = SystemInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = SystemInfo.class) })
    public Response namespaceSystemsGet(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "Limit of items to return.") @QueryParam("limit") Integer limit
,@ApiParam(value = "Offset of items to return.") @QueryParam("offset") Integer offset
,@ApiParam(value = "Sort string") @QueryParam("sort") String sort
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsGet(namespace,limit,offset,sort,securityContext);
    }
    @POST
    @Path("/systems")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Query system information", response = SystemInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = SystemInfo.class) })
    public Response namespaceSystemsPost(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "Limit of items to return.") @QueryParam("limit") Integer limit
,@ApiParam(value = "Offset of items to return.") @QueryParam("offset") Integer offset
,@ApiParam(value = "Sort string") @QueryParam("sort") String sort
,@ApiParam(value = "Key, Comparator, Value trios to filter the data" ) Filters filters
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsPost(namespace,limit,offset,sort,filters,securityContext);
    }
    @PUT
    @Path("/systems")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add or update system information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsPut(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system information" ,required=true) SystemInfo systemInfo
,@ApiParam(value = "CSV of tags") @QueryParam("tags") String tags
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsPut(namespace,systemInfo,tags,securityContext);
    }
    @DELETE
    @Path("/systems/{systemId}/agents/{agentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete agent information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdDelete(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdDelete(namespace,systemId,agentId,securityContext);
    }
    @GET
    @Path("/systems/{systemId}/agents/{agentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get agent information", response = AgentInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = AgentInfo.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Access prohibited.", response = AgentInfo.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdGet(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdGet(namespace,systemId,agentId,securityContext);
    }
    @DELETE
    @Path("/systems/{systemId}/agents/{agentId}/jvms")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete JVM information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsDelete(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdJvmsDelete(namespace,systemId,agentId,securityContext);
    }
    @GET
    @Path("/systems/{systemId}/agents/{agentId}/jvms")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get JVM information", response = JvmInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = JvmInfo.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Access prohibited.", response = JvmInfo.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsGet(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@ApiParam(value = "Limit of items to return.") @QueryParam("limit") Integer limit
,@ApiParam(value = "Offset of items to return.") @QueryParam("offset") Integer offset
,@ApiParam(value = "Sort string") @QueryParam("sort") String sort
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdJvmsGet(namespace,systemId,agentId,limit,offset,sort,securityContext);
    }
    @DELETE
    @Path("/systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete JVM information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdDelete(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@ApiParam(value = "The JVM identity",required=true) @PathParam("jvmId") String jvmId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdDelete(namespace,systemId,agentId,jvmId,securityContext);
    }
    @GET
    @Path("/systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get JVM information", response = JvmInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = JvmInfo.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Access prohibited.", response = JvmInfo.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdGet(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@ApiParam(value = "The JVM identity",required=true) @PathParam("jvmId") String jvmId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdGet(namespace,systemId,agentId,jvmId,securityContext);
    }
    @PUT
    @Path("/systems/{systemId}/agents/{agentId}/jvms/{jvmId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Adds or updates JVM information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdPut(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@ApiParam(value = "The JVM identity",required=true) @PathParam("jvmId") String jvmId
,@ApiParam(value = "The jvm information" ,required=true) JvmInfo jvmInfo
,@ApiParam(value = "CSV of tags") @QueryParam("tags") String tags
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdPut(namespace,systemId,agentId,jvmId,jvmInfo,tags,securityContext);
    }
    @POST
    @Path("/systems/{systemId}/agents/{agentId}/jvms")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Query JVM information", response = JvmInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = JvmInfo.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsPost(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@ApiParam(value = "Limit of items to return.") @QueryParam("limit") Integer limit
,@ApiParam(value = "Offset of items to return.") @QueryParam("offset") Integer offset
,@ApiParam(value = "Sort string") @QueryParam("sort") String sort
,@ApiParam(value = "Key, Comparator, Value trios to filter the data" ) Filters filters
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdJvmsPost(namespace,systemId,agentId,limit,offset,sort,filters,securityContext);
    }
    @PUT
    @Path("/systems/{systemId}/agents/{agentId}/jvms")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Adds or updates JVM information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsPut(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@ApiParam(value = "The jvm information" ,required=true) JvmInfo jvmInfo
,@ApiParam(value = "CSV of tags") @QueryParam("tags") String tags
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdJvmsPut(namespace,systemId,agentId,jvmInfo,tags,securityContext);
    }
    @PUT
    @Path("/systems/{systemId}/agents/{agentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Adds or updates agent information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsAgentIdPut(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent identity",required=true) @PathParam("agentId") String agentId
,@ApiParam(value = "The agent information" ,required=true) AgentInfo agentInfo
,@ApiParam(value = "CSV of tags") @QueryParam("tags") String tags
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsAgentIdPut(namespace,systemId,agentId,agentInfo,tags,securityContext);
    }
    @DELETE
    @Path("/systems/{systemId}/agents")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete agent information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsDelete(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsDelete(namespace,systemId,securityContext);
    }
    @GET
    @Path("/systems/{systemId}/agents")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get agent information", response = AgentInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = AgentInfo.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Access prohibited.", response = AgentInfo.class) })
    public Response namespaceSystemsSystemIdAgentsGet(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "Limit of items to return.") @QueryParam("limit") Integer limit
,@ApiParam(value = "Offset of items to return.") @QueryParam("offset") Integer offset
,@ApiParam(value = "Sort string") @QueryParam("sort") String sort
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsGet(namespace,systemId,limit,offset,sort,securityContext);
    }
    @POST
    @Path("/systems/{systemId}/agents")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Query agent information", response = AgentInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = AgentInfo.class) })
    public Response namespaceSystemsSystemIdAgentsPost(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "Limit of items to return.") @QueryParam("limit") Integer limit
,@ApiParam(value = "Offset of items to return.") @QueryParam("offset") Integer offset
,@ApiParam(value = "Sort string") @QueryParam("sort") String sort
,@ApiParam(value = "Key, Comparator, Value trios to filter the data" ) Filters filters
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsPost(namespace,systemId,limit,offset,sort,filters,securityContext);
    }
    @PUT
    @Path("/systems/{systemId}/agents")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Adds or updates agent information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdAgentsPut(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The agent information" ,required=true) AgentInfo agentInfo
,@ApiParam(value = "CSV of tags") @QueryParam("tags") String tags
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdAgentsPut(namespace,systemId,agentInfo,tags,securityContext);
    }
    @DELETE
    @Path("/systems/{systemId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Delete system information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdDelete(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdDelete(namespace,systemId,securityContext);
    }
    @GET
    @Path("/systems/{systemId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get system information", response = SystemInfo.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = SystemInfo.class) })
    public Response namespaceSystemsSystemIdGet(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdGet(namespace,systemId,securityContext);
    }
    @PUT
    @Path("/systems/{systemId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "text/html; charset=utf-8" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Add or update system information", response = void.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = void.class) })
    public Response namespaceSystemsSystemIdPut(@ApiParam(value = "The namespace",required=true) @PathParam("namespace") String namespace
,@ApiParam(value = "The system identity",required=true) @PathParam("systemId") String systemId
,@ApiParam(value = "The system information" ,required=true) SystemInfo systemInfo
,@ApiParam(value = "CSV of tags") @QueryParam("tags") String tags
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.namespaceSystemsSystemIdPut(namespace,systemId,systemInfo,tags,securityContext);
    }
}
