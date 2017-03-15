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

package com.redhat.thermostat.server.core.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;

import com.redhat.thermostat.server.core.internal.web.configuration.ServerConfiguration;
import com.redhat.thermostat.server.core.internal.web.http.handlers.CommandChannelAgentEndpointHandler;
import com.redhat.thermostat.server.core.internal.web.http.handlers.CommandChannelClientEndpointHandler;
import com.redhat.thermostat.server.core.internal.web.http.handlers.HtmlResourceHandler;
import com.redhat.thermostat.server.core.internal.web.security.authentication.basic.BasicAuthFilter;
import com.redhat.thermostat.server.core.internal.web.security.authentication.basic.BasicLoginService;
import com.redhat.thermostat.server.core.internal.web.security.authentication.basic.BasicUserStore;
import com.redhat.thermostat.server.core.internal.web.security.authentication.none.NoAuthFilter;
import com.redhat.thermostat.server.core.internal.web.security.authentication.proxy.ProxyAuthFilter;
import com.redhat.thermostat.server.core.internal.web.security.authorization.RoleAuthFilter;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

public class CoreServer {
    private Server server;
    private int port = 26000;

    public void buildServer(Map<String, String> serverConfig, Map<String, String> mongoConfig, Map<String, String> userConfig) {
        ResourceConfig resourceConfig = new ResourceConfig();
        setupResourceConfig(serverConfig, userConfig, resourceConfig);

        // Get the jersey servlet
        ServletHolder servlet = new ServletHolder(new ServletContainer(resourceConfig));

        server = new Server();
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SECURITY|ServletContextHandler.SESSIONS);
        context.setServer(server);
        context.setContextPath("/*");
        context.addServlet(servlet, "/*");
        server.setHandler(context);
        try {
            // Initialize javax.websocket layer
            ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);

            // Add WebSocket endpoint(s) to javax.websocket layer
            wscontainer.addEndpoint(CommandChannelClientEndpointHandler.class);
            wscontainer.addEndpoint(CommandChannelAgentEndpointHandler.class);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        setupConnectors(serverConfig, userConfig);
    }

    private void setupResourceConfig(Map<String, String> serverConfig, Map<String, String> userConfig, ResourceConfig resourceConfig) {
        resourceConfig.register(new HtmlResourceHandler());
    	if (serverConfig.containsKey(ServerConfiguration.SECURITY_PROXY.toString()) &&
                serverConfig.get(ServerConfiguration.SECURITY_PROXY.toString()).equals("true")) {
            resourceConfig.register(new ProxyAuthFilter());
        } else if (serverConfig.containsKey(ServerConfiguration.SECURITY_BASIC.toString()) &&
                serverConfig.get(ServerConfiguration.SECURITY_BASIC.toString()).equals("true")) {
            resourceConfig.register(new BasicAuthFilter(new BasicUserStore(userConfig)));
        } else {
            resourceConfig.register(new NoAuthFilter());
        }

        if (serverConfig.containsKey(ServerConfiguration.SWAGGER_UI_ENABLED.toString()) &&
                serverConfig.get(ServerConfiguration.SWAGGER_UI_ENABLED.toString()).equals("true")) {
            // generate swagger.json automatically
            BeanConfig beanConfig = new BeanConfig();
            beanConfig.setBasePath("/");
            beanConfig.setVersion("1.0.0");
            beanConfig.setTitle("Thermostat Web API");
            beanConfig.setLicense("GPL v2 with Classpath Exception");
            beanConfig.setLicenseUrl("http://www.gnu.org/licenses");
            // scan for JAX-RS classes from this package
            beanConfig.setResourcePackage("com.redhat.thermostat.server.core.internal.web.http");
            beanConfig.setScan(true);

            resourceConfig.register(new ApiListingResource());
            resourceConfig.register(new SwaggerSerializers());
        }

        resourceConfig.register(new RolesAllowedDynamicFeature());
        resourceConfig.register(new RoleAuthFilter());
    }

    private void setupConnectors(Map<String, String> serverConfig, Map<String, String> userConfig) {
        server.setConnectors(new Connector[]{});
        ServerConnector httpConnector = new ServerConnector(server);

        if (serverConfig.containsKey(ServerConfiguration.SECURITY_PROXY.toString()) &&
                serverConfig.get(ServerConfiguration.SECURITY_PROXY.toString()).equals("true")) {
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());

            httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));
        } else if (serverConfig.containsKey(ServerConfiguration.SECURITY_BASIC.toString()) &&
                serverConfig.get(ServerConfiguration.SECURITY_BASIC.toString()).equals("true")) {
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));
            // FIXME: Filter for websockets does not seem to be called.
            //        Set up basic auth directly using jetty API
            Handler original = server.getHandler();
            ConstraintSecurityHandler security = new ConstraintSecurityHandler();
            Constraint cons = new Constraint("Thermostat Realm", "thermostat-realm");
            cons.setAuthenticate(true);
            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setConstraint(cons);
            mapping.setMethod("Basic");
            mapping.setPathSpec("/*");
            security.setConstraintMappings(Collections.singletonList(mapping));
            security.setAuthenticator(new BasicAuthenticator());
            security.setLoginService(new BasicLoginService(new BasicUserStore(userConfig)));
            security.setHandler(original);
            server.setHandler(security);
        } else {
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));
        }

        try {
            URL url = new URL(serverConfig.get(ServerConfiguration.URL.toString()));
            httpConnector.setHost(url.getHost());
            port = url.getPort();
            httpConnector.setPort(port);
        } catch (MalformedURLException e) {
            httpConnector.setHost("localhost");
            httpConnector.setPort(port);
        }

        httpConnector.setIdleTimeout(30000);

        server.addConnector(httpConnector);
    }

    public Server getServer() {
        return server;
    }

    public void finish() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }
}
