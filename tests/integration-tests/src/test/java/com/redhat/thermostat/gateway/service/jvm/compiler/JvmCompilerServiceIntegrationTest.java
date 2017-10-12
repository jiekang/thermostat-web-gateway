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

package com.redhat.thermostat.gateway.service.jvm.compiler;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import com.redhat.thermostat.gateway.tests.utils.HttpTestUtil;

public class JvmCompilerServiceIntegrationTest extends MongoIntegrationTest {

    private static final String SERVICE_NAME = "jvm-compiler";
    private static final String VERSION_NUMBER = "0.0.1";
    private static final String SYSTEM_ID = UUID.randomUUID().toString();
    private static final String JVM_ID = UUID.randomUUID().toString();
    private static final String simpleUrl = baseUrl + "/" + SERVICE_NAME + "/" + VERSION_NUMBER;
    private static final String serviceUrl = simpleUrl + "/systems/" + SYSTEM_ID + "/jvms/" + JVM_ID;
    private static final String jvmsServiceUrl = simpleUrl + "/jvms/" + JVM_ID;

    public JvmCompilerServiceIntegrationTest() {
        super(SERVICE_NAME + "/" + VERSION_NUMBER + "/systems/" + SYSTEM_ID + "/jvms/" + JVM_ID, SERVICE_NAME);
    }

    @Override
    public String getServiceVersion() {
        return VERSION_NUMBER;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Test
    public void testGetForJvms() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, jvmsServiceUrl, 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testGetForSystemJvms() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testPut() throws InterruptedException, TimeoutException, ExecutionException {
        String putContent = "{\"set\": {\"a\":\"c\"}}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"}]");
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, serviceUrl, putContent, 405);
    }

    @Test
    public void testDelete() throws InterruptedException, TimeoutException, ExecutionException {
        String expectedBefore = "{\"response\":[{\"a\":\"b\",\"systemId\":\"" + SYSTEM_ID + "\",\"jvmId\":\"" + JVM_ID + "\"}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expectedBefore);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, serviceUrl + "?q=a==b", 200);
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testPost() throws InterruptedException, TimeoutException, ExecutionException {
        String expected = "{\"response\":[{\"a\":\"b\",\"systemId\":\"" + SYSTEM_ID + "\",\"jvmId\":\"" + JVM_ID + "\"}]}";
        HttpTestUtil.addRecords(client, serviceUrl, "[{\"a\":\"b\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, serviceUrl, 200, expected);
    }

}
