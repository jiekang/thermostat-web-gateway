package com.redhat.thermostat.gateway.common.core;

import java.util.Collections;
import java.util.Map;

class GlobalConfiguration extends BasicConfiguration {

    private final CommonPaths paths;
    private final Map<String, String> map;

    GlobalConfiguration(String gatewayHome) {
        paths = new CommonPaths(gatewayHome);
        map = loadConfig(paths.getGlobalConfigFilePath());
    }

    @Override
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(map);
    }

}
