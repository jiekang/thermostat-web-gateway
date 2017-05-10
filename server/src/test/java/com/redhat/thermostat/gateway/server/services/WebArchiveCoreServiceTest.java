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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;
import org.keycloak.adapters.jetty.KeycloakJettyAuthenticator;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ServiceConfiguration;
import com.redhat.thermostat.gateway.server.auth.basic.BasicLoginService;

public class WebArchiveCoreServiceTest {

    private final String contextPath = "/test";
    private final String warPath = "/test.war";
    private final String keycloakJson = "{\n" +
            "  \"realm\": \"thermostat\",\n" +
            "  \"bearer-only\": true,\n" +
            "  \"auth-server-url\": \"http://172.17.0.2:8080/auth\",\n" +
            "  \"ssl-required\": \"external\",\n" +
            "  \"resource\": \"thermostat-bearer\"\n" +
            "}";

    @Test
    public void testBasicService() {
        Map<String, Object> configurationMap = new HashMap<>();

        Configuration configuration = mock(Configuration.class);
        when(configuration.asMap()).thenReturn(configurationMap);

        WebArchiveCoreService service = new WebArchiveCoreService(contextPath, warPath, configuration);

        ServletContextHandler servletContextHandler = service.createServletContextHandler(mock(Server.class));

        assertTrue(servletContextHandler instanceof WebAppContext);
        WebAppContext webAppContext = (WebAppContext) servletContextHandler;

        assertEquals(webAppContext.getContextPath(), contextPath);
        assertEquals(webAppContext.getWar(), warPath);
    }

    @Test
    public void testServiceWithKeycloakAuth() {
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(ServiceConfiguration.ConfigurationKey.SECURITY_KEYCLOAK.name(), "true");
        configurationMap.put(ServiceConfiguration.ConfigurationKey.KEYCLOAK_CONFIG.name(), keycloakJson);
        Configuration configuration = mock(Configuration.class);
        when(configuration.asMap()).thenReturn(configurationMap);

        WebArchiveCoreService service = new WebArchiveCoreService(contextPath, warPath, configuration);

        ServletContextHandler servletContextHandler = service.createServletContextHandler(mock(Server.class));

        assertTrue(servletContextHandler instanceof WebAppContext);
        WebAppContext webAppContext = (WebAppContext) servletContextHandler;

        assertEquals(webAppContext.getContextPath(), contextPath);
        assertEquals(webAppContext.getWar(), warPath);

        assertEquals(webAppContext.getInitParameter("org.keycloak.json.adapterConfig"), keycloakJson);
        assertTrue(Arrays.asList(webAppContext.getSystemClasses()).contains("org.keycloak."));

        SecurityHandler securityHandler = webAppContext.getSecurityHandler();

        assertTrue(securityHandler instanceof ConstraintSecurityHandler);
        assertTrue(securityHandler.getAuthenticator() instanceof KeycloakJettyAuthenticator);
        assertEquals(securityHandler.getRealmName(), "thermostat");
    }


    @Test
    public void testServiceWithBasicAuth() {
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(ServiceConfiguration.ConfigurationKey.SECURITY_BASIC.name(), "true");

        Map<String, String> basicUsers = new HashMap<>();
        basicUsers.put("user", "password");
        configurationMap.put(ServiceConfiguration.ConfigurationKey.SECURITY_BASIC_USERS.name(), basicUsers);

        Configuration configuration = mock(Configuration.class);
        when(configuration.asMap()).thenReturn(configurationMap);

        WebArchiveCoreService service = new WebArchiveCoreService(contextPath, warPath, configuration);

        ServletContextHandler servletContextHandler = service.createServletContextHandler(mock(Server.class));

        assertTrue(servletContextHandler instanceof WebAppContext);
        WebAppContext webAppContext = (WebAppContext) servletContextHandler;
        assertEquals(webAppContext.getContextPath(), contextPath);
        assertEquals(webAppContext.getWar(), warPath);

        SecurityHandler securityHandler = webAppContext.getSecurityHandler();
        assertTrue(securityHandler.getLoginService() instanceof BasicLoginService);
        LoginService loginService =securityHandler.getLoginService();
        UserIdentity u = loginService.login("user", "password");
        assertTrue(loginService.validate(u));
    }

    @Test
    public void testServiceWithWebSockets() {
        Map<String, Object> configurationMap = new HashMap<>();
        configurationMap.put(ServiceConfiguration.ConfigurationKey.WEBSOCKETS.name(), "true");
        Configuration configuration = mock(Configuration.class);
        when(configuration.asMap()).thenReturn(configurationMap);

        WebArchiveCoreService service = new WebArchiveCoreService(contextPath, warPath, configuration);

        Server server = mock(Server.class);
        ServletContextHandler servletContextHandler = service.createServletContextHandler(server);

        assertTrue(servletContextHandler instanceof WebAppContext);
        WebAppContext webAppContext = (WebAppContext) servletContextHandler;


        assertEquals(webAppContext.getContextPath(), contextPath);
        assertEquals(webAppContext.getWar(), warPath);

        assertEquals(webAppContext.getServer(), server);
    }
}
