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

package com.redhat.thermostat.gateway.server.auth.keycloak;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KeycloakConfigurationTest {

    private static final String expectedKeycloakJson = "{\n" +
            "  \"realm\": \"thermostat\",\n" +
            "  \"bearer-only\": true,\n" +
            "  \"auth-server-url\": \"http://172.17.0.2:8080/auth\",\n" +
            "  \"ssl-required\": \"external\",\n" +
            "  \"resource\": \"thermostat-bearer\"\n" +
            "}";
    private static final String validJsonNoRealm = "{\n" +
            "  \"bearer-only\": true,\n" +
            "  \"auth-server-url\": \"http://172.17.0.2:8080/auth\",\n" +
            "  \"ssl-required\": \"external\",\n" +
            "  \"resource\": \"thermostat-bearer\"\n" +
            "}";

    private static final KeycloakConfigurationFactory keycloakFactory = new KeycloakConfigurationFactory();

    @Test
    public void testValidKeycloakJson() {
        keycloakFactory.createKeycloakConfiguration(expectedKeycloakJson);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidJson() {
        keycloakFactory.createKeycloakConfiguration("{\"}");
    }

    @Test
    public void testEmptyJson() {
        KeycloakConfiguration kc = keycloakFactory.createKeycloakConfiguration("");
        assertEquals(null, kc);
    }

    @Test
    public void testGetRealm() {
        KeycloakConfiguration configuration = keycloakFactory.createKeycloakConfiguration(expectedKeycloakJson);

        String expectedRealm = "thermostat";
        assertEquals(expectedRealm, configuration.getRealm());
    }

    @Test
    public void testMissingGetRealm() {
        KeycloakConfiguration configuration = keycloakFactory.createKeycloakConfiguration(validJsonNoRealm);

        assertEquals(null, configuration.getRealm());
    }
}
