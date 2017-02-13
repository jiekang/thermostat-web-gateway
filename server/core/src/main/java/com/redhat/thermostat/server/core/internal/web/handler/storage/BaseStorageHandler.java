package com.redhat.thermostat.server.core.internal.web.handler.storage;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public interface BaseStorageHandler {
    void getAgent(SecurityContext securityContext, AsyncResponse asyncResponse, String plugin, String agentId, String count, String sort);

    void putAgent(String body, SecurityContext context, AsyncResponse asyncResponse, String plugin);

    void getHostInfo(SecurityContext securityContext, AsyncResponse asyncResponse, String plugin, String agentId, String count, String sort, String maxTimestamp, String minTimestamp);

    void getVmInfo(SecurityContext securityContext, AsyncResponse asyncResponse, String plugin, String agentId, String vmId, String count, String sort, String maxTimestamp, String minTimestamp);
}
