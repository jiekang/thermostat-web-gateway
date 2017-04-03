package com.redhat.thermostat.gateway.common.core;

import java.io.File;

public class CommonPaths {

    private static final String GLOBAL_CONFIG_PROPERTIES = "global-config.properties";
    private static final String SERVICE_CONFIG_PROPERTIES = "service-config.properties";

    private final String gatewayHome;

    CommonPaths(String gatewayHome) {
        this.gatewayHome = gatewayHome;
    }

    public String getGlobalConfigFilePath() {
        return getConfigDir() + File.separator + GLOBAL_CONFIG_PROPERTIES;
    }

    public String getServiceConfigFilePath(String serviceName) {
        return getConfigDir() + File.separator + serviceName + File.separator + SERVICE_CONFIG_PROPERTIES;
    }

    private String getConfigDir() {
        return gatewayHome + File.separator + "etc";
    }
}
