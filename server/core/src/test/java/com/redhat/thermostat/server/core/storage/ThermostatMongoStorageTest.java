package com.redhat.thermostat.server.core.storage;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

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
    public void testStorage() throws IOException, InterruptedException {
        MongodTestUtil mongodTestUtil = new MongodTestUtil();
        mongodTestUtil.startMongod();

        ThermostatMongoStorage.start(MongodTestUtil.mongoConfiguration);

        mongodTestUtil.waitForMongodStart();

        assertTrue(ThermostatMongoStorage.isConnected());

        mongodTestUtil.stopMongod();
        mongodTestUtil.waitForMongodStop();
        assertEquals(0, mongodTestUtil.process.exitValue());
    }

}
