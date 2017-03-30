package com.redhat.thermostat.gateway.common.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationMergerTest {

    private Configuration globalConfig;
    private Configuration serviceConfig;

    @Before
    public void setup() {
        globalConfig = mock(Configuration.class);
        serviceConfig = mock(Configuration.class);
    }

    @Test
    public void canGetGlobalConfigOnly() {
        boolean globalOnly = true;
        mergedOneConfigOnlyTest(globalOnly);
    }

    @Test
    public void canGetServiceConfigOnly() {
        boolean globalOnly = false;
        mergedOneConfigOnlyTest(globalOnly);
    }

    private void mergedOneConfigOnlyTest(boolean globalOnly) {
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", "foo-val");
        expected.put("some", "value");
        Map<String, String> global;
        Map<String, String> service;
        if (globalOnly) {
            global = expected;
            service = Collections.emptyMap();
        } else {
            service = expected;
            global = Collections.emptyMap();
        }
        when(globalConfig.asMap()).thenReturn(global);
        when(serviceConfig.asMap()).thenReturn(service);
        Configuration config = new ConfigurationMerger(globalConfig, serviceConfig);
        Map<String, String> actual = config.asMap();
        assertEquals(expected, actual);
    }


    @Test
    public void canGetMergedWithGlobalOnlyKey() {
        mergedWithXOnlyKey(true, "bar-val");
    }

    @Test
    public void canGetMergedWithServiceOnlyKey() {
        // expect "foo" to not be overriden by larger global set.
        mergedWithXOnlyKey(false, "foo-val");
    }

    private void mergedWithXOnlyKey(boolean globalHasOnlyKey, String overrideVal) {
        Map<String, String> moreKeys = new HashMap<>();
        moreKeys.put("foo", "foo-val");
        moreKeys.put("some", "value");
        Map<String, String> lessKeysWithOverride = new HashMap<>();
        lessKeysWithOverride.put("foo", overrideVal);
        Map<String, String> global;
        Map<String, String> service;
        if (globalHasOnlyKey) {
            global = moreKeys;
            service = lessKeysWithOverride;
        } else {
            global = lessKeysWithOverride;
            service = moreKeys;
        }

        when(globalConfig.asMap()).thenReturn(global);
        when(serviceConfig.asMap()).thenReturn(service);
        Configuration config = new ConfigurationMerger(globalConfig, serviceConfig);
        Map<String, String> actual = config.asMap();
        Map<String, String> expected = new HashMap<>();
        expected.put("foo", overrideVal);
        expected.put("some", "value");
        assertEquals(expected, actual);
    }
}
