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

package com.redhat.thermostat.gateway.tests.utils.keycloak;

public enum DefaultKeycloakUsers {

    USER_A("tms-a"),
    USER_B("tms-b"),
    USER_C("tms-c"),
    USER_D("tms-d"),
    USER_E("tms-e"),
    USER_F("tms-f"),
    USER_G("tms-g"),

    ;

    private final String username;

    DefaultKeycloakUsers(String username) {
        this.username = username;
    }

    public KeycloakUserCredentials getCredentials() {
        String defaultUrl = "http://127.0.0.1:31000";
        String defaultRealm = "thermostat";
        String defaultClient = "thermostat-web-client";
        String defaultPassword = "tms-pass";
        return new KeycloakUserCredentials(defaultUrl, defaultRealm, defaultClient, username, defaultPassword);
    }
}
