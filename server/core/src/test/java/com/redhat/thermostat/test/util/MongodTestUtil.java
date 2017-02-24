package com.redhat.thermostat.test.util;

import java.io.IOException;
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
    private static final ServerAddress address = new ServerAddress(host, port);
    public static MongoClient mongoClient;

    static {
        mongoConfiguration.put(MongoConfiguration.MONGO_URL.toString(), host + port);
    }

    public static void startMongod() throws IOException {
        Path tempDbDir = Files.createTempDirectory("tms-mongo-" + UUID.randomUUID());
        Path tempLogFile = Files.createTempFile(tempDbDir, "mongod", ".log");
        ProcessBuilder builder = new ProcessBuilder("mongod --dbpath " + tempDbDir.toAbsolutePath().toString() + " --port " + port + " --fork --logpath " + tempLogFile.toAbsolutePath().toString());
        builder.start();

        mongoClient = new MongoClient(address,  new MongoClientOptions.Builder().serverSelectionTimeout(0).connectTimeout(0).socketTimeout(0).build());
    }

    public static void stopMongod() {
        mongoClient.getDatabase("admin").runCommand(new Document("shutdown", 1));

        mongoClient.close();
    }
}
