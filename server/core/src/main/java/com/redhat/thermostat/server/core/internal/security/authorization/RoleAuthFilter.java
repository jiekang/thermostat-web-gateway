package com.redhat.thermostat.server.core.internal.security.authorization;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RoleAuthFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException, NotAuthorizedException {
        String paths[] = containerRequestContext.getUriInfo().getPath().split("/");
        SecurityContext securityContext = containerRequestContext.getSecurityContext();

        /**
         * Path:
         * /{namespace}/systems/{id}/agents/{id}/jvms/{id}
         *       0         1      2    3      4    5   6
         *       1         2      3    4      5    6   7
         * Roles:
         *
         * thermostat-
         *
         */

        /**
         * thermostat-admin is always authorized
         */
        if (securityContext.isUserInRole("thermostat-admin")) {
            return;
        }

        /**
         * request to / is always authorized
         */
        if (paths.length < 1) {
            return;
        }

        String namespaceRole = "thermostat-namespace-" + paths[0];
        if (!(securityContext.isUserInRole("thermostat-namespace-all") || securityContext.isUserInRole(namespaceRole))) {
            throw new NotAuthorizedException("Authentication credentials are required");
        }

        if (paths.length < 3) {
            return;
        }

        String systemRole = "thermostat-systems-" + paths[2];
        if (!(securityContext.isUserInRole("thermostat-system-all") || securityContext.isUserInRole(systemRole))) {
            throw new NotAuthorizedException("Authentication credentials are required");
        }

        if (paths.length < 5) {
            return;
        }

        String agentRole = "thermostat-agents-" + paths[4];
        if (!(securityContext.isUserInRole("thermostat-agents-all") || securityContext.isUserInRole(agentRole))) {
            throw new NotAuthorizedException("Authentication credentials are required");
        }

        if (paths.length < 7) {
            return;
        }

        String jvmRole = "thermostat-jvms-" + paths[6];
        if (!(securityContext.isUserInRole("thermostat-jvms-all") || securityContext.isUserInRole(jvmRole))) {
            throw new NotAuthorizedException("Authentication credentials are required");
        }
    }
}
