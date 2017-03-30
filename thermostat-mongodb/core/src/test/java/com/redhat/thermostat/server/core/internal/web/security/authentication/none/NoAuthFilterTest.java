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

package com.redhat.thermostat.server.core.internal.web.security.authentication.none;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class NoAuthFilterTest {

    private ContainerRequestContext crq;
    private final ArgumentCaptor<SecurityContext> sc = ArgumentCaptor.forClass(SecurityContext.class);
    @Before
    public void setup() throws IOException {
        crq = mock(ContainerRequestContext.class);

        NoAuthFilter noAuthFilter = new NoAuthFilter();
        noAuthFilter.filter(crq);
    }

    @Test
    public void testGetUserPrincipalName() {
        verify(crq).setSecurityContext(sc.capture());

        assertEquals("user", sc.getValue().getUserPrincipal().getName());
    }

    @Test
    public void testIsUserInRole() {
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(true, sc.getValue().isUserInRole("any-string-works"));
    }

    @Test
    public void testIsSecure() {
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(false, sc.getValue().isSecure());
    }

    @Test
    public void testGetAuthenticationScheme() {
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(null, sc.getValue().getAuthenticationScheme());
    }
}
