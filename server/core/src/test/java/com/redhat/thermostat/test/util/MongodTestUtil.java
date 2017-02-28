package com.redhat.thermostat.test.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.redhat.thermostat.server.core.internal.configuration.MongoConfiguration;

public class MongodTestUtil {

    public static Map<String, String> mongoConfiguration = new HashMap<>();

    public static Map<String, String> timeoutMongoConfiguration = new HashMap<>();

    private static final String host = "127.0.0.1";
    private static final int port = 28000;

    public MongoClient mongoClient;
    public Path tempDbDir;
    public Path tempLogFile;
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

    public void stopMongod() throws IOException {
        try {
            mongoClient.getDatabase("admin").runCommand(new Document("shutdown", 1));
        } catch (Exception e) {
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
            Thread.sleep(100l);
        }
        return false;
    }
}
