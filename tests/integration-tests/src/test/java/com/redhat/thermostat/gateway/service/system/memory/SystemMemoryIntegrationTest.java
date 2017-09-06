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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
    private static final String serviceURL = baseUrl + "/system-memory/" + versionString;
    private static final String TIMESTAMP_TOKEN = "$TIMESTAMP$";
    private static final String AGENT_ID = getRandomSystemId();
    private static final String memInfoJSON =
            "{\n" +
            "    \"total\" : 12566220800,\n" +
            "    \"free\" : 2338582528,\n" +
            "    \"buffers\" : 0,\n" +
            "    \"cached\" : 0,\n" +
            "    \"swapTotal\" : 17666494464,\n" +
            "    \"swapFree\" : 3524055040,\n" +
            "    \"commitLimit\" : 0,\n" +
            "    \"timeStamp\" : " + TIMESTAMP_TOKEN + ",\n" +
            "    \"agentId\" : \"" + AGENT_ID + "\",\n" +
            "}";

    static class TinyMemoryInfo {
        private String agentId;
        private String systemId;
        private long total;
        private long free;
        private long buffers;

        TinyMemoryInfo(String systemId, String agentId, long total, long free, long buffers) {
            this.systemId = systemId;
            this.agentId = agentId;
            this.total = total;
            this.free = free;
            this.buffers = buffers;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getSystemId() {
            return systemId;
        }
    }

    public SystemMemoryIntegrationTest() {
        super(serviceURL, collectionName);
    }

    @Test
    public void testVersions() throws Exception {
        final String systemid = super.getRandomSystemId();
        post(systemid);
        VersionTestUtil.testAllVersions(baseUrl + "/system-memory", versionString, "/systems/" + systemid);
    }

    private static long getLong(JsonObject json, final String id) {
        JsonElement el = json.get(id);
        if (el.isJsonObject()) {
            final JsonObject o = el.getAsJsonObject();
            return o.get("$numberLong").getAsLong();
        } else {
            return el.getAsLong();
        }
    }

    @Override
    protected List<TinyMemoryInfo> parse(ContentResponse contentResponse, final String expectedSystemId) {
        List<SystemMemoryIntegrationTest.TinyMemoryInfo> result = new ArrayList<>();

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(contentResponse.getContentAsString());
        JsonElement response = json.get("response");

        JsonArray allData = response.getAsJsonArray();

        Gson gson = new Gson();
        for (JsonElement entry : allData) {
            TinyMemoryInfo TinyMemoryEntry = gson.fromJson(entry.toString(), TinyMemoryInfo.class);

            assertEquals(AGENT_ID, TinyMemoryEntry.getAgentId());
            if (expectedSystemId != null) {
                assertEquals(expectedSystemId, TinyMemoryEntry.getSystemId());
            }

            result.add(TinyMemoryEntry);
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
}
