package com.redhat.thermostat.test.util;

import java.util.HashMap;
import java.util.Map;

import com.redhat.thermostat.server.core.internal.web.configuration.ServerConfiguration;

public class CoreServerTestUtil {
    public static final Map<String, String> serverConfiguration = new HashMap<>();

    private static final String host = "127.0.0.1";
    private static final int port = TestPortSetup.SERVER_PORT;

    static {
        serverConfiguration.put(ServerConfiguration.URL.toString(), "http://" + host + ":" + port);
    }
}
