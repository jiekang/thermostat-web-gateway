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


package com.redhat.thermostat.gateway.service.jvm.gc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.redhat.thermostat.gateway.common.core.auth.basic.BasicRealmAuthorizer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.thermostat.gateway.common.mongodb.servlet.RequestParameters;
import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import org.bson.Document;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class JvmGcServiceIntegrationTest extends MongoIntegrationTest {

    private static final String serviceName = "jvm-gc";
    private static final String versionNumber = "0.0.3";
    private static final int HTTP_200_OK = 200;
    private static final int HTTP_404_NOTFOUND = 404;
    private static final String NO_EXPECTED_RESPONSE = "";
    private static final String NO_QUERY = "";
    private static final String NO_DATA_TO_SEND = "";
    private static final String NO_DATA_TYPE = "";

    private final String data = "[{ \"a\" : \"test\", \"b\" : \"test1\", \"c\" : \"test2\" }, { \"d\" : \"test3\"}," +
            "{\"e\" : \"test4\" }]";

    private final String jsonData =
            "{\n" +
            "   \"timeStamp\" : " + TIMESTAMP_TOKEN + ",\n" +
            "   \"jvmId\" : " + JVMID_TOKEN + ",\n" +
            "   \"collectorName\" : \"some-collection\",\n" +
            "   \"runCount\" : \"22\",\n" +
            "   \"wallTimeInMicros\" : \"333333\"\n" +
            "}\n";

    private static final String QUERY_PREFIX = "query";
    private static final String LIMIT_PREFIX = "limit";
    private static final String SORT_PREFIX = "sort";
    private static final String OFFSET_PREFIX = "offset";
    private static final String METADATA_PREFIX = "metadata";
    private static final String INCLUDE_PREFIX = "include";
    private static final String EXCLUDE_PREFIX = "exclude";
    private static final String TIMESTAMP_TOKEN = "\"$TIMESTAMP$\"";
    private static final String JVMID_TOKEN = "\"$JVMID_TOKEN\"";

    private final String AGENT_ID = getRandomSystemId();
    private final String JVM_ID = getRandomJvmId();
    private long timeStamp = java.lang.System.nanoTime();

    private final String simpleUrl = baseUrl + "/" + serviceName + "/" + versionNumber;
    private final String serviceUrl = simpleUrl + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;
    private final String SYSTEM_JVM_FRAGMENT = ",\"systemId\":\"" + AGENT_ID + "\",\"jvmId\":\"" + JVM_ID + "\"";

    public JvmGcServiceIntegrationTest() {
        super(serviceName + "/" + versionNumber, serviceName);
    }

    private void makeHttpGetRequest(String url, String expectedResponse, int expectedStatus)
            throws InterruptedException, TimeoutException, ExecutionException {

        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();

        if (!expectedResponse.equals("")) {
            assertEquals(expectedStatus, response.getStatus());
            assertEquals(expectedResponse, response.getContentAsString());
        }
    }

    private void makeHttpMethodRequest(HttpMethod httpMethod, String urlQuery, String dataToSend, String dataType,
                                       String expectedResponse, int expectedStatus)
            throws InterruptedException, TimeoutException, ExecutionException {

        StringContentProvider stringContentProvider = new StringContentProvider(dataToSend, "UTF-8");
        ContentResponse postResponse = client.newRequest(serviceUrl + urlQuery).method(httpMethod)
                .content(stringContentProvider, dataType)
                .send();
        assertEquals(expectedStatus, postResponse.getStatus());

        if (!expectedResponse.equals("")) {
            if (httpMethod == HttpMethod.DELETE) {
                assertEquals(expectedResponse, postResponse.getContentAsString());
            } else {
                makeHttpGetRequest(serviceUrl, expectedResponse, expectedStatus);
            }
        }
    }

    @Test
    public void testGetUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        getUnknown(getRandomSystemId(), getRandomJvmId());
    }

    @Test
    public void testCreateOne() throws InterruptedException, TimeoutException, ExecutionException {

        final String systemid = getRandomSystemId();
        final String jvmid = getRandomJvmId();
        post(systemid, jvmid);
        getKnown(systemid, jvmid);
    }

    @Test
    public void testPut() throws InterruptedException, TimeoutException, ExecutionException {

        final String systemid = getRandomSystemId();
        final String jvmid = getRandomJvmId();

        final long timestamp = getTimestamp();

        // create it
        post(systemid, jvmid);

        // retrieve it
        final ContentResponse response1 = getKnown(systemid, jvmid);
        final List<TinyJvmGc> list1 = parse(response1, jvmid);
        assertEquals(1, list1.size());

        // modify it
        put(systemid, jvmid, timestamp+1);

        // ensure it was changed
        final ContentResponse response2 = getKnown(systemid, jvmid);
        final List<TinyJvmGc> list2 = parse(response2, jvmid);
        assertEquals(1, list2.size());
    }

    @Test
    public void testDeleteUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemid = getRandomSystemId();
        final String jvmid = getRandomJvmId();
        // delete it
        delete(systemid, jvmid);
    }

    @Test
    public void testDeleteOne() throws InterruptedException, ExecutionException, TimeoutException {
        final String systemid = getRandomSystemId();
        final String jvmid = getRandomJvmId();

        // create the new record
        post(systemid, jvmid);

        // check that it's there
        getKnown(systemid, jvmid);

        // delete it
        delete(systemid, jvmid);

        // check that it's not there
        getUnknown(systemid, jvmid);
    }

    @Test
    public void testGet() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        final String expectedResponse = "{\"response\":[{\"a\":\"test\",\"b\":\"test1\","+
                "\"c\":\"test2\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpGetRequest(serviceUrl, expectedResponse, 200);
    }

    @Test
    public void testGetLimitParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        final String expectedResponse = "{\"response\":[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"d\":\"test3\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpGetRequest(serviceUrl + '?' + LIMIT_PREFIX + "=2", expectedResponse, 200);
    }

    @Test
    public void testGetSortParam() throws InterruptedException, TimeoutException, ExecutionException {
        final String data ="[{\"a\":\"1\"}, {\"a\":\"2\"}]";
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        final String expectedResponse = "{\"response\":[{\"a\":\"2\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"a\":\"1\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpGetRequest(serviceUrl + '?' + LIMIT_PREFIX + "=2&" + SORT_PREFIX + "=-a", expectedResponse, 200);
    }

    @Test
    public void testGetProjectParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        final String expectedResponse = "{\"response\":[{\"b\":\"test1\",\"c\":\"test2\"}]}";
        makeHttpGetRequest(serviceUrl + '?' + INCLUDE_PREFIX + "=b,c", expectedResponse, 200);
    }

    @Test
    public void testGetOffsetParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        final String expectedResponse = "{\"response\":[{\"d\":\"test3\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpGetRequest(serviceUrl + '?' + OFFSET_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetQueryParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        final String expectedResponse = "{\"response\":[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1", expectedResponse, 200);
    }

    @Test
    public void testPostJSON() throws InterruptedException, TimeoutException, ExecutionException {
        final String expectedResponse = "{\"response\":[{\"f1\":\"test\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpMethodRequest(HttpMethod.POST, '?' + METADATA_PREFIX + "=false", "[{\"f1\":\"test\"}]", "application/json",
                 expectedResponse, 200);
    }

    @Test
    public void testPostXML() throws InterruptedException,TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, NO_DATA_TO_SEND, "application/xml", NO_EXPECTED_RESPONSE, 415);
    }

    @Test
    public void testPostWithMetaData() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, "[{\"f1\":\"test\"}]", "application/json", NO_EXPECTED_RESPONSE, 200);
        StringContentProvider stringContentProvider = new StringContentProvider("[{\"f1\":\"test\"}]", "UTF-8");
        ContentResponse response = client.newRequest(serviceUrl + "?" + METADATA_PREFIX + "=true")
                .method(HttpMethod.POST)
                .content(stringContentProvider, "application/json")
                .send();

        assertEquals(200, response.getStatus());

        assertEquals("{\"metaData\":{\"insertCount\":1}}", response.getContentAsString());
    }

    @Test
    public void testInvalidDataPost() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, "{\"badFormat\":\"missing square brackets\"}",
                "application/json", NO_EXPECTED_RESPONSE, 400);
    }

    @Test
    public void testDelete() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.DELETE, NO_QUERY, NO_DATA_TO_SEND, NO_DATA_TYPE, NO_EXPECTED_RESPONSE, 200);
    }

    @Test
    public void testDeleteWithMetaData() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, "[{\"f1\":\"test\"}]", "application/json", NO_EXPECTED_RESPONSE, 200);

        makeHttpMethodRequest(HttpMethod.DELETE, "?" + QUERY_PREFIX + "=f1==test&" + METADATA_PREFIX + "=true",
                NO_DATA_TO_SEND, NO_DATA_TYPE, "{\"metaData\":{\"matchCount\":1}}", 200);
    }

    @Test
    public void testNonExistentDataDelete() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.DELETE, '?' + QUERY_PREFIX + "=nonExist==Null", NO_DATA_TO_SEND, NO_DATA_TYPE,
                NO_EXPECTED_RESPONSE, 200);
    }

    @Test
    public void testPostPutDeleteMockedData() throws InterruptedException, TimeoutException, ExecutionException {
        final String expectedResponse = "{\"response\":[{\"f1\":\"test\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, "[{\"f1\":\"test\"}]", "application/json",
                 expectedResponse, 200);

        final String expectedResponse2 = "{\"response\":[{\"f1\":\"newdata\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        makeHttpMethodRequest(HttpMethod.PUT, '?' + QUERY_PREFIX + "=f1==test", "{\"set\": {\"f1\":\"newdata\"}}",
                "application/json", expectedResponse2, 200);

        makeHttpMethodRequest(HttpMethod.DELETE, "?q=f1==test", NO_DATA_TO_SEND, NO_DATA_TYPE, NO_EXPECTED_RESPONSE, 200);
    }

    @Test
    public void testPutWithMetaData() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, "[{\"f1\":\"test\"}]", "application/json", NO_EXPECTED_RESPONSE, 200);
        StringContentProvider stringContentProvider = new StringContentProvider("{\"set\": {\"f1\":\"newdata\"}}", "UTF-8");
        ContentResponse response = client
                .newRequest(serviceUrl + "?" + QUERY_PREFIX + "=f1==test&" + METADATA_PREFIX + "=true")
                .method(HttpMethod.PUT)
                .content(stringContentProvider, "application/json")
                .send();

        assertEquals(200, response.getStatus());
        assertEquals("{\"metaData\":{\"matchCount\":1}}", response.getContentAsString());


    }
    @Test
    public void testGetWithMetaDataAndLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}"],
        //"metaData":{"payloadCount":1,"count":3,
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=1&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"" + SYSTEM_JVM_FRAGMENT + "}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":3,"+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d1\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX  +"\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetNoPatchNumber() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}"],
        //"metaData":{"payloadCount":1,"count":3,
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=1&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"" + SYSTEM_JVM_FRAGMENT + "}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":3,"+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d1\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX  +"\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);

        final String url1 =  baseUrl + "/" + serviceName + "/" + "0.0" + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;
        makeHttpGetRequest(url1 + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", expectedResponse, 200);
        final String url2 =  baseUrl + "/" + serviceName + "/" + "0.0.2" + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;
        makeHttpGetRequest(url2 + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", expectedResponse, 200);
        final String url3 =  baseUrl + "/" + serviceName + "/" + "0.0.4" + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;
        makeHttpGetRequest(url3 + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", "", 404);

    }

    @Test
    public void testGetWithMetaDataAndOffset() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],"metaData":{"payloadCount":1,"count":3,
        //"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":3,"+
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\","+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d2\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatPrevOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d1\","+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=3", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatPrevOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":3,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":3,"+
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\","+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d2\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1&" + OFFSET_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextOffsetLessThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"e\":\"test4\",\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":2,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=1&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=3&l=2&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"e\":\"test4\",\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d3\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":6,
        //"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d1\","+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=3", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":2,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d4\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=2", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":2,"count":6,
        //"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
            "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d4\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=2", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatPrevExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";


        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},"+
                "{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":6,"+
                "\"prev\":\"" + serviceUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
                "\"next\":\"" + serviceUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);
        makeHttpGetRequest(serviceUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=3&" + OFFSET_PREFIX + "=2", expectedResponse, 200);
    }

    @Test
    public void testUpdateDoesNotAffectRealms() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String updateString = "{\"set\" : {\"realms\" : 1, \"a\" : 2}}";
        makeHttpMethodRequest(HttpMethod.PUT, NO_QUERY, updateString, "application/json", NO_EXPECTED_RESPONSE, 200);

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals("[\"a\",\"b\"]", gson.toJson(document.get("realms"), listType));
            }
        });
    }

    @Test
    public void testUpdateOnlyRealmsDoesNotAffectRealms() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"item\":1,}," +
                "{\"item\":2}]";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);

        String updateString = "{\"set\" : {\"realms\" : 1}}";
        makeHttpMethodRequest(HttpMethod.PUT, NO_QUERY, updateString,"application/json", NO_EXPECTED_RESPONSE, 400);

        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals(getRealmArray(BasicRealmAuthorizer.DEFAULT_REALM), gson.toJson(document.get("realms"), listType));
            }
        });
    }

    @Test
    public void testUpdateRealmsQueryMatchIsNotUsed() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\"]}," +
                "{\"item\":2,\"realms\":[\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String updateString = "{\"set\" : {\"item\" : 5}}";
        StringContentProvider stringContentProvider = new StringContentProvider(updateString, "UTF-8");
        ContentResponse response = client.newRequest(serviceUrl).param(RequestParameters.QUERY, "realms==[\"a\"]")
                .method(HttpMethod.PUT).content(stringContentProvider, "application/json")
                .send();
        assertEquals(400, response.getStatus());

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                if (document.get("item", Double.class).equals(5)) {
                    fail();
                }
            }
        });
    }

    @Test
    public void testUpdateMultipleRealmsDoesNotAffectRealms() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"item\":1,}," +
                "{\"item\":2}]";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);

        String updateString = "{\"set\" : {\"realms\" : 1, \"realms\" : 2}}";
        makeHttpMethodRequest(HttpMethod.PUT, NO_QUERY, updateString, "application/json", NO_EXPECTED_RESPONSE, 400);

        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals(getRealmArray(BasicRealmAuthorizer.DEFAULT_REALM), gson.toJson(document.get("realms"), listType));
            }
        });
    }

    @Test
    public void testGetCannotSeeRealms() throws InterruptedException, ExecutionException, TimeoutException {
        String data = "[{\"item\":1},{\"item\":2}]";
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);

        String expected = "{\"response\":[{\"item\":1" + SYSTEM_JVM_FRAGMENT + "}]}";

        makeHttpGetRequest(serviceUrl, expected, 200);
    }

    @Test
    public void testGetProjectionCannotSeeRealms() throws InterruptedException, ExecutionException, TimeoutException {
        String data = "[{\"item\":1},{\"item\":2}]";
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);

        String expected = "{\"response\":[{\"item\":1}]}";

        makeHttpGetRequest(serviceUrl + "?" + RequestParameters.INCLUDE + "=realms,item",expected, 200);
    }

    @Test
    public void testGetQueryCannotMatchRealms() throws InterruptedException, ExecutionException, TimeoutException {
        String data = "[{\"item\":1},{\"item\":2}]";
        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);

        ContentResponse response = client.newRequest(serviceUrl)
                .param(RequestParameters.QUERY, "realms==" + getRealmArray(BasicRealmAuthorizer.DEFAULT_REALM))
                .method(HttpMethod.GET).send();

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testPostCannotAddRealms() throws InterruptedException, ExecutionException, TimeoutException {
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";

        makeHttpMethodRequest(HttpMethod.POST, NO_QUERY, data, "application/json", NO_EXPECTED_RESPONSE, 200);

        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals(getRealmArray(BasicRealmAuthorizer.DEFAULT_REALM), gson.toJson(document.get("realms"), listType));
            }
        });
    }

    @Test
    public void testDeleteCannotQueryRealms() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        ContentResponse response = client.newRequest(serviceUrl)
                .param(RequestParameters.QUERY, "realms==[\"a\",\"b\"]").method(HttpMethod.DELETE)
                .send();
        assertEquals(400, response.getStatus());

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals("[\"a\",\"b\"]", gson.toJson(document.get("realms"), listType));
            }
        });
    }

    private String getRealmArray(String... realms) {
        if (realms != null) {
            StringBuilder arrayBuilder = new StringBuilder();
            arrayBuilder.append("[");

            for (String s : realms) {
                arrayBuilder.append("\"" + s + "\",");
            }
            arrayBuilder.deleteCharAt(arrayBuilder.length() - 1);
            arrayBuilder.append("]");

            return arrayBuilder.toString();
        } else {
            return "[]";
        }
    }


    private static String getRandomSystemId() {
        return UUID.randomUUID().toString();
    }

    private static String getRandomJvmId() {
        return UUID.randomUUID().toString();
    }

    private String createJSON() {
        return createJSON(getTimestamp());
    }

    private long getTimestamp() {
        timeStamp += 1;
        return timeStamp;
    }

    private String createJSON(final long ts) {
        return jsonData.replace(TIMESTAMP_TOKEN, Long.toString(ts));
    }

    private ContentResponse put(final String systemid, final String jvmid, final long ts) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(simpleUrl + "/systems/" + systemid + "/jvms/" + jvmid);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        final String contentStr = createJSON(ts);
        request.content(new StringContentProvider("{ \"set\" : " +contentStr + "}"));
        ContentResponse response = request.method(HttpMethod.PUT).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private ContentResponse post(final String systemid, final String jvmid) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(simpleUrl + "/systems/" + systemid + "/jvms/" + jvmid);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.content(new StringContentProvider( '[' + createJSON() + ']'));
        ContentResponse response = request.method(HttpMethod.POST).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    private ContentResponse getUnknown(final String systemid, final String jvmid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = get(systemid, jvmid);
        assertTrue(parse(response, jvmid).isEmpty());
        return response;
    }

    private ContentResponse getKnown(final String systemid, final String jvmid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = get(systemid, jvmid);
        assertEquals(1, parse(response, jvmid).size());
        return response;
    }

    private ContentResponse get(final String systemid, final String jvmid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(simpleUrl + "/systems/" + systemid + "/jvms/" + jvmid).method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    private ContentResponse get(final String systemid, final String jvmid, final String query) throws InterruptedException, ExecutionException, TimeoutException {
        final Request rq = client.newRequest(simpleUrl + "/systems/" + systemid + "/jvms/" + jvmid + query);
        rq.method(HttpMethod.GET);
        ContentResponse response = rq.send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    private ContentResponse delete(final String systemid, final String jvmid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(simpleUrl + "/systems/" + systemid + "/jvms/" + jvmid).method(HttpMethod.DELETE).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    class TinyJvmGc {
        String jvmId;
        long timeStamp;
        TinyJvmGc(String jvmId, long ts) {
            this.jvmId = jvmId;
            this.timeStamp = ts;
        }
    }
    private List<TinyJvmGc> parse(ContentResponse contentResponse, final String expectedJvmId) {

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(contentResponse.getContentAsString());
        JsonElement response = json.get("response");

        JsonArray allData = response.getAsJsonArray();
        List<TinyJvmGc> result = new ArrayList<>();

        for (JsonElement entry : allData) {

            json = (JsonObject) parser.parse(entry.toString());

            assertTrue(json.has("jvmId"));
            //assertTrue(json.has("agentId"));
            assertTrue(json.has("timeStamp"));

            final String jvmId = json.get("jvmId").getAsString();
            //final String agentId = json.get("agentId").getAsString();
            //final long timeStamp = getLong(json, "timeStamp");

            if (expectedJvmId != null) {
                assertEquals(expectedJvmId, jvmId);
            }

            TinyJvmGc hi = new TinyJvmGc(jvmId, 0);

            result.add(hi);
        }
        return result;
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
}
