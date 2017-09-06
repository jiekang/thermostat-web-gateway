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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;

import com.redhat.thermostat.gateway.common.core.auth.Role;
import com.redhat.thermostat.gateway.common.core.auth.keycloak.KeycloakRealmAuthorizer.CannotReduceRealmsException;

public class KeycloakRealmAuthorizerTest {

    private AccessToken.Access access;
    private KeycloakSecurityContext keycloakSecurityContext;

    @Before
    public void setup() {
        keycloakSecurityContext = mock(KeycloakSecurityContext.class);

        AccessToken accessToken = mock(AccessToken.class);
        when(keycloakSecurityContext.getToken()).thenReturn(accessToken);

        access = mock(AccessToken.Access.class);
        when(accessToken.getRealmAccess()).thenReturn(access);
    }

    @Test
    public void testBuildSingleRealm() throws CannotReduceRealmsException {
        String[] roles = new String[]{"a-realm"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);

        Set<String> realms = realmAuthorizer.getRealmsWithAction("a");
        assertTrue(realms.contains("realm"));
    }

    @Test
    public void testBuildMultipleRealms() throws CannotReduceRealmsException {
        String[] roles = new String[]{"a-realm", "b-another"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);

        Set<String> realms = realmAuthorizer.getRealmsWithAction("a");
        assertTrue(realms.contains("realm"));
        assertFalse(realms.contains("another"));

        realms = realmAuthorizer.getRealmsWithAction("b");
        assertTrue(realms.contains("another"));
        assertFalse(realms.contains("realm"));
    }

    @Test
    public void testBuildMultipleRealmsSameAction() throws CannotReduceRealmsException {
        String[] roles = new String[]{"r-realm", "w-realm"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);


        Set<String> realms = realmAuthorizer.getRealmsWithAction("r");
        assertTrue(realms.contains("realm"));

        realms = realmAuthorizer.getRealmsWithAction("w");
        assertTrue(realms.contains("realm"));
    }

    @Test
    public void testBuildRoleWithoutRealm() throws CannotReduceRealmsException {
        String[] roles = new String[]{"a-"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        Set<Role> realms = realmAuthorizer.getAllRoles();
        assertTrue(realms.isEmpty());
    }

    @Test
    public void testBuildInvalidRoleWithoutAction() throws CannotReduceRealmsException {
        String[] roles = new String[]{"-realm"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        Set<Role> realms = realmAuthorizer.getAllRoles();
        assertTrue(realms.isEmpty());
    }


    private void setupRealms() {
        String[] roles = new String[]{"r-read", "w-write", "d-delete", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));
    }

    @Test
    public void testReadable() throws CannotReduceRealmsException {
        setupRealms();

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertTrue(realmAuthorizer.readable());

        Set<String> realms = realmAuthorizer.getReadableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("read"));
    }

    @Test
    public void testWritable() throws CannotReduceRealmsException {
        setupRealms();

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertTrue(realmAuthorizer.writable());

        Set<String> realms = realmAuthorizer.getWritableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("write"));
    }

    @Test
    public void testUpdatable() throws CannotReduceRealmsException {
        setupRealms();

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertTrue(realmAuthorizer.updatable());

        Set<String> realms = realmAuthorizer.getUpdatableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("update"));
    }

    @Test
    public void testDeletable() throws CannotReduceRealmsException {
        setupRealms();

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertTrue(realmAuthorizer.deletable());

        Set<String> realms = realmAuthorizer.getDeletableRealms();
        assertEquals(1, realms.size());
        assertTrue(realms.contains("delete"));
    }

    @Test
    public void testNotReadable() throws CannotReduceRealmsException {
        String[] roles = new String[]{"w-write", "d-delete", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertFalse(realmAuthorizer.readable());

        Set<String> realms = realmAuthorizer.getReadableRealms();
        assertEquals(0, realms.size());
    }

    @Test
    public void testNotWritable() throws CannotReduceRealmsException {
        String[] roles = new String[]{"r-read", "d-delete", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertFalse(realmAuthorizer.writable());

        Set<String> realms = realmAuthorizer.getWritableRealms();
        assertEquals(0, realms.size());
    }

    @Test
    public void testNotUpdatable() throws CannotReduceRealmsException {
        String[] roles = new String[]{"w-write", "d-delete", "r-read"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertFalse(realmAuthorizer.updatable());

        Set<String> realms = realmAuthorizer.getUpdatableRealms();
        assertEquals(0, realms.size());
    }

    @Test
    public void testNotDeletable() throws CannotReduceRealmsException {
        String[] roles = new String[]{"w-write", "r-read", "u-update"};
        when(access.getRoles()).thenReturn(new HashSet<>(Arrays.asList(roles)));

        KeycloakRealmAuthorizer realmAuthorizer = new KeycloakRealmAuthorizer(keycloakSecurityContext);
        assertFalse(realmAuthorizer.deletable());

        Set<String> realms = realmAuthorizer.getDeletableRealms();
        assertEquals(0, realms.size());
    }

}
