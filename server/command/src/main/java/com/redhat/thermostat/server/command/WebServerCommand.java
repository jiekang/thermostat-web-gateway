package com.redhat.thermostat.server.command;

import java.util.Collections;

import com.redhat.thermostat.server.core.web.CoreServer;


public class WebServerCommand {
    public static void main(String[] args) {
        CoreServer server = new CoreServer();
        server.buildServer(Collections.EMPTY_MAP, Collections.EMPTY_MAP, Collections.EMPTY_MAP);

        try {
            server.getServer().start();
            server.getServer().join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.finish();
        }
    }
}
