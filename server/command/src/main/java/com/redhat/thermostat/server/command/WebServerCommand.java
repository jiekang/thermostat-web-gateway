package com.redhat.thermostat.server.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.redhat.thermostat.server.core.web.CoreServer;


public class WebServerCommand {
    public static void main(String[] args) {
        CoreServer server = new CoreServer();

        Map<String, String> mongoConfig = new HashMap<>();
        mongoConfig.put("MONGO_URL", "mongodb://127.0.0.1:27518");
        mongoConfig.put("MONGO_DB", "thermostat");
        mongoConfig.put("MONGO_USERNAME", "mongodevuser");
        mongoConfig.put("MONGO_PASSWORD", "mongodevpassword");

        Map<String, String> serverConfig = new HashMap<>();
        serverConfig.put("URL", "http://localhost:26000");
        serverConfig.put("SECURITY_PROXY", "false");
        serverConfig.put("SECURITY_BASIC", "false");
        serverConfig.put("SWAGGER_UI_ENABLED", "false");

        server.buildServer(serverConfig, mongoConfig, Collections.EMPTY_MAP);

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
