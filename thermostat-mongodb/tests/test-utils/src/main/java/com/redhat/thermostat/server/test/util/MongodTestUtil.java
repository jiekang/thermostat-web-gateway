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

package com.redhat.thermostat.server.test.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.redhat.thermostat.server.core.internal.storage.mongo.configuration.MongoConfiguration;

public class MongodTestUtil {

    public static final Map<String, String> mongoConfiguration = new HashMap<>();

    public static final Map<String, String> timeoutMongoConfiguration = new HashMap<>();

    private static final String host = "127.0.0.1";
    private static final int port = TestPortSetup.MONGODB_PORT;

    private MongoClient mongoClient;
    private Path tempDbDir;
    private Path tempLogFile;
    public Process process;

    static {
        mongoConfiguration.put(MongoConfiguration.MONGO_URL.toString(), "mongodb://" + host + ":" + port);
        mongoConfiguration.put(MongoConfiguration.MONGO_DB.toString(), "mongoConfig");

        timeoutMongoConfiguration.put(MongoConfiguration.MONGO_URL.toString(), "mongodb://" + host + ":" + port);
        timeoutMongoConfiguration.put(MongoConfiguration.MONGO_DB.toString(), "timeoutMongoConfig");
        timeoutMongoConfiguration.put(MongoConfiguration.MONGO_SERVER_TIMEOUT.toString(), "0");
    }


    public void startMongod() throws IOException {
        tempDbDir = Files.createTempDirectory("tms-mongo");
        tempDbDir.toFile().deleteOnExit();
        Files.createDirectories(tempDbDir.resolve("data/db"));
        tempLogFile = tempDbDir.resolve("mongod.log");
        tempLogFile.toFile().deleteOnExit();

        String[] command = {"mongod", "--dbpath", tempDbDir.resolve("data/db").toAbsolutePath().toString(), "--port", String.valueOf(port), "--fork", "--logpath", tempLogFile.toAbsolutePath().toString()};
        ProcessBuilder builder = new ProcessBuilder(command);
        process = builder.start();

        mongoClient = new MongoClient(new ServerAddress(host, port));
    }

    public void stopMongod() {
        try {
            mongoClient.getDatabase("admin").runCommand(new Document("shutdown", 1));
        } catch (Exception ignored) {
        }
        mongoClient.close();
        mongoClient = null;
    }

    public void finish() throws IOException {
        Files.walkFileTree(tempDbDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    public boolean waitForMongodStart() throws IOException, InterruptedException {
        return waitFor("waiting for connections on port");
    }

    public boolean waitForMongodStop() throws IOException, InterruptedException {
        return waitFor("dbexit:  rc: 0");
    }

    private boolean waitFor(String match) throws IOException, InterruptedException {
        final String[] s = new String[]{""};

        for (int i = 0; i < 100; i++) {
            if (Files.exists(tempLogFile) && !s[0].contains(match)) {
                s[0] = new String(Files.readAllBytes(tempLogFile));
            } else if (s[0].contains(match)) {
                return true;
            }
            Thread.sleep(100L);
        }
        return false;
    }
}
