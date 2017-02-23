package com.redhat.thermostat.server.core.internal.web.handler.swagger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.junit.Test;

public class SwaggerUiHandlerTest {
    @Test
    public void testCreateHandler() throws IOException {
        ResourceHandler h = new SwaggerUiHandler().createSwaggerResourceHandler();

        assertFalse(h.isDirectoriesListed());
        assertTrue(h.getWelcomeFiles().length == 1 && h.getWelcomeFiles()[0].equals("index.html"));

        Resource r = h.getBaseResource();

        assertTrue(r.getResource("index.html").getFile().exists());
        assertTrue(r.getResource("swagger.json").getFile().exists());
    }
}
