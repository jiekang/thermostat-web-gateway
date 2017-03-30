package com.redhat.thermostat.gateway.common.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ConfigurationFactoryTest extends ConfigurationTest {

    @Test
    public void canGetMergedConfigForService() {
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", "service-value"); // override from service config
        expected.put("bar", "baz"); // global only config
        expected.put("test", "me"); // service only config
        String root = getTestRoot();
        ConfigurationFactory factory = new ConfigurationFactory(root);
        Configuration config = factory.getConfigation("test-service");
        assertEquals(expected, config.asMap());
    }
}
