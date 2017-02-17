package io.swagger.api;

import io.swagger.api.*;
import io.swagger.model.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import io.swagger.model.AgentInfo;
import io.swagger.model.Filters;
import io.swagger.model.JvmInfo;
import io.swagger.model.SystemInfo;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-17T16:34:48.096Z")
public abstract class NamespaceApiService {
    public abstract Response namespaceSystemSystemIdAgentsAgentIdJvmsDelete(String namespace,String systemId,String agentId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemSystemIdAgentsAgentIdJvmsGet(String namespace,String systemId,String agentId,Integer limit,Integer offset,String sort,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemSystemIdAgentsAgentIdJvmsPost(String namespace,String systemId,String agentId,Integer limit,Integer offset,String sort,Filters filters,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemSystemIdAgentsAgentIdJvmsPut(String namespace,String systemId,String agentId,JvmInfo jvmInfo,String tags,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsDelete(String namespace,String systemIds,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsGet(String namespace,Integer limit,Integer offset,String sort,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsPost(String namespace,Integer limit,Integer offset,String sort,Filters filters,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsPut(String namespace,SystemInfo systemInfo,String tags,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsAgentIdDelete(String namespace,String systemId,String agentId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsAgentIdGet(String namespace,String systemId,String agentId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdDelete(String namespace,String systemId,String agentId,String jvmId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdGet(String namespace,String systemId,String agentId,String jvmId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsAgentIdJvmsJvmIdPut(String namespace,String systemId,String agentId,String jvmId,JvmInfo jvmInfo,String tags,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsAgentIdPut(String namespace,String systemId,String agentId,AgentInfo agentInfo,String tags,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsDelete(String namespace,String systemId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsGet(String namespace,String systemId,Integer limit,Integer offset,String sort,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsPost(String namespace,String systemId,Integer limit,Integer offset,String sort,Filters filters,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdAgentsPut(String namespace,String systemId,AgentInfo agentInfo,String tags,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdDelete(String namespace,String systemId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdGet(String namespace,String systemId,SecurityContext securityContext) throws NotFoundException;
    public abstract Response namespaceSystemsSystemIdPut(String namespace,String systemId,SystemInfo systemInfo,String tags,SecurityContext securityContext) throws NotFoundException;
}
