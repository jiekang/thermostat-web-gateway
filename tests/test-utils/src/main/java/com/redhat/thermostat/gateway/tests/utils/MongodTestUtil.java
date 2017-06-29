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

package com.redhat.thermostat.gateway.tests.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.redhat.thermostat.gateway.common.util.OS;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongodTestUtil {

    private static final int WAIT_FOR_MAX_ITERATIONS = 100;
    private static final long WAIT_FOR_SLEEP_DURATION = 100L;

    private final String databaseName = "thermostat";
    private final String host = "127.0.0.1";
    private final int port = 27518;
    public final String listenAddress = host + ":" + port;

    private MongoClient mongoClient;
    private Path tempDbDir;
    private Path tempLogFile;
    public Process process;
    private boolean connectedToDatabase;

    public void startMongod() throws IOException, InterruptedException {
        tempDbDir = Files.createTempDirectory("tms-mongo");
        tempDbDir.toFile().deleteOnExit();
        Files.createDirectories(tempDbDir.resolve("data/db"));
        tempLogFile = tempDbDir.resolve("mongod.log");
        tempLogFile.toFile().deleteOnExit();

        String[] posixCommand = {"mongod", "--dbpath", tempDbDir.resolve("data/db").toAbsolutePath().toString(), "--port", String.valueOf(port), "--fork", "--logpath", tempLogFile.toAbsolutePath().toString()};
        String[] windowsCommand = {"cmd", "/c", "mongod", "--dbpath", tempDbDir.resolve("data/db").toAbsolutePath().toString(), "--port", String.valueOf(port), "--logpath", tempLogFile.toAbsolutePath().toString()};
        ProcessBuilder builder = new ProcessBuilder(OS.IS_UNIX ? posixCommand : windowsCommand);
        process = builder.start();
        mongoClient = new MongoClient(new ServerAddress(host, port));
        connectedToDatabase = waitForMongodStart();
    }

    public void stopMongod() throws IOException, InterruptedException {
        if (connectedToDatabase) {
            try {
                mongoClient.getDatabase("admin").runCommand(new Document("shutdown", 1));
            } catch (Exception ignored) {
            }
            mongoClient.close();
            mongoClient = null;
            waitForMongodStop();
            finish();
        }
    }

    public void dropCollection(String collectionName) {
        mongoClient.getDatabase(databaseName).getCollection(collectionName).drop();
    }

    private void finish() throws IOException {
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

    private boolean waitForMongodStart() throws IOException, InterruptedException {
        return waitFor("waiting for connections on port", "addr already in use");
    }

    private boolean waitForMongodStop() throws IOException, InterruptedException {
        return waitFor("dbexit:  rc: 0");
    }

    private boolean waitFor(String match) throws IOException, InterruptedException {
        return waitFor(match, "");
    }

    /**
     * Keeps checking the temporary log file to see if any of the desired
     * matches are found in the file. This allows short-circuiting of checking
     * the files in the event some error happens (like being unable to connect
     * to the database) so it does not keep cycling for a long period of time.
     * @param desiredMatch The string to find, returns true if found. Null
     *                     should not be passed to this argument.
     * @param errorMatch An error string to return false on finding. An empty
     *                   string should be used if no error match is needed (it
     *                   is better to call waitFor(desiredMatch) instead). Null
     *                   should not be used.
     * @return True if any of the desired matches were found, false if an error
     * match was found or it timed out.
     */
    private boolean waitFor(String desiredMatch, String errorMatch) throws IOException, InterruptedException {
        for (int i = 0; i < WAIT_FOR_MAX_ITERATIONS; i++) {
            if (Files.exists(tempLogFile)) {
                String logFileText = new String(Files.readAllBytes(tempLogFile));
                if (logFileText.contains(desiredMatch)) {
                    return true;
                }
                if (!"".equals(errorMatch) && logFileText.contains(errorMatch)) {
                    return false;
                }
            }

            Thread.sleep(WAIT_FOR_SLEEP_DURATION);
        }

        return false;
    }

    public boolean isConnectedToDatabase() {
        return connectedToDatabase;
    }
}