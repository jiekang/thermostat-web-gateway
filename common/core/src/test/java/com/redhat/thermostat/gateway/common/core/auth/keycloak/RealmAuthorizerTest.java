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

package com.redhat.thermostat.gateway.common.core.auth.keycloak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;

public class RealmAuthorizerTest {

    HttpServletRequest request;
    AccessToken.Access access;

    @Before
    public void setup() {
        request = mock(HttpServletRequest.class);
        KeycloakSecurityContext keycloakSecurityContext = mock(KeycloakSecurityContext.class);
        when(request.getAttribute(eq(KeycloakSecurityContext.class.getName()))).thenReturn(keycloakSecurityContext);

        AccessToken accessToken = mock(AccessToken.class);
        when(keycloakSecurityContext.getToken()).thenReturn(accessToken);

        access = mock(AccessToken.Access.class);
        when(accessToken.getRealmAccess()).thenReturn(access);
    }

    @Test
    public void testBuildSingleRealm() throws ServletException {
        String[] roles = new String[]{"a-realm"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);

        Set<String> realms = realmAuthorizer.getRealmsWithAction("a");
        assertTrue(realms.contains("realm"));
    }

    @Test
    public void testBuildMultipleRealms() throws ServletException {
        String[] roles = new String[]{"a-realm", "b-another"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);

        Set<String> realms = realmAuthorizer.getRealmsWithAction("a");
        assertTrue(realms.contains("realm"));
        assertFalse(realms.contains("another"));

        realms = realmAuthorizer.getRealmsWithAction("b");
        assertTrue(realms.contains("another"));
        assertFalse(realms.contains("realm"));
    }

    @Test
    public void testBuildMultipleRealmsSameAction() throws ServletException {
        String[] roles = new String[]{"r-realm", "w-realm"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);


        Set<String> realms = realmAuthorizer.getRealmsWithAction("r");
        assertTrue(realms.contains("realm"));

        realms = realmAuthorizer.getRealmsWithAction("w");
        assertTrue(realms.contains("realm"));
    }

    @Test
    public void testBuildRoleWithoutRealm() throws ServletException {
        String[] roles = new String[]{"a-"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        Set<Role> realms = realmAuthorizer.getAllRoles();
        assertTrue(realms.isEmpty());
    }

    @Test
    public void testBuildInvalidRoleWithoutAction() throws ServletException {
        String[] roles = new String[]{"-realm"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        Set<Role> realms = realmAuthorizer.getAllRoles();
        assertTrue(realms.isEmpty());
    }


    private void setupRealms() {
        String[] roles = new String[]{"r-read", "w-write", "d-delete", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));
    }

    @Test
    public void testReadable() throws ServletException {
        setupRealms();

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertTrue(realmAuthorizer.readable());

        Set<String> realms = realmAuthorizer.getReadableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("read"));
    }

    @Test
    public void testWritable() throws ServletException {
        setupRealms();

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertTrue(realmAuthorizer.writable());

        Set<String> realms = realmAuthorizer.getWritableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("write"));
    }

    @Test
    public void testUpdatable() throws ServletException {
        setupRealms();

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertTrue(realmAuthorizer.updatable());

        Set<String> realms = realmAuthorizer.getUpdatableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("update"));
    }

    @Test
    public void testDeletable() throws ServletException {
        setupRealms();

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertTrue(realmAuthorizer.deletable());

        Set<String> realms = realmAuthorizer.getDeletableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("delete"));
    }

    @Test
    public void testNotReadable() throws ServletException {
        String[] roles = new String[]{"w-write", "d-delete", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertFalse(realmAuthorizer.readable());

        Set<String> realms = realmAuthorizer.getReadableRealms();
        assertEquals(0, realms.size());
    }

    @Test
    public void testNotWritable() throws ServletException {
        String[] roles = new String[]{"r-read", "d-delete", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertFalse(realmAuthorizer.writable());

        Set<String> realms = realmAuthorizer.getWritableRealms();
        assertEquals(0, realms.size());
    }

    @Test
    public void testNotUpdatable() throws ServletException {
        String[] roles = new String[]{"w-write", "d-delete", "r-read"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertFalse(realmAuthorizer.updatable());

        Set<String> realms = realmAuthorizer.getUpdatableRealms();
        assertEquals(0, realms.size());
    }

    @Test
    public void testNotDeletable() throws ServletException {
        String[] roles = new String[]{"w-write", "r-read", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertFalse(realmAuthorizer.deletable());

        Set<String> realms = realmAuthorizer.getDeletableRealms();
        assertEquals(0, realms.size());
    }

    @Test
    public void testRealmsHeaderSubset() throws ServletException {
        String[] roles = new String[]{"w-write", "r-read", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        when(request.getHeader(eq("X-Thermostat-Realms"))).thenReturn("read,update");

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertEquals(1, realmAuthorizer.getReadableRealms().size());
        assertEquals(1, realmAuthorizer.getUpdatableRealms().size());

        assertEquals(0, realmAuthorizer.getWritableRealms().size());
        assertEquals(0, realmAuthorizer.getDeletableRealms().size());
    }

    @Test (expected = ServletException.class)
    public void testRealmsHeaderSuperset() throws ServletException {
        String[] roles = new String[]{"r-read,","u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        when(request.getHeader(eq("X-Thermostat-Realms"))).thenReturn("read,update,other");

        new RealmAuthorizer(request);
    }

    @Test
    public void testRealmsHeaderWhitespace() throws ServletException {
        String[] roles = new String[]{"w-write", "r-read", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        when(request.getHeader(eq("X-Thermostat-Realms"))).thenReturn("  read,  update , write");

        RealmAuthorizer realmAuthorizer = new RealmAuthorizer(request);
        assertEquals(1, realmAuthorizer.getReadableRealms().size());
        assertEquals(1, realmAuthorizer.getUpdatableRealms().size());
        assertEquals(1, realmAuthorizer.getWritableRealms().size());

        assertEquals(0, realmAuthorizer.getDeletableRealms().size());
    }

}
