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

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.redhat.thermostat.gateway.tests.utils.MongodTestUtil;
import com.redhat.thermostat.gateway.tests.utils.ProcessTestUtil;

public class IntegrationTest {
    protected static HttpClient client;
    protected static String baseUrl = "http://127.0.0.1:30000";

    protected static final MongodTestUtil mongodTestUtil = new MongodTestUtil();
    private static final Path distributionImage = Paths.get("../../distribution/target/image");
    private static final String WEB_GATEWAY_SCRIPT = "thermostat-web-gateway.sh";

    private static Process serverProcess;

    @BeforeClass
    public static void beforeClassIntegrationTest() throws Exception {
        mongodTestUtil.startMongod();
        setupMongoCredentials();

        client = new HttpClient();
        client.start();

        startServer();
    }

    private static void setupMongoCredentials() throws IOException, InterruptedException {
        Path mongoSetup = distributionImage.resolve("etc/mongo-dev-setup.js");

        ProcessBuilder processBuilder = new ProcessBuilder().command("mongo", mongodTestUtil.listenAddress, mongoSetup.toAbsolutePath().toString()).inheritIO();
        Process mongoProcess = processBuilder.start();
        mongoProcess.waitFor();
    }

    private static void startServer() throws IOException, InterruptedException {
        String command = distributionImage.resolve("bin").resolve(WEB_GATEWAY_SCRIPT).toAbsolutePath().toString();

        ProcessBuilder processBuilder = new ProcessBuilder().command(command).inheritIO().redirectError(ProcessBuilder.Redirect.PIPE);

        serverProcess = processBuilder.start();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getErrorStream()));

        Future<Boolean> f = Executors.newFixedThreadPool(1).submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String line;
                while ((line = reader.readLine()) != null && !line.contains("Server:main: Started")) {
                    System.out.println(line);
                }
                return true;
            }
        });

        try {
            f.get(5000L, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | TimeoutException e) {
            fail();
        }
    }



    private static void stopServer() throws Exception {
        ProcessTestUtil.killRecursively(serverProcess);
    }

    @AfterClass
    public static void afterClassIntegrationTest() throws Exception {
        mongodTestUtil.stopMongod();
        client.stop();
        stopServer();
    }
}
