package com.redhat.thermostat.gateway.common.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class GlobalConfigurationTest extends ConfigurationTest {

    @Test
    public void canReadGlobalConfig() {
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("foo", "bar");
        expected.put("bar", "baz");
        String root = getTestRoot();
        GlobalConfiguration config = new GlobalConfiguration(root);
        Map<String, String> actual = config.asMap();
        assertEquals(expected, actual);
    }

}
