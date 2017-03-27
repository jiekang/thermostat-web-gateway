package com.redhat.thermostat.gateway.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

public class Start {

    public static void main(String[] args) {
        assert args.length == 1;

        CoreServer server = new CoreServer();

        Path configPath = Paths.get(args[0]);
        assert Files.isRegularFile(configPath);

        Properties configProperties = new Properties();
        try {
            configProperties.load(new FileInputStream(configPath.toFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Object, Object> entry : configProperties.entrySet()) {
            Path warPath = Paths.get(entry.getValue().toString());

            assert Files.isDirectory(warPath);

            server = server.add(entry.getKey().toString(), warPath);
        }

        server.build();

        try {
            server.getServer().start();
            server.getServer().join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            server.getServer().stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
