package com.redhat.thermostat.server.core.storage;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.redhat.thermostat.server.core.internal.storage.mongo.ThermostatMongoStorage;
import com.redhat.thermostat.test.util.MongodTestUtil;

public class ThermostatMongoStorageTest {

    @Test
    public void testNoStorage() {
        ThermostatMongoStorage.start(MongodTestUtil.timeoutMongoConfiguration);

        assertFalse(ThermostatMongoStorage.isConnected());

        ThermostatMongoStorage.finish();
    }

    @Test
    public void testStorage() throws IOException, InterruptedException {
        MongodTestUtil mongodTestUtil = new MongodTestUtil();
        mongodTestUtil.startMongod();

        ThermostatMongoStorage.start(MongodTestUtil.mongoConfiguration);

        assertTrue(mongodTestUtil.waitForMongodStart());
        assertTrue(ThermostatMongoStorage.isConnected());

        mongodTestUtil.stopMongod();

        assertTrue(mongodTestUtil.waitForMongodStop());
        assertEquals(0, mongodTestUtil.process.exitValue());

        ThermostatMongoStorage.finish();
        mongodTestUtil.finish();
    }

}
