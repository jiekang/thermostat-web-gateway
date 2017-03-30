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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

public class RoleAuthFilterTest {
    private RoleAuthFilter roleAuthFilter;
    private ContainerRequestContext containerRequestContext;
    private UriInfo uriInfo;
    private SecurityContext securityContext;

    @Before
    public void setup() {
        containerRequestContext = mock(ContainerRequestContext.class);

        uriInfo = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        securityContext = mock(SecurityContext.class);
        when(containerRequestContext.getSecurityContext()).thenReturn(securityContext);

        roleAuthFilter = new RoleAuthFilter();
    }

    @Test
    public void testAsAdmin() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/systems/all/agents/all/jvms/all");

        when(securityContext.isUserInRole("thermostat-admin")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testAsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/systems/all/agents/all/jvms/all");

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testAsNoneBase() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100");

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testNamespacesAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testNamespacesNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems");

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testNamespacesSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testNamespacesSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testSystemsAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testSystemsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testSystemsSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testSystemsSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testAgentsAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testAgentsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testAgentsSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testAgentsSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testJvmsAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all/jvms/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-jvms-all")).thenReturn(true);


        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testJvmsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all/jvms/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testJvmsSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific/jvms/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(true);


        when(securityContext.isUserInRole("thermostat-jvms-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-jvms-specific")).thenReturn(true);


        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testJvmsSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific/jvms/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(true);


        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }
}
