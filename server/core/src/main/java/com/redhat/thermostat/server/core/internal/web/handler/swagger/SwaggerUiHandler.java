package com.redhat.thermostat.server.core.internal.web.handler.swagger;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

public class SwaggerUiHandler {
    public ResourceHandler createSwaggerResourceHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        resourceHandler.setResourceBase("");
        URL u = this.getClass().getResource("/swagger/index.html");
        URI root;
        try {
            root = u.toURI().resolve("./").normalize();
            resourceHandler.setBaseResource(Resource.newResource(root));
        } catch (URISyntaxException | IOException e) {
            return null;
        }
        return resourceHandler;
    }
}
