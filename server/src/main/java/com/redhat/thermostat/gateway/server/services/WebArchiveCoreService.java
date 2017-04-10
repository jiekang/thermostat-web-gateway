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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.redhat.thermostat.gateway.common.core.Configuration;
import com.redhat.thermostat.gateway.common.core.ServiceConfiguration;
import com.redhat.thermostat.gateway.server.auth.basic.BasicLoginService;
import com.redhat.thermostat.gateway.server.auth.basic.BasicUserStore;

class WebArchiveCoreService implements CoreService {

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
        initializeWebSockets(server, webAppContext);
        setupBasicAuthForContext(server, webAppContext);
        return webAppContext;
    }

    private void setupBasicAuthForContext(Server server, WebAppContext webAppContext) {
        if (isSet(ServiceConfiguration.ConfigurationKey.SECURITY_BASIC)) {
            Map<String, String> userConfig = getBasicAuthUserConfig();
            SecurityHandler security = webAppContext.getSecurityHandler();
            BasicUserStore userStore = new BasicUserStore(userConfig);
            LoginService loginService = new BasicLoginService(userStore, security.getRealmName());
            security.setLoginService(loginService);
        }
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
