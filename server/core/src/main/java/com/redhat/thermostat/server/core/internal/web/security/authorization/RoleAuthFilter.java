package com.redhat.thermostat.server.core.internal.web.security.authorization;

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
        String paths[] = containerRequestContext.getUriInfo().getPath().substring(1).split("/");
        int length = paths.length;
        SecurityContext securityContext = containerRequestContext.getSecurityContext();

        /*
         * Path:
         * /api/v100/{namespace}/systems/{id}/agents/{id}/jvms/{id}
         *   0    1     2         3        4   5       6   7     8
         * Roles:
         *
         * thermostat-admin
         * thermostat-namespaces-(name)
         * thermostat-systems-(name)
         * thermostat-jvms-(name)
         * thermostat-agents-(name)
         *
         * (name) is either 'all' or some string referencing a specific identity
         */

        /*
         * thermostat-admin is always authorized
         */
        if (securityContext.isUserInRole("thermostat-admin")) {
            return;
        }

        /*
         * request to / is always authorized
         */
        if (length < 3) {
            return;
        }

        String namespaceRole = "thermostat-namespaces-" + paths[2];
        if (!(securityContext.isUserInRole("thermostat-namespaces-all") || securityContext.isUserInRole(namespaceRole))) {
            throw new NotAuthorizedException("Basic: realm=\"thermostat\"");
        }

        if (length < 5) {
            return;
        }

        String systemRole = "thermostat-systems-" + paths[4];
        if (!(securityContext.isUserInRole("thermostat-systems-all") || securityContext.isUserInRole(systemRole))) {
            throw new NotAuthorizedException("Basic: realm=\"thermostat\"");
        }

        if (length < 7) {
            return;
        }

        String agentRole = "thermostat-agents-" + paths[6];
        if (!(securityContext.isUserInRole("thermostat-agents-all") || securityContext.isUserInRole(agentRole))) {
            throw new NotAuthorizedException("Basic: realm=\"thermostat\"");
        }

        if (length < 9) {
            return;
        }

        String jvmRole = "thermostat-jvms-" + paths[8];
        if (!(securityContext.isUserInRole("thermostat-jvms-all") || securityContext.isUserInRole(jvmRole))) {
            throw new NotAuthorizedException("Basic: realm=\"thermostat\"");
        }
    }
}
