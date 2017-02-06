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

package com.redhat.thermostat.server.core.internal.security.auth.basic;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.DatatypeConverter;

import com.redhat.thermostat.server.core.internal.security.UserStore;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter {

    private final UserStore userStore;

    public BasicAuthFilter(UserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
            String authentication = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authentication == null) {
                throw new NotAuthorizedException("Authentication credentials are required");
            }

            if (!authentication.startsWith("Basic ")) {
                throw new NotAuthorizedException("Authentication credentials are required");
            }

            authentication = authentication.substring("Basic ".length());
            String[] values = new String(DatatypeConverter.parseBase64Binary(authentication),
                    Charset.forName("ASCII")).split(":");
            if (values.length < 2) {
                throw new WebApplicationException(400);
            }

            String username = values[0];
            String password = values[1];

            BasicWebUser user = (BasicWebUser) userStore.getUser(username);
            if (user == null) {
                throw new NotAuthorizedException("Authentication credentials are required");
            }

            if (!user.getPassword().equals(password)) {
                throw new NotAuthorizedException("Authentication credentials are required");
            }

            requestContext.setSecurityContext(new BasicSecurityContext(user));
    }
}
