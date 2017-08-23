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

package com.redhat.thermostat.gateway.server.auth;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.DatatypeConverter;

import com.redhat.thermostat.gateway.common.core.auth.basic.BasicRealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.auth.InvalidRoleException;
import com.redhat.thermostat.gateway.common.core.auth.RealmAuthorizer;
import com.redhat.thermostat.gateway.server.auth.basic.BasicUserStore;
import com.redhat.thermostat.gateway.common.core.auth.basic.BasicWebUser;

public class BasicAuthFilter implements Filter {
    private BasicUserStore userStore;

    public BasicAuthFilter(BasicUserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        String authentication = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authentication == null) {
            sendHttpUnauth(response);
            return;
        }

        if (!authentication.startsWith("Basic ")) {
            sendHttpUnauth(response);
            return;
        }

        authentication = authentication.substring("Basic ".length());
        String[] values = new String(DatatypeConverter.parseBase64Binary(authentication),
                Charset.forName("ASCII")).split(":");
        if (values.length < 2) {
            sendHttpUnauth(response);
            return;
        }

        String username = values[0];
        String password = values[1];

        BasicWebUser user = userStore.getUser(username);
        if (user == null) {
            sendHttpUnauth(response);
            return;
        }

        if (!user.getPassword().equals(password)) {
            sendHttpUnauth(response);
            return;
        }

        try {
            RealmAuthorizer realmAuthorizer = new BasicRealmAuthorizer(user);
            httpServletRequest.setAttribute(RealmAuthorizer.class.getName(), realmAuthorizer);

            chain.doFilter(request, response);
        } catch (InvalidRoleException e) {
            throw new IllegalStateException("Unable to create DefaultRealmAuthorizer", e);
        }
    }

    private void sendHttpUnauth(ServletResponse response) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("WWW-Authenticate", "Basic realm=\"thermostat\"");
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Override
    public void destroy() {
        // Do nothing
    }
}
