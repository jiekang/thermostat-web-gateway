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

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class RoleFactoryTest {

    private RoleFactory roleFactory;

    @Before
    public void setup() {
        roleFactory = new RoleFactory();
    }

    @Test
    public void testValidRole() throws InvalidRoleException {
        String role = "a-valid,role";
        roleFactory.buildRole(role);

        Set<String> actions = new HashSet<>();
        actions.add("a");
        Role r = roleFactory.buildRole(role);
        verifyRole(r, actions, "valid,role");
    }

    @Test
    public void testValidRoleWithActions() throws InvalidRoleException {
        String role = "r,w,d-role";

        Role r = roleFactory.buildRole(role);
        Set<String> actions = new HashSet<>();
        actions.add("r");
        actions.add("w");
        actions.add("d");
        verifyRole(r, actions, "role");
    }

    @Test(expected = InvalidRoleException.class)
    public void testNoActionRole() throws InvalidRoleException {
        String role = "-role";
        roleFactory.buildRole(role);
    }

    @Test(expected = InvalidRoleException.class)
    public void testNoRealmRole() throws InvalidRoleException {
        String role = "a-";
        roleFactory.buildRole(role);
    }

    @Test
    public void testHyphenRealm() throws InvalidRoleException {
        String role = "a-realm-with-hyphens";

        Role r = roleFactory.buildRole(role);
        Set<String> actions = new HashSet<>();
        actions.add("a");
        verifyRole(r, actions, "realm-with-hyphens");
    }

    @Test(expected = InvalidRoleException.class)
    public void testRealmWithWhitespaceIsInvalid() throws InvalidRoleException {
        String role = "a-invalid \trealm";
        roleFactory.buildRole(role);
    }

    @Test
    public void testRealmWithLeadingWhitespace() throws InvalidRoleException {
        String role = " a-role";

        Role r = roleFactory.buildRole(role);
        Set<String> actions = new HashSet<>();
        actions.add("a");
        verifyRole(r, actions, "role");
    }

    @Test
    public void testRealmWithTrailingWhitespace() throws InvalidRoleException {
        String role = "a-role\t";

        Role r = roleFactory.buildRole(role);
        Set<String> actions = new HashSet<>();
        actions.add("a");
        verifyRole(r, actions, "role");
    }

    private void verifyRole(Role role, Set<String> expectedActions, String expectedRole) {
        for (String item : expectedActions) {
            assertTrue(role.containsAction(item));
        }
        assertEquals(expectedRole, role.getRealm());
    }
}
