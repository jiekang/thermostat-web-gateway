package io.swagger.api.impl;

import io.swagger.api.*;
import io.swagger.model.*;

import io.swagger.model.AgentInfo;
import io.swagger.model.Filters;
import io.swagger.model.JvmInfo;
import io.swagger.model.SystemInfo;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-17T16:34:48.096Z")
public class NamespaceApiServiceImpl extends NamespaceApiService {
    @Override
    public Response namespaceSystemSystemIdAgentsAgentIdJvmsDelete(String namespace, String systemId, String agentId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemSystemIdAgentsAgentIdJvmsGet(String namespace, String systemId, String agentId, Integer limit, Integer offset, String sort, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemSystemIdAgentsAgentIdJvmsPost(String namespace, String systemId, String agentId, Integer limit, Integer offset, String sort, Filters filters, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemSystemIdAgentsAgentIdJvmsPut(String namespace, String systemId, String agentId, JvmInfo jvmInfo, String tags, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsDelete(String namespace, String systemIds, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsGet(String namespace, Integer limit, Integer offset, String sort, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsPost(String namespace, Integer limit, Integer offset, String sort, Filters filters, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsPut(String namespace, SystemInfo systemInfo, String tags, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsAgentIdDelete(String namespace, String systemId, String agentId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsAgentIdGet(String namespace, String systemId, String agentId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdDelete(String namespace, String systemId, String agentId, String jvmId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdGet(String namespace, String systemId, String agentId, String jvmId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdPut(String namespace, String systemId, String agentId, String jvmId, JvmInfo jvmInfo, String tags, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsAgentIdPut(String namespace, String systemId, String agentId, AgentInfo agentInfo, String tags, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsDelete(String namespace, String systemId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsGet(String namespace, String systemId, Integer limit, Integer offset, String sort, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsPost(String namespace, String systemId, Integer limit, Integer offset, String sort, Filters filters, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdAgentsPut(String namespace, String systemId, AgentInfo agentInfo, String tags, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdDelete(String namespace, String systemId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdGet(String namespace, String systemId, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response namespaceSystemsSystemIdPut(String namespace, String systemId, SystemInfo systemInfo, String tags, SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
