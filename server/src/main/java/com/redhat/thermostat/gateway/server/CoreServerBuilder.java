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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.ServletException;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration.ConfigurationKey;
import com.redhat.thermostat.gateway.server.apidoc.SwaggerUiHandler;
import com.redhat.thermostat.gateway.server.services.CoreService;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilder;
import com.redhat.thermostat.gateway.server.webclient.StaticAssetsHandler;

public class CoreServerBuilder {

    private static final String THERMOSTAT_ALIAS = "thermostat";
    private final SwaggerUiHandler swaggerHandler;
    private final StaticAssetsHandler staticAssetsHandler;
    private final Server server = new Server();
    private CoreServiceBuilder coreServiceBuilder;
    private Configuration serverConfig;
    private String gatewayHome;

    public CoreServerBuilder() {
        this(new SwaggerUiHandler(), new StaticAssetsHandler());
    }

    // package-private for test-overrides
    CoreServerBuilder(SwaggerUiHandler swaggerHandler, StaticAssetsHandler staticHandler) {
        this.staticAssetsHandler = staticHandler;
        this.swaggerHandler = swaggerHandler;
    }



    public CoreServerBuilder setServiceBuilder(CoreServiceBuilder builder) {
        this.coreServiceBuilder = builder;
        return this;
    }

    public CoreServerBuilder setServerConfiguration(Configuration config) {
        this.serverConfig = config;
        return this;
    }

    public CoreServerBuilder setGatewayHome(String gatewayHome) {
        this.gatewayHome = gatewayHome;
        return this;
    }

    public Server build() {
        setupHandler();
        setupConnector();

        return server;
    }

    private void setupHandler() {
        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

        for (CoreService service: coreServiceBuilder.build()) {
            ServletContextHandler handler = service.createServletContextHandler(server);
            contextHandlerCollection.addHandler(handler);
            // Initialize javax.websocket layer
            try {
                handler.setServer(server);
                WebSocketServerContainerInitializer.configureContext(handler);
            } catch (ServletException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, Object> config = serverConfig.asMap();
        if (isEnabled(config, ConfigurationKey.WITH_SWAGGER_UI)) {
            // Set up Swagger UI-based API doc handler together with the service handlers.
            // It'll be able to generate API docs based on any URL accessible
            // swagger spec (swagger.json/swagger.yaml). Hence, we set this up
            // once in the deploying server and make the swagger spec available
            // in the services themselves
            ContextHandler swHandler = swaggerHandler.createSwaggerResourceHandler();
            contextHandlerCollection.addHandler(swHandler);
        }

        if (isEnabled(config, ConfigurationKey.WITH_WEB_CLIENT)) {
            // Set up a static resource handler which serves static assets
            // via /web-client context path
            ContextHandler webClientHandler = staticAssetsHandler.create();
            contextHandlerCollection.addHandler(webClientHandler);
        }

        server.setHandler(contextHandlerCollection);
    }

    private boolean isEnabled(Map<String, Object> config, ConfigurationKey configKey) {
        return Boolean.parseBoolean((String)config.get(configKey.name()));
    }

    private void setupConnector() {
        Map<String, Object> serverConfigMap = serverConfig.asMap();
        String listenAddress = (String)serverConfigMap.get(GlobalConfiguration.ConfigurationKey.IP.toString());
        int listenPort = Integer.parseInt((String)serverConfigMap.get(GlobalConfiguration.ConfigurationKey.PORT.toString()));
        Connector connector;
        if (isEnabled(serverConfigMap, ConfigurationKey.WITH_TLS)) {
            connector = getHttpsConnector(listenAddress, listenPort, serverConfigMap);
        } else {
            connector = getHttpConnector(listenAddress, listenPort);
        }
        server.setConnectors(new Connector[]{connector} );
    }

    private Connector getHttpsConnector(String listenAddress, int listenPort, Map<String, Object> serverConfigMap) {
        String keystoreCandidate = (String)serverConfigMap.get((GlobalConfiguration.ConfigurationKey.KEYSTORE_FILE.name()));
        Path keystoreFile = Paths.get(keystoreCandidate);
        if (!keystoreFile.isAbsolute()) {
            // resolve relative to GW home
            keystoreFile = Paths.get(gatewayHome, keystoreCandidate);
        }
        ServerConnector connector = getPreconfiguredHttpsConnector(keystoreFile.toFile());
        return configureHostPort(connector, listenAddress, listenPort);
    }

    private Connector getHttpConnector(String listenAddress, int listenPort) {
        ServerConnector connector = getPreconfiguredHttpConnector();
        return configureHostPort(connector, listenAddress, listenPort);
    }

    private ServerConnector configureHostPort(ServerConnector connector, String listenAddress, int listenPort) {
        connector.setPort(listenPort);
        connector.setHost(listenAddress);
        return connector;
    }

    private ServerConnector getPreconfiguredHttpConnector() {
        ServerConnector httpConnector = new ServerConnector(server);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));
        return httpConnector;
    }

    private ServerConnector getPreconfiguredHttpsConnector(File keystoreFile) {
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
        // May be overridden by setting the property -Dorg.eclipse.jetty.ssl.password=<password>
        sslContextFactory.setKeyStorePassword("OBF:1sot1v961saj1v9i1v941sar1v9g1sox");
        // May be overridden by setting the property -Dorg.eclipse.jetty.ssl.keypassword=<password>
        sslContextFactory.setKeyManagerPassword("OBF:1sot1v961saj1v9i1v941sar1v9g1sox");
        sslContextFactory.setCertAlias(THERMOSTAT_ALIAS);

        HttpConfiguration httpsConfig = new HttpConfiguration();
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(8443);
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        httpsConfig.addCustomizer(src);

        ServerConnector https = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory,
                        HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));
        return https;
    }

}
