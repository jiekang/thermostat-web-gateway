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

import java.util.Map;
import java.util.logging.Logger;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

import com.redhat.thermostat.gateway.common.core.auth.RealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.auth.basic.BasicRealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.auth.basic.BasicWebUser;
import com.redhat.thermostat.gateway.common.core.auth.keycloak.KeycloakRealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.auth.keycloak.KeycloakRealmAuthorizer.CannotReduceRealmsException;
import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ServiceConfiguration;
import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;
import com.redhat.thermostat.gateway.common.util.LoggingUtil;

public class RealmAuthorizerConfigurator extends Configurator {

    private static final Logger LOGGER = LoggingUtil.getLogger(RealmAuthorizerConfigurator.class);

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        Configuration serviceConfig = (Configuration)config.getUserProperties().get(GlobalConstants.SERVICE_CONFIG_KEY);

        RealmAuthorizer realmAuthorizer = RealmAuthorizer.DENY_ALL_AUTHORIZER; // Default to deny all
        if (isKeycloakAuthEnabled(serviceConfig)) {
            /* The keycloak authentication handler (a.k.a its Jetty adapter) looks for
             * credentials/tokens in the following way:
             *
             * 1. Look at the HTTP headers => Authorization: Bearer <token>
             * 2. Look if a query parameter 'access_token' exists and is a valid token
             * 3. Use Basic auth credentials so as to get a token, which is then used
             *
             * In the web-sockets case we expect for the user to have specified the token
             * via the query parameter. Thus, once we arrive here we have a valid principal
             * which in turn gives us access to the keycloak security context. We use that,
             * in turn, to set up the appropriate realm authorizer.
             */
            @SuppressWarnings({ "rawtypes", "unchecked" })
            KeycloakPrincipal<KeycloakSecurityContext> keycloakPrincipal = (KeycloakPrincipal)request.getUserPrincipal();
            if (keycloakPrincipal != null) {
                KeycloakSecurityContext secContext = keycloakPrincipal.getKeycloakSecurityContext();
                try {
                    realmAuthorizer = new KeycloakRealmAuthorizer(secContext);
                    LOGGER.config("Using Keycloak realm authorizer");
                } catch (CannotReduceRealmsException e) {
                    LOGGER.warning(e.getMessage());
                }
            }
        } else if (isBasicAuthEnabled(serviceConfig)) {
            BasicWebUser user = (BasicWebUser)request.getUserPrincipal();
            if (user != null) {
                realmAuthorizer = new BasicRealmAuthorizer(user);
                LOGGER.config("Using BASIC auth realm authorizer");
            }
        }
        config.getUserProperties().put(RealmAuthorizer.class.getName(), realmAuthorizer);
    }

    private boolean isBasicAuthEnabled(Configuration serviceConfig) {
        return isSet(serviceConfig, ServiceConfiguration.ConfigurationKey.SECURITY_BASIC);
    }

    private boolean isKeycloakAuthEnabled(Configuration serviceConfig) {
        return isSet(serviceConfig, ServiceConfiguration.ConfigurationKey.SECURITY_KEYCLOAK);
    }

    private boolean isSet(Configuration serviceConfig, ServiceConfiguration.ConfigurationKey configKey) {
        Map<String, Object> map = serviceConfig.asMap();
        return Boolean.parseBoolean((String)map.get(configKey.name()));
    }
}
