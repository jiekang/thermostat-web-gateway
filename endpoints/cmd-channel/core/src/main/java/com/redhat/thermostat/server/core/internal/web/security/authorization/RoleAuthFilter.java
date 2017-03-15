/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

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
