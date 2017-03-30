package com.redhat.thermostat.gateway.common.core;

import java.util.Collections;
import java.util.Map;

class ServiceConfiguration extends BasicConfiguration {

    private final CommonPaths paths;
    @SuppressWarnings("unused")
    private final String serviceName;
    private final Map<String, String> map;

    ServiceConfiguration(String gatewayHome, String serviceName) {
        this.paths = new CommonPaths(gatewayHome);
        this.serviceName = serviceName;
        this.map = loadConfig(paths.getServiceConfigFilePath(serviceName));
    }

    @Override
    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(map);
    }

}
