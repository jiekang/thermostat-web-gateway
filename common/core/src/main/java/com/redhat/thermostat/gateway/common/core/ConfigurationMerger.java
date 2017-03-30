package com.redhat.thermostat.gateway.common.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class ConfigurationMerger implements Configuration {

    private final Configuration globalConfig;
    private final Configuration serviceConfig;

    ConfigurationMerger(Configuration globalConfig, Configuration serviceConfig) {
        this.globalConfig = globalConfig;
        this.serviceConfig = serviceConfig;
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> mergedConfig = new HashMap<>(serviceConfig.asMap());
        for (Entry<String, String> entry: globalConfig.asMap().entrySet()) {
            if (!mergedConfig.containsKey(entry.getKey())) {
                mergedConfig.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(mergedConfig);
    }

}
