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

package com.redhat.thermostat.gateway.server.services;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.keycloak.adapters.jetty.KeycloakJettyAuthenticator;

import com.redhat.thermostat.gateway.common.core.auth.RealmAuthorizer;
import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration;
import com.redhat.thermostat.gateway.common.core.config.IllegalConfigurationException;
import com.redhat.thermostat.gateway.common.core.config.ServiceConfiguration;
import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;
import com.redhat.thermostat.gateway.server.auth.basic.BasicAuthFilter;
import com.redhat.thermostat.gateway.server.auth.basic.BasicLoginService;
import com.redhat.thermostat.gateway.server.auth.basic.BasicUserStore;
import com.redhat.thermostat.gateway.server.auth.keycloak.KeycloakConfiguration;
import com.redhat.thermostat.gateway.server.auth.keycloak.KeycloakConfigurationFactory;
import com.redhat.thermostat.gateway.server.auth.keycloak.KeycloakRequestFilter;

class WebArchiveCoreService implements CoreService {

    // Users need to be in at least this role for authentication to succeed.
    // This applies for keycloak and basic.
    private static final String AUTH_ACCESS_ROLE = "thermostat";
    private final String contextPath;
    private final String warPath;
    private final Configuration serviceConfig;

    WebArchiveCoreService(String contextPath, String warPath, Configuration serviceConfig) {
        this.contextPath = contextPath;
        this.warPath = warPath;
        this.serviceConfig = serviceConfig;
    }

    @Override
    public ServletContextHandler createServletContextHandler(Server server) {
        WebAppContext webAppContext = new WebAppContext();

        webAppContext.setContextPath(contextPath);
        webAppContext.setWar(warPath);

        webAppContext.setAttribute(GlobalConstants.SERVICE_CONFIG_KEY, serviceConfig);
        webAppContext.addSystemClass(Configuration.class.getName());
        webAppContext.addSystemClass(RealmAuthorizer.class.getName());


        initializeWebSockets(server, webAppContext);

        setupAuthForContext(webAppContext);

        return webAppContext;
    }


    private void setupAuthForContext(WebAppContext webAppContext) throws IllegalConfigurationException {
        // Check Keycloak first as it has higher priority. Only one auth scheme allowed
        if (isSet(ServiceConfiguration.ConfigurationKey.SECURITY_KEYCLOAK)) {
            setupKeycloakAuthForContext(webAppContext);
        } else if (isSet(ServiceConfiguration.ConfigurationKey.SECURITY_BASIC)) {
            setupBasicAuthForContext(webAppContext);
        } else {
            throw new IllegalConfigurationException("No auth scheme specified.");
        }
    }

    private void setupBasicAuthForContext(WebAppContext webAppContext) {
        Map<String, String> userConfig = getBasicAuthUserConfig();
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        String realmName = "Thermostat Realm"; // must match name of login service
        Constraint cons = new Constraint();
        cons.setName(realmName);
        cons.setRoles(new String[] { AUTH_ACCESS_ROLE });
        cons.setAuthenticate(true);
        boolean isTLS = Boolean.parseBoolean((String)serviceConfig.asMap().get(GlobalConfiguration.ConfigurationKey.WITH_TLS.name()));
        if (isTLS) {
            cons.setDataConstraint(Constraint.DC_CONFIDENTIAL);
        }
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(cons);
        mapping.setMethodOmissions(new String[] {});
        mapping.setPathSpec("/*");
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(new BasicLoginService(new BasicUserStore(userConfig), realmName));
        webAppContext.setSecurityHandler(security);

        // Add the realm authorizer
        FilterHolder filterHolder = new FilterHolder(new BasicAuthFilter());
        webAppContext.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    private void setupKeycloakAuthForContext(WebAppContext webAppContext) {
        String keycloakConfig = (String) serviceConfig.asMap().get(ServiceConfiguration.ConfigurationKey.KEYCLOAK_CONFIG.name());
        KeycloakConfiguration keycloakConfiguration = new KeycloakConfigurationFactory().createKeycloakConfiguration(keycloakConfig);
        String realm = keycloakConfiguration.getRealm();

        KeycloakJettyAuthenticator authenticator = new KeycloakJettyAuthenticator();
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(authenticator);
        securityHandler.setAuthMethod("BASIC");
        securityHandler.setRealmName(realm);

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{ AUTH_ACCESS_ROLE });
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        securityHandler.addConstraintMapping(constraintMapping);

        webAppContext.setInitParameter("org.keycloak.json.adapterConfig", keycloakConfig);
        webAppContext.setSecurityHandler(securityHandler);
        webAppContext.addSystemClass("org.keycloak.");

        webAppContext.addFilter(KeycloakRequestFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getBasicAuthUserConfig() {
        if (serviceConfig.asMap().containsKey(ServiceConfiguration.ConfigurationKey.SECURITY_BASIC_USERS.name())) {
            Map<String, Object> rawConfig = (Map<String, Object>)serviceConfig.asMap().get(ServiceConfiguration.ConfigurationKey.SECURITY_BASIC_USERS.name());
            Map<String, String> userConfig = new HashMap<>();
            for (Entry<String, Object> entry: rawConfig.entrySet()) {
                userConfig.put(entry.getKey(), (String)entry.getValue());
            }
            return userConfig;
        } else {
            return Collections.emptyMap();
        }
    }

    private void initializeWebSockets(Server server, ServletContextHandler contextHandler) {
        if (isSet(ServiceConfiguration.ConfigurationKey.WEBSOCKETS)) {
            try {
                contextHandler.setServer(server);
                WebSocketServerContainerInitializer.configureContext(contextHandler);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isSet(ServiceConfiguration.ConfigurationKey configKey) {
        Map<String, Object> map = serviceConfig.asMap();
        return Boolean.parseBoolean((String)map.get(configKey.name()));
    }

}
