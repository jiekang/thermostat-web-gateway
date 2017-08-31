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

package com.redhat.thermostat.gateway.service.jvm.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import com.redhat.thermostat.gateway.tests.utils.HttpTestUtil;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmIoServiceIntegrationTest extends MongoIntegrationTest {

    private static final String serviceName = "jvm-io";
    private static final String versionNumber = "0.0.1";

    private static final int HTTP_200_OK = 200;
    private static final int HTTP_404_NOTFOUND = 404;

    private static final String QUERY_PREFIX = "query";
    private static final String LIMIT_PREFIX = "limit";
    private static final String SORT_PREFIX = "sort";
    private static final String OFFSET_PREFIX = "offset";
    private static final String METADATA_PREFIX = "metadata";
    private static final String INCLUDE_PREFIX = "include";

    private static final String TIMESTAMP_TOKEN = "\"$TIMESTAMP$\"";
    private static final String JVMID_TOKEN = "\"$JVMID_TOKEN\"";

    private final String AGENT_ID = getRandomSystemId();
    private final String JVM_ID = getRandomJvmId();
    private long timeStamp = java.lang.System.nanoTime();
    private final String SYSTEM_JVM_FRAGMENT = ",\"systemId\":\"" + AGENT_ID + "\",\"jvmId\":\"" + JVM_ID + "\"";

    private final String jsonData =
            "{\n" +
                    "   \"timeStamp\" : " + TIMESTAMP_TOKEN + ",\n" +
                    "   \"jvmId\" : " + JVMID_TOKEN + ",\n" +
                    "   \"metaspaceMaxCapacity\" : \"1000\",\n" +
                    "   \"metaspaceMinCapacity\" : \"22\",\n" +
                    "   \"metaspaceCapacity\" : \"777\",\n" +
                    "   \"metaspaceUsed\" : \"77\"\n" +
                    "}\n";

    private final String simpleUrl = baseUrl + "/" + serviceName + "/" + versionNumber;
    private final String serviceUrl = simpleUrl + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;

    private final String returnedUrl;

    public JvmIoServiceIntegrationTest() {
        super(serviceName + "/" + versionNumber, serviceName);
        this.returnedUrl = serviceUrl;
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
        final List<TinyJvmIo> list1 = parse(response1, jvmid);
        assertEquals(1, list1.size());

        // modify it
        put(systemid, jvmid, timestamp+1);

        // ensure it was changed
        final ContentResponse response2 = getKnown(systemid, jvmid);
        final List<TinyJvmIo> list2 = parse(response2, jvmid);
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
    public void testGetWithUnsupportedQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?xyz=5", 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testDefaultLimitOne() throws InterruptedException, TimeoutException, ExecutionException {
        String expected = "{\"response\":[{\"a\":\"b\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"},{\"a\":\"d\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?s=+a&l=", 200, expected);
    }

    @Test
    public void testGetWithCommaQuery() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedFirst = "{\"response\":[{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedAll = "{\"response\":[{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"" + SYSTEM_JVM_FRAGMENT + "},{\"x\":\"y\"" + SYSTEM_JVM_FRAGMENT + "},{\"z\":\"z\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"},{\"x\":\"y\"},{\"z\":\"z\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedAll);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=a==b", 200, expectedFirst);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=c==d", 200, expectedFirst);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=a==b,c==d", 200, expectedFirst);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=a==b,c==none", 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testGetWithAmpersandQuery() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedAll = "{\"response\":[{\"a\":\"b\"" + SYSTEM_JVM_FRAGMENT + "},{\"c\":\"d\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedAmpersand = "{\"response\":[{\"a\":\"b\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"},{\"c\":\"d\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedAll);

        // Since q=a==b&c==d means "q= 'a==b'" and "c= '=d'", we should only
        // get 'a: b' back.
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=a==b&c==d", 200, expectedAmpersand);

        // The following should find the one with multiple matches, not the
        // single match. This means we will evaluate u==v only, and should get
        // back the first one it finds since no limit query is specified.
        String expectedSingleMatch = "{\"response\":[{\"u\":\"v\",\"x\":\"y\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedMultiMatch = "{\"response\":[{\"u\":\"v\",\"x\":\"y\"" + SYSTEM_JVM_FRAGMENT + "},{\"u\":\"v\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"x\":\"y\"},{\"u\":\"v\",\"x\":\"y\"},{\"u\":\"v\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=u==v&" + QUERY_PREFIX + "=x==y", 200, expectedSingleMatch);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=u==v&" + QUERY_PREFIX + "=x==y&" + LIMIT_PREFIX + "=5", 200, expectedMultiMatch);
    }

    @Test
    public void testMultiplePosts() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedResponse = "{\"response\":[{\"fakedata\":\"test\"" + SYSTEM_JVM_FRAGMENT + "},{\"new\":\"data\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"new\":\"data\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedResponse);
    }

    @Test
    public void testPostPutAddsData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataBeforePut = "{\"response\":[{\"a\":\"b\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedDataAfterPut = "{\"response\":[{\"a\":\"b\"" + SYSTEM_JVM_FRAGMENT + ",\"x\":\"y\"}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"},{\"a\":\"c\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedDataBeforePut);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl + "?" + QUERY_PREFIX + "=a==b", "{\"set\":{\"x\":\"y\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedDataAfterPut);
    }

    @Test
    public void testPostPutModifiesData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataBeforePut = "{\"response\":[{\"a\":\"b\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedDataAfterPut = "{\"response\":[{\"a\":\"c\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedAllDataAfterPut = "{\"response\":[{\"a\":\"c\"" + SYSTEM_JVM_FRAGMENT + "},{\"x\":\"y\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"},{\"x\":\"y\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedDataBeforePut);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl + "?" + QUERY_PREFIX + "=a==b", "{\"set\":{\"a\":\"c\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedDataAfterPut);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedAllDataAfterPut);
    }

    @Test
    public void testDeleteProperlyDeletesData() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, serviceUrl + "?" + QUERY_PREFIX + "=fakedata==test", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, HttpTestUtil.EMPTY_RESPONSE);

        String expectedAfterDeletion = "{\"response\":[{\"c\":\"d\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"fakedata\":\"test\",\"a\":\"b\"},{\"c\":\"d\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, serviceUrl + "?" + QUERY_PREFIX + "=a==b", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedAfterDeletion);
    }

    @Test
    public void testMalformedDeleteRequestDoesNotMutateData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"fakedata\":\"test\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedDataResponse);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, serviceUrl + "?" + QUERY_PREFIX + "=nosuchkey==", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedDataResponse);
    }

    @Test
    public void testPutDataWithoutUrlQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl, "{\"set\":{\"fakedata\":\"test\"}}", 200);
    }

    @Test
    public void testPostAndPutWithInvalidData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"fakedata\":\"test\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String urlQuery = serviceUrl + "?" + QUERY_PREFIX + "=nosuchkey==nosuchvalue";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, urlQuery, "{\"set\":{\"fakedata\":\"somethingnew\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedDataResponse);
    }

    @Test
    public void testPutWithIdenticalData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"fakedata\":\"test\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl, "{\"set\":{\"fakedata\":\"test\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedDataResponse);
    }

    @Test
    public void testPutDifferentData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"a\":\"b\"" + SYSTEM_JVM_FRAGMENT + ",\"c\":\"d\"}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl + "?" + QUERY_PREFIX + "=a==b", "{\"set\":{\"c\":\"d\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedDataResponse);
    }

    @Test
    public void testChangeDataWithPutMultipleTimes() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedData = "{\"response\":[{\"a\":\"a2\"" + SYSTEM_JVM_FRAGMENT + ",\"b\":\"b2\",\"c\":\"c2\"}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"a2\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl + "?" + QUERY_PREFIX + "=a==a2", "{\"set\":{\"b\":\"b2\"}}", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl + "?" + QUERY_PREFIX + "=a==a2", "{\"set\":{\"c\":\"c2\"}}", 200);

        // It won't find the target from the query, so the addition should not occur to any object.
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl + "?" + QUERY_PREFIX + "=a==none", "{\"set\":{\"d\":\"d2\"}}", 200);

        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5", 200, expectedData);
    }

    @Test
    public void testGetWithBadUrlQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=2", 400);
    }

    @Test
    public void testGetLimitWithQuery() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataOne = "{\"response\":[{\"a\":\"a2\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedDataAll = "{\"response\":[{\"a\":\"a2\"" + SYSTEM_JVM_FRAGMENT + "},{\"b\":\"b2\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"a2\"},{\"b\":\"b2\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedDataOne);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=2", 200, expectedDataAll);
    }

    @Test
    public void testQueryOffset() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedOffsetRestOfData = "{\"response\":[{\"b\":\"2\"" + SYSTEM_JVM_FRAGMENT + "},{\"c\":\"3\"" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"1\"},{\"b\":\"2\"},{\"c\":\"3\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + OFFSET_PREFIX + "=1", 200, "{\"response\":[{\"b\":\"2\"" + SYSTEM_JVM_FRAGMENT + "}]}");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + OFFSET_PREFIX + "=3", 200, HttpTestUtil.EMPTY_RESPONSE);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=5&" + OFFSET_PREFIX + "=1", 200, expectedOffsetRestOfData);
    }

    @Test
    public void testNegativeOffsetQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"1\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + OFFSET_PREFIX + "=-1", 400);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + OFFSET_PREFIX + "=-582", 400);
    }

    @Test
    public void testQueryOrdering() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedGet = "{\"response\":[{\"a\":1" + SYSTEM_JVM_FRAGMENT + "},{\"a\":2" + SYSTEM_JVM_FRAGMENT + "}]}";
        String expectedGetReverse = "{\"response\":[{\"a\":2" + SYSTEM_JVM_FRAGMENT + "},{\"a\":1" + SYSTEM_JVM_FRAGMENT + "}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":1},{\"a\":2}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=10&" + SORT_PREFIX + "=+a", 200, expectedGet);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + LIMIT_PREFIX + "=10&" + SORT_PREFIX + "=-a", 200, expectedGetReverse);
    }

    @Test
    public void testQueryProjection() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedGet = "{\"response\":[{\"b\":\"2\",\"c\":\"3\"}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"1\",\"b\":\"2\",\"c\":\"3\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + INCLUDE_PREFIX + "=b,c", 200, expectedGet);
    }

    @Test
    public void testGetWithMetaDataAndLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}"],
        // "metaData":{"payloadCount":1,"count":3,
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=1&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"a\":\"test\",\"b\":\"test1\"," +
                "\"c\":\"test2\"" + SYSTEM_JVM_FRAGMENT + "}],\"metaData\":{\"payloadCount\":1,\"count\":3," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d1\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", 200, expectedResponse);
    }

    @Test
    public void testGetWithVersions() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        String expectedResponse = "{\"response\":[{\"a\":\"test\",\"b\":\"test1\"," +
                "\"c\":\"test2\"" + SYSTEM_JVM_FRAGMENT + "}],\"metaData\":{\"payloadCount\":1,\"count\":3," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d1\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        final String url1 =  baseUrl + "/" + serviceName + "/" + "0.0" + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, url1 + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", 200, expectedResponse);
        final String url2 =  baseUrl + "/" + serviceName + "/" + "0.0.1" + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, url2 + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", 200, expectedResponse);
        final String url3 =  baseUrl + "/" + serviceName + "/" + "0.0.3" + "/systems/" + AGENT_ID + "/jvms/" + JVM_ID;
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, url3 + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", 404, null);
    }

    @Test
    public void testGetWithMetaDataAndOffset() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],"metaData":{"payloadCount":1,"count":3,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=1",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":3," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d2\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=1", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevOffsetLessThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":5,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=1&o=0",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=4&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"e\":\"test4\"," +
                "\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":5," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue" +
                "\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + OFFSET_PREFIX + "\\u003d0\",\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d4" +
                "\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=1&" + LIMIT_PREFIX + "=3", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=2&o=1",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d1\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=3&" + LIMIT_PREFIX + "=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":3,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=1",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":3," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d2\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=1&" + LIMIT_PREFIX + "=1", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextOffsetLessThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"e\":\"test4\",\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":2,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=1&o=0",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=3&l=2&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"e\":\"test4\"," +
                "\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}],\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + OFFSET_PREFIX + "\\u003d0\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d3\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=1&" + LIMIT_PREFIX + "=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=2&o=1",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d1\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=3&" +LIMIT_PREFIX + "=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";
        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":2,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=2&o=0",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d4\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=2&" + LIMIT_PREFIX + "=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":2,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q===test1&m=true&l=2&o=0",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d4\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=2&" + LIMIT_PREFIX + "=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}","{\"b\":\"test1\"}"],"
        // metaData":{"payloadCount":1,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-io/0.0.2?q=b==test1&m=true&l=2&o=0",
        // "next":"http://127.0.0.1:30000/jvm-io/0.0.2?o=5&l=1&q=b==test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}," +
                "{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "},{\"b\":\"test1\"" + SYSTEM_JVM_FRAGMENT + "}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"" + returnedUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\"," +
                "\"next\":\"" + returnedUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, serviceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl + "?" + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=2&" + LIMIT_PREFIX + "=3", 200, expectedResponse);
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

    class TinyJvmIo {
        String jvmId;
        long timeStamp;
        public TinyJvmIo(String jvmId, long ts) {
            this.jvmId = jvmId;
            this.timeStamp = ts;
        }
    }
    private List<TinyJvmIo> parse(ContentResponse contentResponse, final String expectedJvmId) {

        JsonParser parser = new JsonParser();
        JsonObject json = (JsonObject) parser.parse(contentResponse.getContentAsString());
        JsonElement response = json.get("response");

        JsonArray allData = response.getAsJsonArray();
        List<TinyJvmIo> result = new ArrayList<>();

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

            TinyJvmIo hi = new TinyJvmIo(jvmId, 0);

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
