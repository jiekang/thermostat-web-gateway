package com.redhat.thermostat.server.core.internal.web.handler.storage;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ChunkedOutput;

public interface PluginStorageHandler {
    void getPath(SecurityContext securityContext,
                 AsyncResponse asyncResponse,
                 String path);

    void putPath(SecurityContext securityContext,
                 AsyncResponse asyncResponse,
                 String path,
                 String body);

    void postPath(SecurityContext securityContext,
                  AsyncResponse asyncResponse,
                  String path,
                  String body);

    void deletePath(SecurityContext securityContext,
                    AsyncResponse asyncResponse,
                    String path);

    ChunkedOutput<String> streamPath(SecurityContext securityContext,
                                     AsyncResponse asyncResponse,
                                     String path);

    void getPathItem(SecurityContext securityContext,
                     AsyncResponse asyncResponse,
                     String path,
                     String item);

    void putPathItem(SecurityContext securityContext,
                     AsyncResponse asyncResponse,
                     String path,
                     String item,
                     String body);

    void postPathItem(SecurityContext securityContext,
                      AsyncResponse asyncResponse,
                      String path,
                      String item,
                      String body);

    void deletePathItem(SecurityContext securityContext,
                        AsyncResponse asyncResponse,
                        String path,
                        String item);
}
