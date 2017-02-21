package com.redhat.thermostat.server.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletContainer;

import com.redhat.thermostat.server.core.internal.configuration.ServerConfiguration;
import com.redhat.thermostat.server.core.internal.security.UserStore;
import com.redhat.thermostat.server.core.internal.security.auth.basic.BasicAuthFilter;
import com.redhat.thermostat.server.core.internal.security.auth.none.NoAuthFilter;
import com.redhat.thermostat.server.core.internal.security.auth.proxy.ProxyAuthFilter;
import com.redhat.thermostat.server.core.internal.storage.ThermostatMongoStorage;
import io.swagger.api.Bootstrap;
import io.swagger.jersey.config.JerseyJaxrsConfig;

public class SwaggerServer {

    Server server;

    public void buildServer(Map<String, String> serverConfig, Map<String, String> userConfig) {
        ThermostatMongoStorage.start(27518);

        server = new Server();

        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletHandler.setContextPath("/");
        server.setHandler(servletHandler);

        ResourceConfig resourceConfig = new ResourceConfig();
        setupResourceConfig(serverConfig, userConfig, resourceConfig);

        ServletHolder jetty = new ServletHolder(new ServletContainer(resourceConfig));
        servletHandler.addServlet(jetty, "/api/v100/*");
        jetty.setInitOrder(1);

        ServletHolder jerseyConfig = servletHandler.addServlet(JerseyJaxrsConfig.class, "/swagger-core");
        jerseyConfig.setInitOrder(2);
        jerseyConfig.setInitParameter("api.version", "1.0.0");
        jerseyConfig.setInitParameter("swagger.api.title", "Thermostat Web API");
        jerseyConfig.setInitParameter("swagger.api.basepath", "https://localhost/api/v100");

        ServletHolder bootstrap = new ServletHolder(new Bootstrap());
        servletHandler.addServlet(bootstrap, "/swagger-bootstrap");
        bootstrap.setInitOrder(2);

        servletHandler.addFilter(io.swagger.api.ApiOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        setupConnectors(serverConfig);
    }

    private void setupResourceConfig(Map<String, String> serverConfig, Map<String, String> userConfig, ResourceConfig resourceConfig) {
        resourceConfig.packages("io.swagger.jaxrs.listing, io.swagger.sample.resource, io.swagger.api");
        resourceConfig.register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
        resourceConfig.property("jersey.config.server.wadl.disableWadl", "true");

        if (serverConfig.containsKey(ServerConfiguration.SECURITY_PROXY_URL.toString())) {
            resourceConfig.register(new ProxyAuthFilter(new UserStore(userConfig)));
        } else if (serverConfig.containsKey(ServerConfiguration.SECURITY_BASIC_URL.toString())) {
            resourceConfig.register(new BasicAuthFilter(new UserStore(userConfig)));
        } else {
            resourceConfig.register(new NoAuthFilter());
        }
        resourceConfig.register(new RolesAllowedDynamicFeature());
    }

    private void setupConnectors(Map<String, String> serverConfig) {
        server.setConnectors(new Connector[]{});
        if (serverConfig.containsKey(ServerConfiguration.SECURITY_PROXY_URL.toString())) {
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());

            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));

            try {
                URL url = new URL(serverConfig.get(ServerConfiguration.SECURITY_PROXY_URL.toString()));
                httpConnector.setHost(url.getHost());
                httpConnector.setPort(url.getPort());
            } catch (MalformedURLException e) {

                httpConnector.setHost("localhost");
                httpConnector.setPort(8090);
            }
            httpConnector.setIdleTimeout(30000);

            server.addConnector(httpConnector);
        } else if (serverConfig.containsKey(ServerConfiguration.SECURITY_BASIC_URL.toString())) {
            HttpConfiguration httpConfig = new HttpConfiguration();
            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));

            try {
                URL url = new URL(serverConfig.get(ServerConfiguration.SECURITY_BASIC_URL.toString()));
                httpConnector.setHost(url.getHost());
                httpConnector.setPort(url.getPort());
            } catch (MalformedURLException e) {
                httpConnector.setHost("localhost");
                httpConnector.setPort(8090);
            }
            httpConnector.setIdleTimeout(30000);

            server.addConnector(httpConnector);
        } else {
            HttpConfiguration httpConfig = new HttpConfiguration();
            ServerConnector httpConnector = new ServerConnector(server);
            httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));

            httpConnector.setHost("localhost");
            httpConnector.setPort(8090);
            httpConnector.setIdleTimeout(30000);

            server.addConnector(httpConnector);
        }
    }

    public Server getServer() {
        return server;
    }

    public void finish() {
        ThermostatMongoStorage.finish();
    }
}
