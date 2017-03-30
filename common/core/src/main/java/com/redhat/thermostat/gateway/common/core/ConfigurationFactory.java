package com.redhat.thermostat.gateway.common.core;

public class ConfigurationFactory {

    private final String gatewayHome;

    public ConfigurationFactory(String gatewayHome) {
        this.gatewayHome = gatewayHome;
    }

    public Configuration getConfigation(String serviceName) {
        Configuration global = new GlobalConfiguration(gatewayHome);
        Configuration serviceConfig = new ServiceConfiguration(gatewayHome, serviceName);
        return new ConfigurationMerger(global, serviceConfig);
    }
}
