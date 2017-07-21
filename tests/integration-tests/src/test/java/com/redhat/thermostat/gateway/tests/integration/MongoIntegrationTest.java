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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.redhat.thermostat.gateway.tests.utils.MongodTestUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public class MongoIntegrationTest extends IntegrationTest {

    protected static final MongodTestUtil mongodTestUtil = new MongodTestUtil();

    protected String collectionName;

    public MongoIntegrationTest(String serviceUrl, String collectionName) {
        super(serviceUrl);
        this.collectionName = Objects.requireNonNull(collectionName);
    }

    @BeforeClass
    public static void beforeClassMongoIntegrationTest() throws Exception {
        mongodTestUtil.startMongod();
        if (!mongodTestUtil.isConnectedToDatabase()) {
            fail("Unable to start mongodb database, port in use");
        }
        setupMongoCredentials();
    }

    @Before
    public void beforeIntegrationTest() {
        mongodTestUtil.dropCollection(collectionName);
    }

    private static void setupMongoCredentials() throws IOException, InterruptedException {
        Path mongoSetup = distributionImage.resolve("etc/mongo-dev-setup.js");

        ProcessBuilder processBuilder = new ProcessBuilder().command("mongo", mongodTestUtil.listenAddress, mongoSetup.toAbsolutePath().toString());
        Process mongoProcess = processBuilder.start();
        mongoProcess.waitFor();
    }

    @AfterClass
    public static void afterClassMongoIntegrationTest() throws Exception {
        mongodTestUtil.stopMongod();
    }
}
