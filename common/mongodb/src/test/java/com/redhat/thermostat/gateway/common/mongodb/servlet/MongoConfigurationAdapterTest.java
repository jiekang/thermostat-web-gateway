package com.redhat.thermostat.gateway.common.mongodb.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.Configuration;
import com.redhat.thermostat.gateway.common.mongodb.configuration.MongoConfiguration;

public class MongoConfigurationAdapterTest {

    private Configuration serviceConfig;

    @Before
    public void setup() {
        serviceConfig = mock(Configuration.class);
    }

    @Test
    public void testAdapt() {
        Map<String, String> expected = new HashMap<>();
        expected.put(MongoConfiguration.MONGO_DB.name(), "foo");
        expected.put(MongoConfiguration.MONGO_USERNAME.name(), "foo-user");
        expected.put(MongoConfiguration.MONGO_PASSWORD.name(), "foo-password");
        expected.put(MongoConfiguration.MONGO_URL.name(), "mongodb://127.0.0.1:23793");

        Map<String, String> service = new HashMap<>();
        service.put(MongoConfiguration.MONGO_DB.name(), "foo");
        service.put(MongoConfiguration.MONGO_USERNAME.name(), "foo-user");
        service.put(MongoConfiguration.MONGO_PASSWORD.name(), "foo-password");
        service.put(MongoConfiguration.MONGO_URL.name(), "mongodb://127.0.0.1:23793");
        service.put("soome-val", "other");

        when(serviceConfig.asMap()).thenReturn(service);

        Configuration mongoConfig = new MongoConfigurationAdapter(serviceConfig);
        assertEquals(expected, mongoConfig.asMap());
    }
}
