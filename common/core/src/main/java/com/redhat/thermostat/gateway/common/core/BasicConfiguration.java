package com.redhat.thermostat.gateway.common.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

abstract class BasicConfiguration implements Configuration {

    protected static Map<String, String> loadConfig(String configFile) {
        Properties props = new Properties();
        File globalConfig = new File(configFile);
        try (FileInputStream fis = new FileInputStream(globalConfig)) {
            props.load(fis);
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, String> config = new HashMap<>();
        for (Entry<Object, Object> entry: props.entrySet()) {
            config.put((String)entry.getKey(), (String)entry.getValue());
        }
        return config;
    }
}
