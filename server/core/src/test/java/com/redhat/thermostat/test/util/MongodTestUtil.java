package com.redhat.thermostat.test.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.redhat.thermostat.server.core.internal.configuration.MongoConfiguration;

public class MongodTestUtil {

    public static Map<String, String> mongoConfiguration = new HashMap<>();
    private static final String host = "127.0.0.1";
    private static final int port = 28000;

    public MongoClient mongoClient;
    public Path tempDbDir;
    public Path tempLogFile;
    public Process process;

    static {
        mongoConfiguration.put(MongoConfiguration.MONGO_URL.toString(), "mongodb://" + host + ":" + port);
    }


    public void startMongod() throws IOException {
        tempDbDir = Files.createTempDirectory("tms-mongo");
        Files.createDirectories(tempDbDir.resolve("data/db"));
        tempLogFile = tempDbDir.resolve("mongod.log");

        String[] command = {"mongod", "--dbpath", tempDbDir.resolve("data/db").toAbsolutePath().toString(), "--port", String.valueOf(port), "--fork", "--logpath", tempLogFile.toAbsolutePath().toString()};
        ProcessBuilder builder = new ProcessBuilder(command);
        process = builder.start();

        mongoClient = new MongoClient(new ServerAddress(host, port),  new MongoClientOptions.Builder().serverSelectionTimeout(0).connectTimeout(0).socketTimeout(0).build());
    }

    public void stopMongod() {
        try {
            mongoClient.getDatabase("admin").runCommand(new Document("shutdown", 1));
        } catch (Exception e) {
        }
        mongoClient.close();
    }

    public void waitForMongodStart() throws IOException, InterruptedException {
        waitFor("waiting for connections on port");
    }

    public void waitForMongodStop() throws IOException, InterruptedException {
        waitFor("dbexit:  rc: 0");
    }

    private void waitFor(String match) throws IOException, InterruptedException {
        final String[] s = new String[]{""};

        for (int i = 0; i < 50; i++) {
            if (Files.exists(tempLogFile) && !s[0].contains(match)) {
                s[0] = new String(Files.readAllBytes(tempLogFile));
                System.out.println(s[0]);
                Thread.sleep(100l);
            } else {
                return;
            }
        }
    }
}
