package com.redhat.thermostat.server.command;

import java.util.Collections;

import org.eclipse.jetty.server.Server;

import com.redhat.thermostat.server.core.SwaggerServer;

public class TestCommand {
    public static void main(String[] args) {
        SwaggerServer swaggerServer = new SwaggerServer();

        swaggerServer.buildServer(Collections.EMPTY_MAP, Collections.EMPTY_MAP);

        Server server = swaggerServer.getServer();
        try {
            server.start();
            System.out.println(server.dump());
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            swaggerServer.finish();
        }

//        Server server = new Server(8090);
//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        server.setHandler(context);
//
//        // Setup API resources
//        ServletHolder apiServlet = context.addServlet(ServletContainer.class, "/api/v100/*");
//        apiServlet.setInitOrder(1);
//        apiServlet.setInitParameter("jersey.config.server.provider.packages", "io.swagger.jaxrs.listing, io.swagger.sample.resource, io.swagger.api");
//        apiServlet.setInitParameter("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");
//        apiServlet.setInitParameter("jersey.config.server.wadl.disableWadl", "true");
//
//        // Setup Swagger servlet
//        ServletHolder swaggerServlet = context.addServlet(DefaultJaxrsConfig.class, "/swagger-core");
//        swaggerServlet.setInitOrder(2);
//        swaggerServlet.setInitParameter("api.version", "1.0.0");
//
//        try {
//            server.start();
//            server.join();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
