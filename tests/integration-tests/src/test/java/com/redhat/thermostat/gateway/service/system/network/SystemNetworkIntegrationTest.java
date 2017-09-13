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

package com.redhat.thermostat.gateway.service.system.network;

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

public class SystemNetworkIntegrationTest extends SystemIntegrationTestSuites<SystemNetworkIntegrationTest.TinyNetworkInfo> {

    private static final String collectionName = "network-info";
    private static final String versionString = "0.0.1";
    private static final String serviceName = "system-network";
    private static final String serviceURL = baseUrl + "/" + serviceName + "/" + versionString;
    private static final String memInfoJSON =
            "{\n" +
                    "   \"timeStamp\" : " + TIMESTAMP_TOKEN + ",\n" +
                    "   \"agentId\" : \"" + AGENT_ID + "\",\n" +
                    "   \"interfaces\":[\n" +
                    "      {\n" +
                    "         \"interfaceName\":\"lo\",\n" +
                    "         \"ip4Addr\":\"127.0.0.1\",\n" +
                    "         \"ip6Addr\":\"0:0:0:0:0:0:0:1\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "         \"interfaceName\":\"wlan1\",\n" +
                    "         \"ip4Addr\":null,\n" +
                    "         \"ip6Addr\":\"fe80:0:0:0:8814:46e7:ff0a:21cf%wlan1\"\n" +
                    "      }\n" +
                    "   ]\n" +
                    "}\n";

    static class TinyNetworkInfo {
        private String agentId;
        private String systemId;

        TinyNetworkInfo(String systemId, String agentId) {
            this.systemId = systemId;
            this.agentId = agentId;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getSystemId() {
            return systemId;
        }
    }

    public SystemNetworkIntegrationTest() {
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
    protected String createJSONTimeStamp(long ts) {
        return memInfoJSON.replace(TIMESTAMP_TOKEN, Long.toString(ts));
    }

    protected List<TinyNetworkInfo> parse(ContentResponse contentResponse, final String expectedSystemId) {

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(contentResponse.getContentAsString());
        JsonElement response = json.get("response");

        JsonArray allData = response.getAsJsonArray();
        List<TinyNetworkInfo> result = new ArrayList<>();

        for (JsonElement entry : allData) {

            json = (JsonObject) parser.parse(entry.toString());

            assertTrue(json.has("systemId"));
            assertTrue(json.has("agentId"));
            assertTrue(json.has("timeStamp"));

            final String systemId = json.get("systemId").getAsString();
            final String agentId = json.get("agentId").getAsString();
            //final long timeStamp = getLong(json, "timeStamp");

            assertEquals(AGENT_ID, agentId);
            if (expectedSystemId != null) {
                assertEquals(expectedSystemId, systemId);
            }

            TinyNetworkInfo hi = new TinyNetworkInfo(systemId, agentId);

            result.add(hi);
        }
        return result;
    }

    @Test
    public void testSystemNetworkGetAll() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetAll();
    }

    @Test
    public void testSystemNetworkGetAllFails() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetAllFails();
    }

    @Test
    public void testSystemNetworkGetUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        super.testGetUnknown();
    }

    @Test
    public void testSystemNetworkCreateOne() throws InterruptedException, TimeoutException, ExecutionException {
        super.testCreateOne();
    }

    @Test
    public void testSystemNetworkPutModifiesData() throws InterruptedException, TimeoutException, ExecutionException {
        super.testPutModifiesData();
    }

    @Test
    public void testSystemNetworkDeleteUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        super.testDeleteUnknown();
    }

    @Test
    public void testSystemNetworkDeleteOne() throws InterruptedException, ExecutionException, TimeoutException {
        super.testDeleteOne();
    }
}
