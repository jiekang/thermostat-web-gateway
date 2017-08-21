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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakSecurityContext;

import com.redhat.thermostat.gateway.common.core.auth.InvalidRoleException;
import com.redhat.thermostat.gateway.common.core.auth.RealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.auth.Role;
import com.redhat.thermostat.gateway.common.core.auth.RoleFactory;

public class KeycloakRealmAuthorizer extends RealmAuthorizer {

    public static final String REALMS_HEADER = "X-Thermostat-Realms";
    private static final String REALMS_HEADER_DELIMITER_REGEX = "\\s+";

    private final RoleFactory roleFactory = new RoleFactory();

    public KeycloakRealmAuthorizer(HttpServletRequest httpServletRequest) throws ServletException {
        this.clientRoles = buildClientRoles(httpServletRequest);
    }

    /**
     * Package private for testing
     */
    Set<Role> getAllRoles() {
        return clientRoles;
    }

    private Set<Role> buildClientRoles(HttpServletRequest httpServletRequest) throws ServletException {
        Set<Role> keycloakRoles = buildKeycloakRoles(httpServletRequest);

        String realmsHeader = httpServletRequest.getHeader(REALMS_HEADER);
        if (realmsHeader != null) {
            return buildClientPreferredRoles(keycloakRoles, realmsHeader);
        }

        return Collections.unmodifiableSet(keycloakRoles);
    }

    /**
     * @return the set of roles from the Keycloak security token
     */
    private Set<Role> buildKeycloakRoles(HttpServletRequest httpServletRequest) {
        Set<Role> keycloakRoles = new HashSet<>();

        KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) httpServletRequest
                .getAttribute(KeycloakSecurityContext.class.getName());

        for (String role : keycloakSecurityContext.getToken().getRealmAccess().getRoles()) {
            try {
                keycloakRoles.add(roleFactory.buildRole(role));
            } catch (InvalidRoleException e) {
                //Do nothing
            }
        }

        return keycloakRoles;
    }

    /**
     * Builds a set of roles based on a clients preferred set, provided in a comma separated realms header string
     * @param trustedRoles : The trusted set of roles that the client has
     * @param realmsHeader : The REALMS_HEADER value as a string
     * @return The set of roles that the client has selected
     * @throws ServletException If realms header contains realms the client does not have or no valid realms
     */
    private Set<Role> buildClientPreferredRoles(Set<Role> trustedRoles, String realmsHeader) throws ServletException {
        realmsHeader = realmsHeader.trim();
        Set<String> preferredRealms = new HashSet<>(Arrays.asList(realmsHeader.split(REALMS_HEADER_DELIMITER_REGEX)));
        Set<Role> selectedRoles = new HashSet<>();

        for (String preferredRealm : preferredRealms) {
            boolean found = false;
            for (Role role : trustedRoles) {
                if (role.getRealm().equals(preferredRealm)) {
                    selectedRoles.add(role);
                    found = true;
                }
            }
            if (!found) {
                throw new ServletException("Not authorized to access preferred realms.");
            }
        }

        if (selectedRoles.size() > 0) {
            return selectedRoles;
        } else {
            throw new ServletException("No realms selected");
        }
    }


}
