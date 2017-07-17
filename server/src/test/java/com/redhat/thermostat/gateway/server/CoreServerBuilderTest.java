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

package com.redhat.thermostat.gateway.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration;
import com.redhat.thermostat.gateway.server.apidoc.SwaggerUiHandler;
import com.redhat.thermostat.gateway.server.services.CoreService;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilder;
import com.redhat.thermostat.gateway.server.webclient.StaticAssetsHandler;
import com.redhat.thermostat.gateway.server.webclient.StaticAssetsHandler.EnvHelper;
import com.redhat.thermostat.gateway.server.webclient.StaticAssetsHandlerFactory;

public class CoreServerBuilderTest {

    /**
     * Tests building without optional handlers, like swagger-ui or web-client
     * static resources.
     */
    @Test
    public void testBuildNoDefaultHandlers() {
        CoreServiceBuilder serviceBuilder = getMockServiceBuilder();

        Map<String, Object> configMap = new HashMap<>();
        String ip = "127.0.0.1";
        String port = "8080";
        configMap.put(GlobalConfiguration.ConfigurationKey.IP.name(), ip);
        configMap.put(GlobalConfiguration.ConfigurationKey.PORT.name(), port);
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_SWAGGER_UI.name(), Boolean.FALSE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_WEB_CLIENT.name(), Boolean.FALSE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_TLS.name(), Boolean.FALSE.toString());
        Configuration configuration = getMockConfiguration(configMap);

        CoreServerBuilder builder = new CoreServerBuilder();
        builder.setServerConfiguration(configuration);
        builder.setServiceBuilder(serviceBuilder);

        Server server = builder.build();

        ContextHandlerCollection handler = (ContextHandlerCollection) server.getHandler();
        // Expects 1 handlers, 1 service mocked. Others disabled by config
        assertEquals(1, handler.getHandlers().length);

        ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        assertEquals(connector.getPort(), Integer.parseInt(port));
        assertEquals(connector.getHost(), ip);
    }

    /**
     * Tests building *with* optional handlers, like swagger-ui or web-client
     * static resources.
     */
    @Test
    public void testBuildWithDefaultHandlers() {
        CoreServiceBuilder serviceBuilder = getMockServiceBuilder();

        Map<String, Object> configMap = new HashMap<>();
        String ip = "127.0.0.1";
        String port = "8080";
        configMap.put(GlobalConfiguration.ConfigurationKey.IP.name(), ip);
        configMap.put(GlobalConfiguration.ConfigurationKey.PORT.name(), port);
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_SWAGGER_UI.name(), Boolean.TRUE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_WEB_CLIENT.name(), Boolean.TRUE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_TLS.name(), Boolean.FALSE.toString());
        Configuration configuration = getMockConfiguration(configMap);

        SwaggerUiHandler swaggerHandler = new SwaggerUiHandler();
        EnvHelper envHelper = mock(EnvHelper.class);
        when(envHelper.getEnv(any(String.class))).thenReturn("/fake-gw-home");
        StaticAssetsHandler staticAssetsHandler = new StaticAssetsHandlerFactory().create(envHelper);
        CoreServerBuilder builder = new CoreServerBuilder(swaggerHandler, staticAssetsHandler);
        builder.setServerConfiguration(configuration);
        builder.setServiceBuilder(serviceBuilder);

        Server server = builder.build();

        ContextHandlerCollection handler = (ContextHandlerCollection) server.getHandler();
        // Expects 3 handlers, 1 service mocked above, 1 swagger ui handler, 1 static resource handler
        assertEquals(3, handler.getHandlers().length);
    }

    @Test
    public void testHttpConnector() {
        CoreServiceBuilder serviceBuilder = getMockServiceBuilder();

        Map<String, Object> configMap = new HashMap<>();
        String ip = "127.0.0.1";
        String port = "8080";
        configMap.put(GlobalConfiguration.ConfigurationKey.IP.name(), ip);
        configMap.put(GlobalConfiguration.ConfigurationKey.PORT.name(), port);
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_SWAGGER_UI.name(), Boolean.FALSE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_WEB_CLIENT.name(), Boolean.FALSE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_TLS.name(), Boolean.FALSE.toString());
        Configuration configuration = getMockConfiguration(configMap);
        CoreServerBuilder builder = new CoreServerBuilder();
        builder.setServerConfiguration(configuration);
        builder.setServiceBuilder(serviceBuilder);

        Server server = builder.build();
        Connector[] connectors = server.getConnectors();
        assertEquals(1, connectors.length);
        assertTrue(connectors[0] instanceof ServerConnector);
        ServerConnector serverConnector = (ServerConnector)connectors[0];
        assertTrue(serverConnector.getDefaultConnectionFactory() instanceof HttpConnectionFactory);
    }

    @Test
    public void testHttpsConnectorDefaultKeystoreLocation() {
        CoreServiceBuilder serviceBuilder = getMockServiceBuilder();

        Map<String, Object> configMap = new HashMap<>();
        String ip = "127.0.0.1";
        String port = "8080";
        configMap.put(GlobalConfiguration.ConfigurationKey.IP.name(), ip);
        configMap.put(GlobalConfiguration.ConfigurationKey.PORT.name(), port);
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_SWAGGER_UI.name(), Boolean.FALSE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_WEB_CLIENT.name(), Boolean.FALSE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.WITH_TLS.name(), Boolean.TRUE.toString());
        configMap.put(GlobalConfiguration.ConfigurationKey.KEYSTORE_FILE.name(), "test_me.jks");
        Configuration configuration = getMockConfiguration(configMap);
        CoreServerBuilder builder = new CoreServerBuilder();
        builder.setServerConfiguration(configuration);
        builder.setServiceBuilder(serviceBuilder);
        builder.setGatewayHome(getTestRoot("/test_gw_home"));

        Server server = builder.build();
        Connector[] connectors = server.getConnectors();
        assertEquals(1, connectors.length);
        assertTrue(connectors[0] instanceof ServerConnector);
        ServerConnector serverConnector = (ServerConnector)connectors[0];
        assertTrue(serverConnector.getDefaultConnectionFactory() instanceof SslConnectionFactory);
    }

    private Configuration getMockConfiguration(Map<String, Object> configMap) {
        Configuration configuration = mock(Configuration.class);
        when(configuration.asMap()).thenReturn(configMap);
        return configuration;
    }

    private CoreServiceBuilder getMockServiceBuilder() {
        CoreServiceBuilder serviceBuilder = mock(CoreServiceBuilder.class);
        List<CoreService> serviceList = new ArrayList<>();
        CoreService service = mock(CoreService.class);
        ServletContextHandler servletContextHandler = mock(ServletContextHandler.class);
        when(servletContextHandler.getServer()).thenReturn(mock(Server.class));
        when(service.createServletContextHandler(any(Server.class))).thenReturn(servletContextHandler);
        serviceList.add(service);
        when(serviceBuilder.build()).thenReturn(serviceList);
        return serviceBuilder;
    }

    private String getTestRoot(String path) {
        URL rootUrl = CoreServerBuilderTest.class.getResource(path);
        return decodeFilePath(rootUrl);
    }

    private String decodeFilePath(URL url) {
        try {
            // Spaces are encoded as %20 in URLs - handle cases like that.
            // requires Java 1.7
            return Paths.get(url.toURI()).toFile().toString();
        } catch (URISyntaxException e) {
            throw new AssertionError("Syntax error in URI" + e.getMessage());
        }
    }
}
