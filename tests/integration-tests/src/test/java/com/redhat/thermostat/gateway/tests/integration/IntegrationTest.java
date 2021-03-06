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

package com.redhat.thermostat.gateway.tests.integration;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ConfigurationFactory;
import com.redhat.thermostat.gateway.common.core.config.GatewayHomeRetriever;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration;
import com.redhat.thermostat.gateway.server.Start;

/**
 * Integration tests base class. It's sole responsibility is it to start/stop
 * the gateway service.
 *
 */
public class IntegrationTest {
    private static final ConfigurationFactory factory;

    protected static HttpClient client;
    protected static String baseUrl;

    protected static final Path distributionImage;

    static {
        String distDir = new GatewayHomeRetriever().getGatewayHome();
        distributionImage = Paths.get(distDir);
        factory = new ConfigurationFactory();
        String scheme;
        if (isTLSEnabled()) {
            scheme = "https";
        } else {
            scheme = "http";
        }
        baseUrl = scheme + "://127.0.0.1:30000";
    }

    protected String resourceUrl;

    public IntegrationTest(String serviceUrl) {
        this.resourceUrl = baseUrl + "/" + serviceUrl;
    }

    @BeforeClass
    public static void beforeClassIntegrationTest() throws Exception {
        client = createAndStartHttpClient();
        startServer();
    }

    @AfterClass
    public static void afterClassIntegrationTest() throws Exception {
        client.stop();
        stopServer();
    }

    static HttpClient createAndStartHttpClient() throws Exception {
        final HttpClient theclient;
        if (isTLSEnabled()) {
            SslContextFactory sslFactory = new SslContextFactory();
            sslFactory.setTrustAll(true);
            theclient = new HttpClient(sslFactory);
        } else {
            theclient = new HttpClient();
        }
        addAuthentication(theclient);
        theclient.start();
        return theclient;
    }

    private static Thread serverThread = null;
    private static Start serverObject = null;

    private static class StartListener extends AbstractLifeCycle.AbstractLifeCycleListener {

        private final CountDownLatch latch;
        private Throwable cause = null;
        private boolean failed = false;

        StartListener(CountDownLatch cdl) {
            this.latch = cdl;
        }

        @Override
        public void lifeCycleStarted(LifeCycle event) {
            super.lifeCycleStarted(event);
            latch.countDown();
        }

        @Override
        public void lifeCycleFailure(LifeCycle event, Throwable cause) {
            this.failed = true;
            this.cause = cause;
            this.latch.countDown();
        }

        boolean hasFailed() {
            return failed;
        }

        Throwable getCause() {
            return this.cause;
        }
    }

    private static void startServer() throws IOException, InterruptedException {

        if (serverThread == null) {

            final CountDownLatch contextStartedLatch = new CountDownLatch(1);

            final StartListener listener = new StartListener(contextStartedLatch);

            serverObject = new Start(listener);
            serverThread = new Thread(serverObject);
            serverThread.start();

            // wait for Jetty is up and running?
            contextStartedLatch.await();
            if (listener.hasFailed()) {
                throw new IllegalStateException(listener.getCause());
            }
        }
    }

    private static void stopServer() throws Exception {
        if (serverThread != null) {
            Thread st = serverThread;
            synchronized (serverThread) {
                serverThread = null;
                serverObject.stopServer();
                st.join();
                serverObject = null;
            }
        }
    }

    private static boolean isTLSEnabled() {
        Configuration config = factory.createGlobalConfiguration();
        return Boolean.parseBoolean((String)config.asMap().get(GlobalConfiguration.ConfigurationKey.WITH_TLS.name()));
    }

    /**
     * Adds authentication credentials to the given client: {@code agent:agent-pwd}.
     * @param theClient The HttpClient to operate on.
     */
    protected static void addAuthentication(HttpClient theClient) {
        AuthenticationStore authenticationStore = theClient.getAuthenticationStore();
        URI uri = URI.create(baseUrl);
        String realmName = "Thermostat Realm"; // must match Basic login service's realm name.
        authenticationStore.addAuthentication(new BasicAuthentication(uri, realmName, "agent", "agent-pwd"));
    }

    /**
     * Removes authentication credentials from the given client. It also clears previous
     * authentication results from it.
     *
     * @param theClient The HttpClient to operate on.
     */
    protected static void removeAuthentication(HttpClient theClient) {
        AuthenticationStore authStore = theClient.getAuthenticationStore();
        authStore.clearAuthentications();
        authStore.clearAuthenticationResults();
    }

}
