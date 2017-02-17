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
    }
}
