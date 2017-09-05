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

package com.redhat.thermostat.gateway.service.commands.channel.endpoints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.junit.Before;
import org.junit.Test;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;

import com.redhat.thermostat.gateway.common.core.auth.RealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.auth.basic.BasicRealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.auth.basic.BasicWebUser;
import com.redhat.thermostat.gateway.common.core.auth.keycloak.KeycloakRealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ServiceConfiguration;
import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;

public class RealmAuthorizerConfiguratorTest {

    private ServerEndpointConfig config;
    private HandshakeRequest request;
    private HandshakeResponse response;
    private Configuration serviceConfig;
    private Set<String> roles;
    private Map<String, Object> userProps;

    @Before
    public void setup() {
        serviceConfig = mock(Configuration.class);
        userProps = new HashMap<>();
        userProps.put(GlobalConstants.SERVICE_CONFIG_KEY, serviceConfig);
        config = mock(ServerEndpointConfig.class);
        when(config.getUserProperties()).thenReturn(userProps);
        request = mock(HandshakeRequest.class);
        response = mock(HandshakeResponse.class);
        roles = new HashSet<>();
        roles.add("ping-commands");
        roles.add("ignored");
        roles.add("r-foo-realm");
    }

    @Test
    public void testBasicAuthAuthorizer() {
        setupConfig(ServiceConfiguration.ConfigurationKey.SECURITY_BASIC, Boolean.TRUE);
        BasicWebUser userPrincipal = new BasicWebUser("ignored", new char[] {}, roles);
        setupPrincipal(userPrincipal);

        RealmAuthorizerConfigurator configurator = new RealmAuthorizerConfigurator();
        configurator.modifyHandshake(config, request, response);

        RealmAuthorizer authorizer = (RealmAuthorizer)userProps.get(RealmAuthorizer.class.getName());
        assertNotNull(authorizer);
        assertTrue(authorizer instanceof BasicRealmAuthorizer);
        verifyRoles(authorizer);
    }

    @Test
    public void testKeycloakAuthAuthorizer() {
        setupConfig(ServiceConfiguration.ConfigurationKey.SECURITY_KEYCLOAK, Boolean.TRUE);
        KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal = setupKeycloakPrincipal();
        setupPrincipal(keycloakPrincipal);

        RealmAuthorizerConfigurator configurator = new RealmAuthorizerConfigurator();
        configurator.modifyHandshake(config, request, response);

        RealmAuthorizer authorizer = (RealmAuthorizer)userProps.get(RealmAuthorizer.class.getName());
        assertNotNull(authorizer);
        assertTrue("Actual: " + authorizer.getClass(), authorizer instanceof KeycloakRealmAuthorizer);
        verifyRoles(authorizer);
    }

    @Test
    public void testDenyAllForUnknown() {
        // empty config
        when(serviceConfig.asMap()).thenReturn(Collections.<String, Object>emptyMap());

        RealmAuthorizerConfigurator configurator = new RealmAuthorizerConfigurator();
        configurator.modifyHandshake(config, request, response);

        RealmAuthorizer authorizer = (RealmAuthorizer)userProps.get(RealmAuthorizer.class.getName());
        assertNotNull(authorizer);
        assertSame(RealmAuthorizer.DENY_ALL_AUTHORIZER, authorizer);
    }

    private KeycloakPrincipal<KeycloakSecurityContext> setupKeycloakPrincipal() {
        @SuppressWarnings("unchecked")
        KeycloakPrincipal<KeycloakSecurityContext> p = mock(KeycloakPrincipal.class);
        KeycloakSecurityContext secContext = mock(KeycloakSecurityContext.class);
        AccessToken accessToken = mock(AccessToken.class);
        Access access = mock(Access.class);
        when(access.getRoles()).thenReturn(roles);
        when(accessToken.getRealmAccess()).thenReturn(access);
        when(secContext.getToken()).thenReturn(accessToken);
        when(p.getKeycloakSecurityContext()).thenReturn(secContext);
        return p;
    }

    private void verifyRoles(RealmAuthorizer authorizer) {
        Set<String> realmPingActions = new HashSet<>();
        realmPingActions.add("commands");
        assertEquals(realmPingActions, authorizer.getRealmsWithAction("ping"));
        Set<String> readableRealms = new HashSet<>();
        readableRealms.add("foo-realm");
        assertEquals(readableRealms, authorizer.getReadableRealms());
    }

    private void setupConfig(ServiceConfiguration.ConfigurationKey key, Boolean value) {
        Map<String, Object> configImpl = new HashMap<>();
        configImpl.put(key.name(), value.toString());
        when(serviceConfig.asMap()).thenReturn(configImpl);
    }

    private void setupPrincipal(Principal p) {
        when(request.getUserPrincipal()).thenReturn(p);
    }
}
