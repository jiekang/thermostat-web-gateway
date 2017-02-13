package com.redhat.thermostat.server.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.redhat.thermostat.server.core.internal.configuration.ServerConfiguration;
import com.redhat.thermostat.server.core.internal.security.UserStore;
import com.redhat.thermostat.server.core.internal.security.auth.basic.BasicAuthFilter;
import com.redhat.thermostat.server.core.internal.security.auth.proxy.ProxyAuthFilter;
import com.redhat.thermostat.server.core.internal.storage.ThermostatMongoStorage;
import com.redhat.thermostat.server.core.internal.web.handler.http.AnotherPluginHttpHandler;
import com.redhat.thermostat.server.core.internal.web.handler.http.CoreHttpHandler;
import com.redhat.thermostat.server.core.internal.web.handler.storage.MongoBaseStorageHandler;
import com.redhat.thermostat.server.core.internal.web.handler.storage.MongoCoreStorageHandler;

@Component
@Service(CoreServer.class)
public class CoreServer {
    private Server server;

    public void buildServer(Map<String, String> serverConfig, Map<String, String> userConfig) {

        server = new Server();

        URI baseUri = UriBuilder.fromUri("http://localhost").port(8080).build();

        ResourceConfig resourceConfig = new ResourceConfig();
        setupResourceConfig(serverConfig, userConfig, resourceConfig);

        server = JettyHttpContainerFactory.createServer(baseUri, resourceConfig, false);

        setupConnectors(serverConfig);

        setupHandlers(serverConfig);

        ThermostatMongoStorage.start(27518);
    }

    private void setupResourceConfig(Map<String, String> serverConfig, Map<String, String> userConfig, ResourceConfig resourceConfig) {
        MongoBaseStorageHandler baseHandler = new MongoBaseStorageHandler();
        resourceConfig.register(new AnotherPluginHttpHandler(baseHandler));
        resourceConfig.register(new CoreHttpHandler(baseHandler, new MongoCoreStorageHandler()));
        if (serverConfig.containsKey(ServerConfiguration.SECURITY_PROXY_URL.toString())) {
            resourceConfig.register(new ProxyAuthFilter(new UserStore(userConfig)));
        } else if (serverConfig.containsKey(ServerConfiguration.SECURITY_BASIC_URL.toString())) {
            resourceConfig.register(new BasicAuthFilter(new UserStore(userConfig)));
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
                httpConnector.setPort(8091);
            }
            httpConnector.setIdleTimeout(30000);

            server.addConnector(httpConnector);
        }
    }

    private void setupHandlers(Map<String, String> serverConfig) {
        if (serverConfig.containsKey(ServerConfiguration.SWAGGER_ENABLED.toString()) &&
                serverConfig.get(ServerConfiguration.SWAGGER_ENABLED.toString()).equals("true")) {
            Handler originalHandler = server.getHandler();

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{createSwaggerResource(), originalHandler});

            server.setHandler(handlers);
        }
    }

    private Handler createSwaggerResource() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        resourceHandler.setResourceBase("");
        URL u = this.getClass().getResource("/swagger/index.html");
        URI root;
        try {
            root = u.toURI().resolve("./").normalize();
            resourceHandler.setBaseResource(Resource.newResource(root));
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
        return resourceHandler;
    }

    public Server getServer() {
        return server;
    }

    public void finish() {
        ThermostatMongoStorage.finish();
    }
}
