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

package com.redhat.thermostat.gateway.service.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.thermostat.gateway.service.system.SystemIntegrationTestSuites;

public class SystemCPUIntegrationTest extends SystemIntegrationTestSuites<SystemCPUIntegrationTest.TinyCPUInfo> {

    private static final String collectionName = "cpu-info";
    private static final String versionString = "0.0.1";
    private static final String serviceURL = baseUrl + "/system-cpu/" + versionString;

    private static final String cpuInfoJSON =
            "{\n" +
            "    \"perProcessorUsage\" : [ \n" +
            "        94.0, \n" +
            "        76.0, \n" +
            "        94.0, \n" +
            "        82.0\n" +
            "    ],\n" +
            "    \"timeStamp\" : " + TIMESTAMP_TOKEN + ",\n" +
            "    \"agentId\": \"" + AGENT_ID + "\",\n" +
            "}";


    static class TinyCPUInfo {
        private String agentId;
        private String systemId;
        private int[] perProcessorUsage;

        TinyCPUInfo(String systemId, String agentId, int[] ppusage) {
            this.systemId = systemId;
            this.agentId = agentId;
            this.perProcessorUsage = ppusage;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getSystemId() {
            return systemId;
        }
    }

    public SystemCPUIntegrationTest() {
        super(serviceURL, collectionName);
    }

    @Override
    protected String createJSONTimeStamp(long ts) {
        return cpuInfoJSON.replace(TIMESTAMP_TOKEN, Long.toString(ts));
    }

    protected List<TinyCPUInfo> parse(ContentResponse contentResponse, final String expectedSystemId) {

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(contentResponse.getContentAsString());
        JsonElement response = json.get("response");

        JsonArray allData = response.getAsJsonArray();
        List<TinyCPUInfo> result = new ArrayList<>();

        for (JsonElement entry : allData) {

            json = (JsonObject) parser.parse(entry.toString());

            assertTrue(json.has("systemId"));
            assertTrue(json.has("agentId"));
            assertTrue(json.has("timeStamp"));
            assertTrue(json.has("perProcessorUsage"));

            final String systemId = json.get("systemId").getAsString();
            final String agentId = json.get("agentId").getAsString();
            // TODO timestamp is special because it's long // final long timeStamp = json.get("timeStamp").getAsLong();
            final JsonArray usages = json.get("perProcessorUsage").getAsJsonArray();

            final int[] cpuUsages;

            // Deal with the case of a non-array value.
            if (usages == null) {
                cpuUsages = new int[0];
            } else {
                cpuUsages = new int[usages.size()];
                for (int i = 0; i < usages.size(); ++i) {
                    cpuUsages[i] = usages.get(i).getAsInt();
                }
            }
            assertEquals(AGENT_ID, agentId);
            if (expectedSystemId != null) {
                assertEquals(expectedSystemId, systemId);
            }

            TinyCPUInfo hi = new TinyCPUInfo(systemId, agentId, cpuUsages);

            result.add(hi);
        }
        return result;
    }

    @Test
    public void testSystemCPUGetAll() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetAll();
    }

    @Test
    public void testSystemCPUGetAllFails() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetAllFails();
    }

    @Test
    public void testSystemCPUGetUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetUnknown();
    }

    @Test
    public void testSystemCPUCreateOne() throws InterruptedException, TimeoutException, ExecutionException {
        super.testCreateOne();
    }

    @Test
    public void testSystemCPUPutModifiesData() throws InterruptedException, TimeoutException, ExecutionException {
        super.testPutModifiesData();
    }

    @Test
    public void testSystemCPUDeleteUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        super.testDeleteUnknown();
    }

    @Test
    public void testSystemCPUDeleteOne() throws InterruptedException, ExecutionException, TimeoutException {
        super.testDeleteOne();
    }

}
