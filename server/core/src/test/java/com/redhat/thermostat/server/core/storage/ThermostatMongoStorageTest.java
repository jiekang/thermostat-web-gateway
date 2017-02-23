package com.redhat.thermostat.server.core.storage;

import static junit.framework.TestCase.assertFalse;

import org.junit.Ignore;
import org.junit.Test;

import com.redhat.thermostat.server.core.internal.storage.ThermostatMongoStorage;

public class ThermostatMongoStorageTest {

    @Test
    public void testNoStorage() {
        ThermostatMongoStorage.start(1000);

        assertFalse(ThermostatMongoStorage.isConnected());

        ThermostatMongoStorage.finish();
    }

    // TODO: Test connecting to a temporary mongo instance created by the test
    @Test
    @Ignore
    public void testStorage() {
        ThermostatMongoStorage.start(28000);
    }

}
