package com.redhat.thermostat.server.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.redhat.thermostat.server.core.web.CoreServer;


public class WebServerCommand {
    public static void main(String[] args) {
        CoreServer server = new CoreServer();

        Map<String, String> mongoConfig = new HashMap<>();
        mongoConfig.put("MONGO_USERNAME", "mongodevuser");
        mongoConfig.put("MONGO_PASSWORD", "mongodevpassword");

        server.buildServer(Collections.EMPTY_MAP, mongoConfig, Collections.EMPTY_MAP);

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
