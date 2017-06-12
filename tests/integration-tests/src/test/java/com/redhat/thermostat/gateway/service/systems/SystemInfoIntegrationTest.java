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

package com.redhat.thermostat.gateway.service.systems;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.thermostat.gateway.tests.integration.IntegrationTest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SystemInfoIntegrationTest extends IntegrationTest {

    private static final String collectionName = "system-info";
    private static final String serviceURL = baseUrl + "/systems/0.0.1";
    private static final int HTTP_200_OK = 200;
    private static final String CPU_STRING1 = "Intel";
    private static final String CPU_STRING2 = "AMD";
    private static final String AGENT_ID = getRandomSystemId();
    private static final String HOSTNAME = getRandomSystemId();
    private static final String systemInfoJSON =
            "{\n" +
            "  \"agentId\": \"" + AGENT_ID + "\",\n" +
            "  \"hostname\": \"" + HOSTNAME + "\",\n" +
            "  \"osName\": \"Windows 10\",\n" +
            "  \"osKernel\": \"10.0\",\n" +
            "  \"cpuModel\": \"" + CPU_STRING1 + "\",\n" +
            "  \"cpuCount\": 4,\n" +
            "  \"totalMemory\": {\n" +
            "    \"$numberLong\": \"12566220800\"\n" +
            "  }\n" +
            "}";

    private static class TinyHostInfo {
        TinyHostInfo(String sytehId, String agentId, String hostName, String cpuModel) {
            this.systemId = sytehId;
            this.agentId = agentId;
            this.hostName = hostName;
            this.cpuModel = cpuModel;
        }
        String hostName;
        String agentId;
        String systemId;
        String cpuModel;
    }

    public SystemInfoIntegrationTest() {
        super(serviceURL);
    }

    @Before
    public void beforeIntegrationTest() {
        mongodTestUtil.dropCollection(collectionName);
    }

    @Test
    public void testGetAll() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemid1 = getRandomSystemId();
        postSystemInfo(systemid1);

        final String systemid2 = getRandomSystemId();
        postSystemInfo(systemid2);

        ContentResponse response = client.newRequest(serviceURL).method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final List<TinyHostInfo> list = parseHostInfo(response, null);
        assertEquals(1, list.size());

        ContentResponse response2 = client.newRequest(serviceURL + "?limit=99").method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response2.getStatus());
        final List<TinyHostInfo> list2 = parseHostInfo(response2, null);
        assertEquals(2, list2.size());

        ContentResponse response3 = client.newRequest(serviceURL + "?limit=0").method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response3.getStatus());
        final List<TinyHostInfo> list3 = parseHostInfo(response3, null);
        assertEquals(2, list3.size());
    }

    @Test
    public void testGetAllEmpty() throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = client.newRequest(serviceURL).method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final List<TinyHostInfo> list = parseHostInfo(response, null);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemid = getRandomSystemId();
        getUnknownSystemInfo(systemid);
    }

    @Test
    public void testCreateOne() throws InterruptedException, TimeoutException, ExecutionException {

        final String systemid = getRandomSystemId();
        postSystemInfo(systemid);
        getKnownSystemInfo(systemid);
    }

    @Test
    public void testPut() throws InterruptedException, TimeoutException, ExecutionException {

        final String systemid = getRandomSystemId();

        // create it
        postSystemInfo(systemid);

        // retrieve it
        final ContentResponse response1 = getKnownSystemInfo(systemid);
        final List<TinyHostInfo> list1 = parseHostInfo(response1, systemid);
        assertEquals(1, list1.size());
        assertEquals(CPU_STRING1, list1.get(0).cpuModel);

        // modify it
        putSystemInfo(systemid, CPU_STRING2);

        // ensure it was changed
        final ContentResponse response2 = getKnownSystemInfo(systemid);
        final List<TinyHostInfo> list2 = parseHostInfo(response2, systemid);
        assertEquals(1, list2.size());
        assertEquals(CPU_STRING2, list2.get(0).cpuModel);
    }

    @Test
    public void testDeleteUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemid = getRandomSystemId();

        // delete it
        deleteSystemInfo(systemid);
    }

    @Test
    public void testDeleteOne() throws InterruptedException, ExecutionException, TimeoutException {
        final String systemid = getRandomSystemId();

        // create the new record
        postSystemInfo(systemid);

        // check that it's there
        getKnownSystemInfo(systemid);

        // delete it
        deleteSystemInfo(systemid);

        // check that it's not there
        getUnknownSystemInfo(systemid);
    }

    private ContentResponse postSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(serviceURL + "/systems/" + systemid);
        request.header("Content-Type", "application/json");
        request.content(new StringContentProvider("[" + createSystemInfoJSON(systemid) + "]"));
        ContentResponse response = request.method(HttpMethod.POST).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private List<TinyHostInfo> parseHostInfo(ContentResponse contentResponse, final String expectedSystemId) {

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(contentResponse.getContentAsString());
        JsonElement response = json.get("response");

        JsonArray allData = response.getAsJsonArray();
        List<TinyHostInfo> result = new ArrayList<>();

        for (JsonElement entry : allData) {

            json = (JsonObject) parser.parse(entry.toString());

            assertTrue(json.has("systemId"));
            assertTrue(json.has("agentId"));
            assertTrue(json.has("hostname"));
            assertTrue(json.has("cpuModel"));

            final String systemId = json.get("systemId").getAsString();
            final String agentId = json.get("agentId").getAsString();
            final String hostName = json.get("hostname").getAsString();
            final String cpuModel = json.get("cpuModel").getAsString();

            assertEquals(AGENT_ID, agentId);
            assertEquals(HOSTNAME, hostName);
            if (expectedSystemId != null) {
                assertEquals(expectedSystemId, systemId);
            }

            TinyHostInfo hi = new TinyHostInfo(systemId, agentId, hostName, cpuModel);

            result.add(hi);
        }
        return result;
    }

    private ContentResponse putSystemInfo(final String systemid, final String cpuid) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(serviceURL + "/systems/" + systemid);
        request.header("Content-Type", "application/json");
        final String contentStr = createSystemInfoJSON(systemid).replace(CPU_STRING1, cpuid);
        request.content(new StringContentProvider("{ \"set\" : " +contentStr + "}"));
        ContentResponse response = request.method(HttpMethod.PUT).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private ContentResponse getUnknownSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = getSystemInfo(systemid);
        assertTrue(parseHostInfo(response, systemid).isEmpty());
        return response;
    }

    private ContentResponse getKnownSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = getSystemInfo(systemid);
        assertEquals(1, parseHostInfo(response, systemid).size());
        return response;
    }

    private ContentResponse getSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(serviceURL + "/systems/" + systemid).method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    private ContentResponse deleteSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(serviceURL + "/systems/" + systemid).method(HttpMethod.DELETE).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private static String getRandomSystemId() {
        return UUID.randomUUID().toString();
    }

    private String createSystemInfoJSON(final String systemid) {
        return systemInfoJSON;
    }
}
