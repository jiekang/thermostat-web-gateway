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

package com.redhat.thermostat.gateway.service.jvm.memory;

import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import com.redhat.thermostat.gateway.tests.utils.HttpTestUtil;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class JvmMemoryServiceIntegrationTest extends MongoIntegrationTest {

    private static final String serviceName = "jvm-memory";
    private static final String versionNumber = "0.0.2";

    public JvmMemoryServiceIntegrationTest() {
        super(serviceName + "/" + versionNumber, serviceName);
    }

    @Test
    public void testGetWithUnsupportedQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?xyz=5", 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testDefaultLimitOne() throws InterruptedException, TimeoutException, ExecutionException {
        String expected = "{\"response\":[{\"a\":\"b\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"b\"},{\"a\":\"d\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?s=+a&l=", 200, expected);
    }

    @Test
    public void testGetWithCommaQuery() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedFirst = "{\"response\":[{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"}]}";
        String expectedAll = "{\"response\":[{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"},{\"x\":\"y\"},{\"z\":\"z\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"b\",\"c\":\"d\",\"e\":\"f\"},{\"x\":\"y\"},{\"z\":\"z\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedAll);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=a==b", 200, expectedFirst);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=c==d", 200, expectedFirst);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=a==b,c==d", 200, expectedFirst);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=a==b,c==none", 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testGetWithAmpersandQuery() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedAll = "{\"response\":[{\"a\":\"b\"},{\"c\":\"d\"}]}";
        String expectedAmpersand = "{\"response\":[{\"a\":\"b\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"b\"},{\"c\":\"d\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedAll);

        // Since q=a==b&c==d means "q= 'a==b'" and "c= '=d'", we should only
        // get 'a: b' back.
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=a==b&c==d", 200, expectedAmpersand);

        // The following should find the one with multiple matches, not the
        // single match. This means we will evaluate u==v only, and should get
        // back the first one it finds since no limit query is specified.
        String expectedSingleMatch = "{\"response\":[{\"u\":\"v\",\"x\":\"y\"}]}";
        String expectedMultiMatch = "{\"response\":[{\"u\":\"v\",\"x\":\"y\"},{\"u\":\"v\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"x\":\"y\"},{\"u\":\"v\",\"x\":\"y\"},{\"u\":\"v\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=u==v&q=x==y", 200, expectedSingleMatch);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=u==v&q=x==y&l=5", 200, expectedMultiMatch);
    }

    @Test
    public void testMultiplePosts() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedResponse = "{\"response\":[{\"fakedata\":\"test\"},{\"new\":\"data\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"new\":\"data\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedResponse);
    }

    @Test
    public void testPostPutAddsData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataBeforePut = "{\"response\":[{\"a\":\"b\"}]}";
        String expectedDataAfterPut = "{\"response\":[{\"a\":\"b\",\"x\":\"y\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"b\"},{\"a\":\"c\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataBeforePut);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==b", "{\"set\":{\"x\":\"y\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataAfterPut);
    }

    @Test
    public void testPostPutModifiesData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataBeforePut = "{\"response\":[{\"a\":\"b\"}]}";
        String expectedDataAfterPut = "{\"response\":[{\"a\":\"c\"}]}";
        String expectedAllDataAfterPut = "{\"response\":[{\"a\":\"c\"},{\"x\":\"y\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"b\"},{\"x\":\"y\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataBeforePut);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==b", "{\"set\":{\"a\":\"c\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataAfterPut);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedAllDataAfterPut);
    }

    @Test
    public void testDeleteProperlyDeletesData() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, resourceUrl + "?q=fakedata==test", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, HttpTestUtil.EMPTY_RESPONSE);

        String expectedAfterDeletion = "{\"response\":[{\"c\":\"d\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"fakedata\":\"test\",\"a\":\"b\"},{\"c\":\"d\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, resourceUrl + "?q=a==b", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedAfterDeletion);
    }

    @Test
    public void testMalformedDeleteRequestDoesNotMutateData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"fakedata\":\"test\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataResponse);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, resourceUrl + "?q=nosuchkey==", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataResponse);
    }

    @Test
    public void testPutDataWithoutUrlQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl, "{\"set\":{\"fakedata\":\"test\"}}", 400);
    }

    @Test
    public void testPostAndPutWithInvalidData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"fakedata\":\"test\"}]}";
        String urlQuery = resourceUrl + "?q=nosuchkey==nosuchvalue";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, urlQuery, "{\"set\":{\"fakedata\":\"somethingnew\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedDataResponse);
    }

    @Test
    public void testPutWithIdenticalData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"fakedata\":\"test\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"fakedata\":\"test\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl, "{\"set\":{\"fakedata\":\"test\"}}", 400);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedDataResponse);
    }

    @Test
    public void testPutDifferentData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{\"response\":[{\"a\":\"b\",\"c\":\"d\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"b\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==b", "{\"set\":{\"c\":\"d\"}}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedDataResponse);
    }

    @Test
    public void testChangeDataWithPutMultipleTimes() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedData = "{\"response\":[{\"a\":\"a2\",\"b\":\"b2\",\"c\":\"c2\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"a2\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==a2", "{\"set\":{\"b\":\"b2\"}}", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==a2", "{\"set\":{\"c\":\"c2\"}}", 200);

        // It won't find the target from the query, so the addition should not occur to any object.
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==none", "{\"set\":{\"d\":\"d2\"}}", 200);

        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedData);
    }

    @Test
    public void testGetWithBadUrlQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=2", 400);
    }

    @Test
    public void testGetLimitWithQuery() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataOne = "{\"response\":[{\"a\":\"a2\"}]}";
        String expectedDataAll = "{\"response\":[{\"a\":\"a2\"},{\"b\":\"b2\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"a2\"},{\"b\":\"b2\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataOne);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=2", 200, expectedDataAll);
    }

    @Test
    public void testQueryOffset() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedOffsetRestOfData = "{\"response\":[{\"b\":\"2\"},{\"c\":\"3\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"1\"},{\"b\":\"2\"},{\"c\":\"3\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?o=1", 200, "{\"response\":[{\"b\":\"2\"}]}");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?o=3", 200, HttpTestUtil.EMPTY_RESPONSE);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5&o=1", 200, expectedOffsetRestOfData);
    }

    @Test
    public void testNegativeOffsetQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"1\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?o=-1", 400);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?o=-582", 400);
    }

    @Test
    public void testQueryOrdering() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedGet = "{\"response\":[{\"a\":1},{\"a\":2}]}";
        String expectedGetReverse = "{\"response\":[{\"a\":2},{\"a\":1}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":1},{\"a\":2}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=10&s=+a", 200, expectedGet);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=10&s=-a", 200, expectedGetReverse);
    }

    @Test
    public void testQueryProjection() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedGet = "{\"response\":[{\"b\":\"2\",\"c\":\"3\"}]}";
        HttpTestUtil.addRecords(client, resourceUrl, "[{\"a\":\"1\",\"b\":\"2\",\"c\":\"3\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?p=b,c", 200, expectedGet);
    }

    @Test
    public void testGetWithMetaDataAndLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}"],
        // "metaData":{"payloadCount":1,"count":3,
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=1&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"a\":\"test\",\"b\":\"test1\"," +
                "\"c\":\"test2\"}],\"metaData\":{\"payloadCount\":1,\"count\":3," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d1\\u0026l\\u003d1\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&l=1", 200, expectedResponse);
    }

    @Test
    public void testGetWithMetaDataAndOffset() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],"metaData":{"payloadCount":1,"count":3,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=1",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":3," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d1\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d2\\u0026l\\u003d1\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=1", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevOffsetLessThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":5,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=1&o=0",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=4&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"},{\"e\":\"test4\"," +
                "\"b\":\"test1\"},{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":5," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue" +
                "\\u0026l\\u003d1\\u0026o\\u003d0\",\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d4" +
                "\\u0026l\\u003d1\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=1&l=3", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=2&o=1",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"},{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d2\\u0026o\\u003d1\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d5\\u0026l\\u003d1\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=3&l=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":3,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=1",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":3," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d1\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d2\\u0026l\\u003d1\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=1&l=1", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextOffsetLessThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"e\":\"test4\",\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":2,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=1&o=0",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=3&l=2&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"},{\"e\":\"test4\"," +
                "\"b\":\"test1\"}],\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d1\\u0026o\\u003d0\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d3\\u0026l\\u003d2\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=1&l=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":1,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=2&o=1",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"b\":\"test1\"},{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d2\\u0026o\\u003d1\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d5\\u0026l\\u003d1\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=3&l=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";
        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":2,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=2&o=0",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"},{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d2\\u0026o\\u003d0\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d4\\u0026l\\u003d2\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=2&l=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatNextExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        // "metaData":{"payloadCount":2,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q===test1&m=true&l=2&o=0",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"},{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":2,\"count\":6," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d2\\u0026o\\u003d0\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d4\\u0026l\\u003d2\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=2&l=2", 200, expectedResponse);
    }

    @Test
    public void testGetMetDatPrevExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"}," +
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"}, " +
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}","{\"b\":\"test1\"}"],"
        // metaData":{"payloadCount":1,"count":6,
        // "prev":"http://127.0.0.1:30000/jvm-memory/0.0.2?q=b==test1&m=true&l=2&o=0",
        // "next":"http://127.0.0.1:30000/jvm-memory/0.0.2?o=5&l=1&q=b==test1&m=true"}}
        String expectedResponse = "{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"}," +
                "{\"b\":\"test1\"},{\"b\":\"test1\"}]," +
                "\"metaData\":{\"payloadCount\":1,\"count\":6," +
                "\"prev\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\\u0026l\\u003d2\\u0026o\\u003d0\"," +
                "\"next\":\"http://127.0.0.1:30000/jvm-memory/0.0.2?o\\u003d5\\u0026l\\u003d1\\u0026q\\u003db\\u003d\\u003dtest1\\u0026m\\u003dtrue\"}}";

        HttpTestUtil.addRecords(client, resourceUrl, data);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=b==test1&m=true&o=2&l=3", 200, expectedResponse);
    }
}
