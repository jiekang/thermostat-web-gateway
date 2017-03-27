package com.redhat.thermostat.gateway.server;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class CoreServer {

    private Server server = new Server();
    private List<WebAppContext> webAppContextList = new ArrayList<>();


    public CoreServer add(String contextPath, Path warPath) {
        WebAppContext webAppContext = new WebAppContext();

        webAppContext.setContextPath(contextPath);
        webAppContext.setWar(warPath.toAbsolutePath().toString());

        webAppContextList.add(webAppContext);

        return this;
    }

    public CoreServer build() {
        setupHandler();
        setupConnector();

        return this;
    }

    private void setupHandler() {
        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();

        for (WebAppContext webAppContext : webAppContextList) {
            contextHandlerCollection.addHandler(webAppContext);
        }

        server.setHandler(contextHandlerCollection);
    }

    private void setupConnector() {
        ServerConnector httpConnector = new ServerConnector(server);

        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConnector.addConnectionFactory(new HttpConnectionFactory(httpConfig));

        httpConnector.setHost("localhost");
        httpConnector.setPort(30000);


        server.setConnectors(new Connector[]{httpConnector});
    }

    public Server getServer() {
        return this.server;
    }

}
