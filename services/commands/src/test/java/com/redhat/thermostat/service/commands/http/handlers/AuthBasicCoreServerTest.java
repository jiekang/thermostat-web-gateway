/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.service.commands.http.handlers;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.Servlet;
import javax.websocket.server.ServerContainer;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.redhat.thermostat.gateway.common.core.Configuration;
import com.redhat.thermostat.gateway.common.core.GlobalConfiguration;
import com.redhat.thermostat.gateway.server.CoreServerBuilder;
import com.redhat.thermostat.gateway.server.auth.basic.BasicLoginService;
import com.redhat.thermostat.gateway.server.auth.basic.BasicUserStore;
import com.redhat.thermostat.gateway.server.services.CoreService;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilder;

public class AuthBasicCoreServerTest {

    private static final String SERVICE_NAME = "commands";
    private static final String CONTEXT_NAME = "/" + SERVICE_NAME;
    private static final int TEST_PORT = 32039;
    private static final String TEST_ADDRESS = "127.0.0.1";
    protected WebSocketClient client;
    protected final String baseUrl = "ws://" + TEST_ADDRESS + ":" + TEST_PORT + CONTEXT_NAME +"/v1/";

    private static Thread thread;
    private static Server server;

    protected static CountDownLatch serverReadyLatch = new CountDownLatch(1);

    @BeforeClass
    public static void setupClass() {
        CoreServerBuilder builder = new CoreServerBuilder();
        builder.setServiceBuilder(new CoreServiceBuilder() {

            @Override
            public CoreServiceBuilder setConfiguration(Configuration config) {
                // nothing
                return null;
            }

            @Override
            public List<CoreService> build() {
                return Arrays.<CoreService>asList(new TestCoreService(getUserConfig()));
            }
        });
        builder.setServerConfiguration(new Configuration() {

            @Override
            public Map<String, Object> asMap() {
                Map<String, Object> config = new HashMap<>();
                config.put(GlobalConfiguration.ConfigurationKey.IP.name(), TEST_ADDRESS);
                config.put(GlobalConfiguration.ConfigurationKey.PORT.name(), Integer.toString(TEST_PORT));
                return config;
            }
        });
        server = builder.build();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                    serverReadyLatch.countDown();
                    server.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Before
    public void setup() throws Exception {
        boolean expired = !serverReadyLatch.await(2, TimeUnit.SECONDS);
        if (expired) {
            throw new RuntimeException("Server not becoming available");
        }
        client = new WebSocketClient();
        client.start();
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        server.stop();
        thread.join();
    }

    protected static Map<String, String> getUserConfig() {
        Map<String, String> userConfig = new HashMap<>();
        userConfig.put("foo-agent-user", "agent-pwd,thermostat-commands-provider-testAgent");
        userConfig.put("bar-client-user", "client-pwd,thermostat-commands-grant-dump-heap,thermostat-commands-grant-jvm-abc");
        userConfig.put("insufficient-roles-agent", "agent-pwd");
        userConfig.put("insufficient-roles-client", "client-pwd,thermostat-commands-grant-dump-heap");
        return userConfig;
    };

    public static class TestCoreService implements CoreService {

        private final Map<String, String> userConfig;

        public TestCoreService(Map<String, String> userConfig) {
            this.userConfig = userConfig;
        }

        @Override
        public ServletContextHandler createServletContextHandler(Server server) {
            ServletContextHandler contextHandler = createContext(server);
            addWebSocketsHandlers(server, contextHandler);
            setupAuthForContext(server, contextHandler);
            return contextHandler;
        }

        private ServletContextHandler createContext(Server server) {
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SECURITY|ServletContextHandler.SESSIONS);
            context.setServer(server);
            context.setContextPath(CONTEXT_NAME);
            context.addServlet(new ServletHolder(mock(Servlet.class)), "/v1/*");
            return context;
        }

        private void setupAuthForContext(Server server, ServletContextHandler contextHandler) {
            // FIXME: Filter for websockets does not seem to be called.
            //        Set up basic auth directly using jetty API
            ConstraintSecurityHandler security = new ConstraintSecurityHandler();
            String realmName = "Thermostat Realm";
            Constraint cons = new Constraint(realmName, "thermostat-realm");
            cons.setAuthenticate(true);
            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setConstraint(cons);
            mapping.setMethod("Basic");
            mapping.setPathSpec("/*");
            security.setConstraintMappings(Collections.singletonList(mapping));
            security.setAuthenticator(new BasicAuthenticator());
            security.setLoginService(new BasicLoginService(new BasicUserStore(userConfig), realmName));
            contextHandler.setSecurityHandler(security);
        }

        private void addWebSocketsHandlers(Server server, ServletContextHandler contextHandler) {
            // Initialize javax.websocket layer
            try {
                contextHandler.setServer(server);
                ServerContainer container = WebSocketServerContainerInitializer.configureContext(contextHandler);
                container.addEndpoint(CommandChannelClientEndpointHandler.class);
                container.addEndpoint(CommandChannelAgentEndpointHandler.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
