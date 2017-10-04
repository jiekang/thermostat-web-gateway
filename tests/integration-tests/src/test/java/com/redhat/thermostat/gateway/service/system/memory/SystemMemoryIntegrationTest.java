/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.gateway.service.system.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.redhat.thermostat.gateway.common.mongodb.response.MongoGsonFactory;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.thermostat.gateway.service.system.SystemIntegrationTestSuites;
import com.redhat.thermostat.gateway.tests.integration.VersionTestUtil;


public class SystemMemoryIntegrationTest extends SystemIntegrationTestSuites<SystemMemoryIntegrationTest.TinyMemoryInfo> {

    private static final String collectionName = "memory-info";
    private static final String versionString = "0.0.1";
    private static final String serviceName = "system-memory";
    private static final String serviceURL = baseUrl + "/" + serviceName + "/" + versionString;
    private static final String TIMESTAMP_TOKEN = "$TIMESTAMP$";
    private static final String AGENT_ID = getRandomSystemId();
    private static final String memInfoJSON =
            "{\n" +
            "    \"total\" : { \"$numberLong\" : \"12566220800\" },\n" +
            "    \"free\" : { \"$numberLong\" : \"23677331911\" },\n" +
            "    \"buffers\" : { \"$numberLong\" : \"0\" },\n" +
            "    \"cached\" : { \"$numberLong\" : \"0\" },\n" +
            "    \"swapTotal\" : { \"$numberLong\" : \"17666494464\" },\n" +
            "    \"swapFree\" : { \"$numberLong\" : \"3524055040\" },\n" +
            "    \"commitLimit\" : { \"$numberLong\" : \"0\" },\n" +
            "    \"timestamp\" : { \"$numberLong\" : \"" + TIMESTAMP_TOKEN + "\" },\n" +
            "    \"agentId\" : \"" + AGENT_ID + "\",\n" +
            "}";

    public SystemMemoryIntegrationTest() {
        super(serviceURL, collectionName);
    }

    @Override
    public String getServiceVersion() {
        return versionString;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Test
    public void testVersions() throws Exception {
        final String systemid = super.getRandomSystemId();
        post(systemid);
        VersionTestUtil.testAllVersions(baseUrl + "/system-memory", versionString, "/systems/" + systemid);
    }

    @Override
    protected List<TinyMemoryInfo> parse(ContentResponse contentResponse, final String expectedSystemId) {
        List<SystemMemoryIntegrationTest.TinyMemoryInfo> result = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(contentResponse.getContentAsString());
        JsonElement response = json.get("response");

        JsonArray allData = response.getAsJsonArray();

        Gson gson = MongoGsonFactory.getGson();
        for (int entryIndex = 0; entryIndex < allData.size(); entryIndex++) {
            TinyMemoryInfo tinyMemoryEntry = gson.fromJson(allData.get(entryIndex).toString(), TinyMemoryInfo.class);

            assertEquals(AGENT_ID, tinyMemoryEntry.getAgentId());
            if (expectedSystemId != null) {
                assertEquals(expectedSystemId, tinyMemoryEntry.getSystemId());
            }
            assertEquals(12566220800L, tinyMemoryEntry.getTotal());
            assertEquals(23677331911L, tinyMemoryEntry.getFree());
            assertEquals(0L, tinyMemoryEntry.getBuffers());
            assertEquals(0L, tinyMemoryEntry.getCached());
            assertEquals(17666494464L, tinyMemoryEntry.getSwapTotal());
            assertEquals(3524055040L, tinyMemoryEntry.getSwapFree());
            assertEquals(0L, tinyMemoryEntry.getCommitLimit());
            assertNotEquals(0L, tinyMemoryEntry.getTimestamp());

            result.add(tinyMemoryEntry);
        }

        return result;
    }

    @Override
    protected String createJSONTimeStamp(final long ts) {
        return memInfoJSON.replace(TIMESTAMP_TOKEN, Long.toString(ts));
    }

    @Test
    public void testSystemMemoryGetAll() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetAll();
    }

    @Test
    public void testSystemMemoryGetAllFails() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetAllFails();
    }

    @Test
    public void testSystemMemoryGetUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetUnknown();
    }

    @Test
    public void testSystemMemoryCreateOne() throws InterruptedException, TimeoutException, ExecutionException {
        super.testCreateOne();
    }

    @Test
    public void testSystemMemoryPutModifiesData() throws InterruptedException, TimeoutException, ExecutionException {
        super.testPutModifiesData();
    }

    @Test
    public void testSystemMemoryDeleteUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        super.testDeleteUnknown();
    }

    @Test
    public void testSystemMemoryDeleteOne() throws InterruptedException, ExecutionException, TimeoutException {
        super.testDeleteOne();
    }

    static class TinyMemoryInfo {
        private String agentId;
        private String systemId;
        private Long timestamp;
        private Long total;
        private Long free;
        private Long buffers;
        private Long cached;
        private Long swapTotal;
        private Long swapFree;
        private Long commitLimit;

        public String getAgentId() {
            return agentId;
        }

        public String getSystemId() {
            return systemId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public long getTotal() {
            return total;
        }

        public long getFree() {
            return free;
        }

        public long getBuffers() {
            return buffers;
        }

        public long getCached() {
            return cached;
        }

        public long getSwapTotal() {
            return swapTotal;
        }

        public long getSwapFree() {
            return swapFree;
        }

        public long getCommitLimit() {
            return commitLimit;
        }
    }
}
