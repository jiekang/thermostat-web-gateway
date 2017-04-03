package com.redhat.thermostat.gateway.common.core;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class CommonPathsTest {

    private static final String ROOT = "somepath";

    @Test
    public void canGetGlobalConfig() {
        CommonPaths paths = new CommonPaths(ROOT);
        String expected = ROOT + File.separator + "etc" + File.separator + "global-config.properties";
        assertEquals(expected, paths.getGlobalConfigFilePath());
    }

    @Test
    public void canGetServiceConfig() {
        String serviceName = "test-me-service";
        CommonPaths paths = new CommonPaths(ROOT);
        String expected = ROOT + File.separator + "etc" + File.separator + serviceName + File.separator + "service-config.properties";
        assertEquals(expected, paths.getServiceConfigFilePath(serviceName));
    }
}
