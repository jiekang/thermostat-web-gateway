package com.redhat.thermostat.server.core.storage;

import static junit.framework.TestCase.assertFalse;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.redhat.thermostat.server.core.internal.storage.ThermostatMongoStorage;
import com.redhat.thermostat.test.util.MongodTestUtil;

public class ThermostatMongoStorageTest {

    @Test
    public void testNoStorage() {
        ThermostatMongoStorage.start(MongodTestUtil.mongoConfiguration);

        assertFalse(ThermostatMongoStorage.isConnected());

        ThermostatMongoStorage.finish();
    }

    // TODO: Test connecting to a temporary mongo instance created by the test
    @Test
    @Ignore
    public void testStorage() throws IOException {
        MongodTestUtil.startMongod();
        ThermostatMongoStorage.start(MongodTestUtil.mongoConfiguration);
        MongodTestUtil.stopMongod();
    }

}
