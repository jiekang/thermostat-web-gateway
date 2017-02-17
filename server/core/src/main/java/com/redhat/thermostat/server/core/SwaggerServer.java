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
import org.eclipse.jetty.servlet.ServletHandler;
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

        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);

        ResourceConfig resourceConfig = new ResourceConfig();
        setupResourceConfig(serverConfig, userConfig, resourceConfig);

        ServletHolder jetty = new ServletHolder(new ServletContainer(resourceConfig));
        jetty.setInitOrder(1);
        jetty.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing, io.swagger.sample.resource, io.swagger.api");
        jetty.setInitParameter("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");
        jetty.setInitParameter("jersey.config.server.wadl.disableWadl", "true");

        servletHandler.addServletWithMapping(jetty, "/api/v100/*");

        ServletHolder jerseyConfig = new ServletHolder(new JerseyJaxrsConfig());
        jerseyConfig.setInitOrder(2);
        jerseyConfig.setInitParameter("api.version", "1.0.0");
        jerseyConfig.setInitParameter("swagger.api.title", "Thermostat Web API");
        jerseyConfig.setInitParameter("swagger.api.basepath", "https://localhost/api/v100");

        servletHandler.addServlet(jerseyConfig);

        ServletHolder bootstrap = new ServletHolder(new Bootstrap());
        servletHandler.addServlet(bootstrap);

        servletHandler.addFilterWithMapping(io.swagger.api.ApiOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        setupConnectors(serverConfig);
    }

    private void setupResourceConfig(Map<String, String> serverConfig, Map<String, String> userConfig, ResourceConfig resourceConfig) {
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
