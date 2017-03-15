package com.redhat.thermostat.web.setup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.client.HttpClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.redhat.thermostat.server.core.internal.web.configuration.ServerConfiguration;
import com.redhat.thermostat.server.core.web.CoreServer;
import com.redhat.thermostat.server.test.util.CoreServerTestUtil;
import com.redhat.thermostat.server.test.util.MongodTestUtil;

public class SwaggerCoreServerTestSetup extends TimedTestSetup {
    private static CoreServer coreServer;
    protected static HttpClient client;
    private static int port;
    protected final String baseUrl = "http://localhost:" + port;
    protected final String apiUrl = baseUrl + "/api/v100";

    private static Thread thread;
    private static final AtomicBoolean ready = new AtomicBoolean(false);

    @BeforeClass
    public static void setupClassSwaggerCoreServerTestSetup() throws Exception {
        Map<String, String> serverConfig = new HashMap<>(CoreServerTestUtil.serverConfiguration);
        serverConfig.put(ServerConfiguration.SWAGGER_UI_ENABLED.toString(), "true");

        coreServer= new CoreServer();
        coreServer.buildServer(serverConfig, MongodTestUtil.timeoutMongoConfiguration, Collections.EMPTY_MAP);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    coreServer.getServer().start();
                    ready.getAndSet(true);
                    coreServer.getServer().join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        client = new HttpClient();
        client.start();

        port = coreServer.getPort();
    }

    @AfterClass
    public static void cleanupClassSwaggerCoreServerTestSetup() throws Exception {
        coreServer.finish();
        client.stop();
        thread.join();
    }

    @Before
    public void setupSwaggerCoreServerTestSetup() {
        while (!ready.get()){
        }
    }
}
