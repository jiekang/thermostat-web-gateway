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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
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

public class SystemCPUIntegrationTest extends MongoIntegrationTest {

    private static final String collectionName = "cpu-info";
    private static final String serviceURL = baseUrl + "/system-cpu/0.0.1";
    private static final int HTTP_200_OK = 200;
    private static final int HTTP_404_NOTFOUND = 404;
    private static final String TIMESTAMP_TOKEN = "$TIMESTAMP$";
    private static final String AGENT_ID = getRandomSystemId();
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


    private static class TinyCPUInfo {
        TinyCPUInfo(String systemId, String agentId, int[] ppusage) {
            this.systemId = systemId;
            this.agentId = agentId;
            this.perProcessorUsage = ppusage;
        }
        String agentId;
        String systemId;
        int[] perProcessorUsage;
    }

    public SystemCPUIntegrationTest() {
        super(serviceURL, collectionName);
    }

    @Before
    public void beforeIntegrationTest() {
        mongodTestUtil.dropCollection(collectionName);
    }

    @Test
    public void testGetAll() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemid = getRandomSystemId();

        post(systemid);
        Thread.sleep(5);
        post(systemid);
        Thread.sleep(5);
        post(systemid);

        ContentResponse response = get(systemid);
        final List<TinyCPUInfo> list = parse(response, systemid);
        assertEquals(1, list.size());

        ContentResponse response2 = get(systemid, "?limit=2");
        final List<TinyCPUInfo> list2 = parse(response2, systemid);
        assertEquals(2, list2.size());

        ContentResponse response3 = get(systemid, "?limit=0");
        final List<TinyCPUInfo> list3 = parse(response3, systemid);
        assertEquals(3, list3.size());
    }

    @Test
    public void testGetAllFails() throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = client.newRequest(serviceURL).method(HttpMethod.GET).send();
        assertEquals(HTTP_404_NOTFOUND, response.getStatus());
    }

    @Test
    public void testGetUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemid = getRandomSystemId();
        getUnknown(systemid);
    }

    @Test
    public void testCreateOne() throws InterruptedException, TimeoutException, ExecutionException {

        final String systemid = getRandomSystemId();
        post(systemid);
        getKnown(systemid);
    }

    @Test
    public void testPut() throws InterruptedException, TimeoutException, ExecutionException {

        final String systemid = getRandomSystemId();
        final long timestamp = getTimestamp();

        // create it
        post(systemid);

        // retrieve it
        final ContentResponse response1 = getKnown(systemid);
        final List<TinyCPUInfo> list1 = parse(response1, systemid);
        assertEquals(1, list1.size());
        //assertEquals(CPU_STRING1, list1.get(0).cpuModel);

        // modify it
        put(systemid, timestamp+1);

        // ensure it was changed
        final ContentResponse response2 = getKnown(systemid);
        final List<TinyCPUInfo> list2 = parse(response2, systemid);
        assertEquals(1, list2.size());
        //assertEquals(timestamp+1, list2.get(0).????);
    }

    @Test
    public void testDeleteUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemid = getRandomSystemId();

        // delete it
        delete(systemid);
    }

    @Test
    public void testDeleteOne() throws InterruptedException, ExecutionException, TimeoutException {
        final String systemid = getRandomSystemId();

        // create the new record
        post(systemid);

        // check that it's there
        getKnown(systemid);

        // delete it
        delete(systemid);

        // check that it's not there
        getUnknown(systemid);
    }

    private ContentResponse post(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(serviceURL + "/systems/" + systemid);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.content(new StringContentProvider("[" + createJSON() + "]"));
        ContentResponse response = request.method(HttpMethod.POST).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private List<TinyCPUInfo> parse(ContentResponse contentResponse, final String expectedSystemId) {

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

    private ContentResponse put(final String systemid, final long ts) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(serviceURL + "/systems/" + systemid);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        final String contentStr = createJSON(ts);
        request.content(new StringContentProvider("{ \"set\" : " +contentStr + "}"));
        ContentResponse response = request.method(HttpMethod.PUT).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private ContentResponse getUnknown(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = get(systemid);
        assertTrue(parse(response, systemid).isEmpty());
        return response;
    }

    private ContentResponse getKnown(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = get(systemid);
        assertEquals(1, parse(response, systemid).size());
        return response;
    }

    private ContentResponse get(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(serviceURL + "/systems/" + systemid).method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    private ContentResponse get(final String systemid, final String query) throws InterruptedException, ExecutionException, TimeoutException {
        final Request rq = client.newRequest(serviceURL + "/systems/" + systemid + query);
        rq.method(HttpMethod.GET);
        ContentResponse response = rq.send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    private ContentResponse delete(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(serviceURL + "/systems/" + systemid).method(HttpMethod.DELETE).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private static String getRandomSystemId() {
        return UUID.randomUUID().toString();
    }

    private static long getTimestamp() {
        return java.lang.System.nanoTime();
    }

    private String createJSON() {
        return createJSON(getTimestamp());
    }

    private String createJSON(final long ts) {
        return cpuInfoJSON.replace(TIMESTAMP_TOKEN, Long.toString(ts));
    }
}
