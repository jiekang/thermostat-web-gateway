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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

public class JvmGcServiceIntegrationTest extends MongoIntegrationTest {

    private static final String serviceName = "jvm-gc";
    private static final String versionNumber = "0.0.2";
    private static final String gcUrl = baseUrl + "/" + serviceName + "/" + versionNumber;
    private final String data = "[{ \"a\" : \"test\", \"b\" : \"test1\", \"c\" : \"test2\" }, { \"d\" : \"test3\"}," +
            "{\"e\" : \"test4\" }]";

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
        ContentResponse postResponse = client.newRequest(gcUrl + urlQuery).method(httpMethod)
                .content(stringContentProvider, dataType)
                .send();
        assertEquals(expectedStatus, postResponse.getStatus());

        if (!expectedResponse.equals("")) {
            makeHttpGetRequest(gcUrl, expectedResponse, expectedStatus);
        }
    }

    @Test
    public void testGet() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", data, "application/json", "", 200);
        makeHttpGetRequest(gcUrl, "{ \"response\" : [{ \"a\" : \"test\", \"b\" : \"test1\", \"c\" : \"test2\" }] }", 200);
    }

    @Test
    public void testGetLimitParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", data, "application/json", "", 200);
        makeHttpGetRequest(gcUrl + "?l=2", "{ \"response\" : [{ \"a\" : \"test\", \"b\" : \"test1\", " +
                        "\"c\" : \"test2\" },{ \"d\" : \"test3\" }] }", 200);
    }

    @Test
    public void testGetSortParam() throws InterruptedException, TimeoutException, ExecutionException {
        String data = "[{ \"a\" : \"1\"}, {\"a\" : \"2\"}]";

        makeHttpMethodRequest(HttpMethod.POST, "", data,"application/json", "", 200);
        makeHttpGetRequest(gcUrl + "?l=3&s=-a", "{ \"response\" : [{ \"a\" : \"2\" },{ \"a\" : \"1\" }] }", 200);
    }

    @Test
    public void testGetProjectParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", data, "application/json", "", 200);
        makeHttpGetRequest(gcUrl + "?p=b,c", "{ \"response\" : [{ \"b\" : \"test1\", \"c\" : \"test2\" }] }", 200);
    }

    @Test
    public void testGetOffsetParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", data, "application/json", "", 200);
        makeHttpGetRequest(gcUrl + "?o=1", "{ \"response\" : [{ \"d\" : \"test3\" }] }", 200);
    }

    @Test
    public void testGetQueryParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", data,"application/json", "", 200);
        makeHttpGetRequest(gcUrl + "?q=b==test1", "{ \"response\" : [{ \"a\" : \"test\", \"b\" : \"test1\"," +
                " \"c\" : \"test2\" }] }", 200);
    }

    @Test
    public void testPostJSON() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", "[{ \"f1\" : \"test\" }]", "application/json",
                "{ \"response\" : [{ \"f1\" : \"test\" }] }", 200);
    }

    @Test
    public void testPostXML() throws InterruptedException,TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", "", "application/xml", "", 415);
    }

    @Test
    public void testInvalidDataPost() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", "{ \"badFormat\" : \"missing square brackets\" }",
                "application/json", "", 400);
    }

    @Test
    public void testDelete() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.DELETE, "", "", "", "", 200);
    }

    @Test
    public void testNonExistentDataDelete() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.DELETE, "?q=nonExist==Null", "", "", "", 200);
    }

    @Test
    public void testPostPutDeleteMockedData() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", "[{ \"f1\" : \"test\" }]", "application/json",
                "{ \"response\" : [{ \"f1\" : \"test\" }] }", 200);

        makeHttpMethodRequest(HttpMethod.PUT, "?q=f1==test", "{ \"set\" : { \"f1\" : \"newdata\" }}",
                "application/json", "{ \"response\" : [{ \"f1\" : \"newdata\" }] }", 200);

        makeHttpMethodRequest(HttpMethod.DELETE, "?q=f1==test", "", "", "", 200);
    }

    @Test
    public void testPutIdenticalPreexistingData() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, "", "[{ \"f2\" : \"identical\" }]",
                "application/json", "{ \"response\" : [{ \"f2\" : \"identical\" }] }", 200);

        makeHttpMethodRequest(HttpMethod.PUT,"?q=f2==identical","{ \"set\" : { \"f2\" : \"identical\" }}",
                "application/json","{ \"response\" : [{ \"f2\" : \"identical\" }] }", 200);
    }
}
