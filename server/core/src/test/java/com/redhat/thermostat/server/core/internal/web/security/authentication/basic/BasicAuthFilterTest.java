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

package com.redhat.thermostat.server.core.internal.web.security.authentication.basic;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class BasicAuthFilterTest {

    private BasicAuthFilter basicAuthFilter;
    private ContainerRequestContext crq;
    private final String userName = "user";


    private final ArgumentCaptor<SecurityContext> sc = ArgumentCaptor.forClass(SecurityContext.class);

    @Before
    public void setup() {
        String password = "password;";
        String userInfo = password + ",a,b";

        Map<String, String> entries = new HashMap<>();
        entries.put(userName, userInfo);

        crq = mock(ContainerRequestContext.class);

        String authString = DatatypeConverter.printBase64Binary((userName + ":" + password).getBytes());
        when(crq.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + authString);

        basicAuthFilter = new BasicAuthFilter(new BasicUserStore(entries));
    }

    @Test(expected = NotAuthorizedException.class)
    public void testNoAuthHeader() throws IOException {
        ContainerRequestContext crq = mock(ContainerRequestContext.class);
        basicAuthFilter.filter(crq);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testInvalidAuthHeader() throws IOException {
        ContainerRequestContext crq = mock(ContainerRequestContext.class);
        String authString = DatatypeConverter.printBase64Binary("user:password".getBytes());
        when(crq.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("NotBasic " + authString);
        basicAuthFilter.filter(crq);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testInvalidUserPassHeader() throws IOException {
        ContainerRequestContext crq = mock(ContainerRequestContext.class);
        String authString = DatatypeConverter.printBase64Binary("blob".getBytes());
        when(crq.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + authString);
        basicAuthFilter.filter(crq);
    }


    @Test(expected = NotAuthorizedException.class)
    public void testUserNotInStore() throws IOException {
        basicAuthFilter = new BasicAuthFilter(new BasicUserStore(Collections.EMPTY_MAP));
        basicAuthFilter.filter(crq);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testIncorrectPassword() throws IOException {
        ContainerRequestContext crq = mock(ContainerRequestContext.class);
        String authString = DatatypeConverter.printBase64Binary((userName + ":badpassword").getBytes());
        when(crq.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("Basic " + authString);
        basicAuthFilter.filter(crq);
    }

    @Test
    public void testGetUserPrincipalName() throws IOException {
        basicAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(userName, sc.getValue().getUserPrincipal().getName());
    }

    @Test
    public void testIsUserInRole() throws IOException {
        basicAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(true, sc.getValue().isUserInRole("a"));
        assertEquals(true, sc.getValue().isUserInRole("b"));
    }

    @Test
    public void testIsUserNotInRole() throws IOException {
        basicAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(false, sc.getValue().isUserInRole("unknown-role"));
    }

    @Test
    public void testIsSecure() throws IOException {
        basicAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(true, sc.getValue().isSecure());
    }

    @Test
    public void testGetAuthenticationScheme() throws IOException {
        basicAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals("Basic", sc.getValue().getAuthenticationScheme());
    }
}
