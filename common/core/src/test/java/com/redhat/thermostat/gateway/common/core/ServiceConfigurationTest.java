package com.redhat.thermostat.gateway.common.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ServiceConfigurationTest extends ConfigurationTest {

    @Test
    public void canReadServiceConfig() {
        String serviceName = "test-service";
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("foo", "service-value");
        expected.put("test", "me");
        String root = getTestRoot();
        ServiceConfiguration config = new ServiceConfiguration(root, serviceName);
        Map<String, String> actual = config.asMap();
        assertEquals(expected, actual);
    }
}
