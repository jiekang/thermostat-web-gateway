package com.redhat.thermostat.gateway.common.mongodb.servlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.redhat.thermostat.gateway.common.core.Configuration;
import com.redhat.thermostat.gateway.common.mongodb.configuration.MongoConfiguration;

class MongoConfigurationAdapter implements Configuration {

    private final Configuration config;

    MongoConfigurationAdapter(Configuration config) {
        this.config = config;
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> adaptedConfig = adaptConfig();
        return Collections.unmodifiableMap(adaptedConfig);
    }

    private Map<String, String> adaptConfig() {
        Map<String, String> result = new HashMap<>();
        Map<String, String> totalConfig = config.asMap();
        for (MongoConfiguration item: MongoConfiguration.values()) {
            if (totalConfig.containsKey(item.name())) {
                result.put(item.name(), totalConfig.get(item.name()));
            }
        }
        return result;
    }
}
