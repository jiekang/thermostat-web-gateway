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

import java.util.Map;

import javax.servlet.ServletException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration;
import com.redhat.thermostat.gateway.server.apidoc.SwaggerUiHandler;
import com.redhat.thermostat.gateway.server.services.CoreService;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilder;

public class CoreServerBuilder {

    private Server server = new Server();
    private CoreServiceBuilder coreServiceBuilder;
    private Configuration serverConfig;

    public CoreServerBuilder setServiceBuilder(CoreServiceBuilder builder) {
        this.coreServiceBuilder = builder;
        return this;
    }

    public CoreServerBuilder setServerConfiguration(Configuration config) {
        this.serverConfig = config;
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

        // Set up Swagger UI-based API doc handler together with the service handlers.
        // It'll be able to generate API docs based on any URL accessible
        // swagger spec (swagger.json/swagger.yaml). Hence, we set this up
        // once in the deploying server and make the swagger spec available
        // in the services themselves
        ContextHandler swaggerHandler = new SwaggerUiHandler().createSwaggerResourceHandler();
        contextHandlerCollection.addHandler(swaggerHandler);
        server.setHandler(contextHandlerCollection);
    }

    private void setupConnector() {
        ServerConnector httpConnector = new ServerConnector(server);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));

        Map<String, Object> serverConfigMap = serverConfig.asMap();
        String listenAddress = (String)serverConfigMap.get(GlobalConfiguration.ConfigurationKey.IP.toString());
        int listenPort = Integer.parseInt((String)serverConfigMap.get(GlobalConfiguration.ConfigurationKey.PORT.toString()));

        httpConnector.setHost(listenAddress);
        httpConnector.setPort(listenPort);

        server.setConnectors(new Connector[]{httpConnector});
    }

}
