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
    public void testGet() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testGetWithQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=10", 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testGetWithMalformedQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?xyz=5", 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testPostProperlyAddsData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedResponse = "{ \"response\" : [{ \"fakedata\" : \"test\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"fakedata\" : \"test\" }]", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedResponse);
    }

    @Test
    public void testMultiplePosts() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedResponse = "{ \"response\" : [{ \"fakedata\" : \"test\" },{ \"new\" : \"data\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"fakedata\" : \"test\" }]", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"new\" : \"data\" }]", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedResponse);
    }

    @Test
    public void testPostPutAddsData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataBeforePut = "{ \"response\" : [{ \"a\" : \"b\" }] }";
        String expectedDataAfterPut = "{ \"response\" : [{ \"a\" : \"b\", \"x\" : \"y\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"a\" : \"b\" }]", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataBeforePut);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==b", "{ \"set\" : { \"x\" : \"y\" }}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataAfterPut);
    }

    @Test
    public void testPostPutModifiesData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataBeforePut = "{ \"response\" : [{ \"a\" : \"b\" }] }";
        String expectedDataAfterPut = "{ \"response\" : [{ \"a\" : \"c\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"a\" : \"b\" }]", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataBeforePut);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==b", "{ \"set\" : { \"a\" : \"c\" }}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataAfterPut);
    }

    @Test
    public void testDeleteProperlyDeletesData() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"fakedata\" : \"test\" }]", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, resourceUrl + "?q=fakedata==test", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testMalformedDeleteRequestDoesNotMutateData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{ \"response\" : [{ \"fakedata\" : \"test\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"fakedata\" : \"test\" }]", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataResponse);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, resourceUrl + "?q=nosuchkey==", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataResponse);
    }

    @Test
    public void testPutDataWithoutUrlQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"fakedata\" : \"test\" }]", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl, "{ \"set\" : { \"fakedata\" : \"test\" }}", 400);
    }

    @Test
    public void testPostAndPutWithInvalidData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{ \"response\" : [{ \"fakedata\" : \"test\" }] }";
        String urlQuery = resourceUrl + "?q=nosuchkey==nosuchvalue";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, urlQuery, "[{ \"fakedata\" : \"test\" }]", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, urlQuery, "{ \"set\" : { \"fakedata\" : \"somethingnew\" }}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedDataResponse);
    }

    @Test
    public void testPutWithIdenticalData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{ \"response\" : [{ \"fakedata\" : \"test\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"fakedata\" : \"test\" }]", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl, "{ \"set\" : { \"fakedata\" : \"test\" }}", 400);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedDataResponse);
    }

    @Test
    public void testPutDifferentData() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedDataResponse = "{ \"response\" : [{ \"a\" : \"b\", \"c\" : \"d\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"a\" : \"b\" }]", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==b", "{ \"set\" : { \"c\" : \"d\" }}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedDataResponse);
    }

    @Test
    public void testChangeDataWithPutMultipleTimes() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedData = "{ \"response\" : [{ \"a\" : \"a2\", \"b\" : \"b2\", \"c\" : \"c2\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"a\" : \"a2\" }]", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==a2", "{ \"set\" : { \"b\" : \"b2\" }}", 200);
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, resourceUrl + "?q=a==a2", "{ \"set\" : { \"c\" : \"c2\" }}", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=5", 200, expectedData);
    }

    @Test
    public void testGetWithBadUrlQuery() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?q=2", 400);
    }

    @Test
    public void testGetLimitWithQuery() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{ \"a\" : \"a2\" },{ \"b\" : \"b2\" }]";
        String expectedDataOne = "{ \"response\" : [{ \"a\" : \"a2\" }] }";
        String expectedDataAll = "{ \"response\" : " + data + " }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, data, 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl, 200, expectedDataOne);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=2", 200, expectedDataAll);
    }

    @Test
    public void testQueryOffset() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, "[{ \"a\" : \"1\" } , {\"b\" : \"2\"}, {\"c\" : \"3\"}]", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?o=1", 200, "{ \"response\" : [{ \"b\" : \"2\" }] }");
    }

    @Test
    public void testQueryOrdering() throws InterruptedException, TimeoutException, ExecutionException {
        String content = "[{ \"a\" : 1 },{ \"a\" : 2 }]";
        String expectedGet = "{ \"response\" : " + content + " }";
        String expectedGetReverse = "{ \"response\" : [{ \"a\" : 2 },{ \"a\" : 1 }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, content, 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=10&s=+a", 200, expectedGet);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?l=10&s=-a", 200, expectedGetReverse);
    }

    @Test
    public void testQueryProjection() throws InterruptedException, TimeoutException, ExecutionException {
        String content = "[{ \"a\" : \"1\", \"b\" : \"2\", \"c\" : \"3\" }]";
        String expectedGet = "{ \"response\" : [{ \"b\" : \"2\", \"c\" : \"3\" }] }";
        HttpTestUtil.testContentResponse(client, HttpMethod.POST, resourceUrl, content, 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, resourceUrl + "?p=b,c", 200, expectedGet);
    }
}
