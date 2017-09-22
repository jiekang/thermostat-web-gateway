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

package com.redhat.thermostat.gateway.service.jvm.byteman;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

import com.redhat.thermostat.gateway.tests.utils.HttpTestUtil;

/**
 * Integration tests for {@code jvm-byteman/<version>/metrics/*} URLs
 *
 */
public class JvmBytemanMetricsServiceIntegrationTest extends JvmBytemanServiceIntegrationTest {

    private static final String COLLECTION_NAME = "jvm-byteman-metrics";
    private static final String SYSTEM_ID = UUID.randomUUID().toString();
    private static final String JVM_ID = UUID.randomUUID().toString();
    private static final String STATUS_BASE_URL = baseUrl + "/" + SERVICE_NAME + "/" + VERSION_NUMBER + "/" + "metrics";
    private static final String SERVICE_URL_SYSTEM_JVM = STATUS_BASE_URL + "/systems/" + SYSTEM_ID + "/jvms/" + JVM_ID;
    private static final String SERVICE_URL_JVM = STATUS_BASE_URL + "/jvms/" + JVM_ID;

    public JvmBytemanMetricsServiceIntegrationTest() {
        super("ignored", COLLECTION_NAME);
    }

    @Test
    public void testGetForJvms() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, SERVICE_URL_JVM, HttpStatus.OK_200, HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testPost() throws InterruptedException, TimeoutException, ExecutionException {
        String expected = "{\"response\":[{\"a\":\"b\",\"systemId\":\"" + SYSTEM_ID + "\",\"jvmId\":\"" + JVM_ID + "\"}]}";
        HttpTestUtil.addRecords(client, SERVICE_URL_SYSTEM_JVM, "[{\"a\":\"b\"}]");
        HttpTestUtil.testContentlessResponse(client, HttpMethod.GET, SERVICE_URL_JVM, HttpStatus.OK_200, expected);
    }

    /**
     * Verifies that PUT is not an allowed HTTP method.
     *
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    @Test
    public void testPut() throws InterruptedException, TimeoutException, ExecutionException {
        String putPayload = "any String, ignore me";
        HttpTestUtil.testContentResponse(client, HttpMethod.PUT, SERVICE_URL_SYSTEM_JVM, putPayload, HttpStatus.METHOD_NOT_ALLOWED_405);
    }

    /**
     * Verifies DELETE is not an allowed HTTP method.
     *
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    @Test
    public void testDelete() throws InterruptedException, TimeoutException, ExecutionException {
        HttpTestUtil.testContentlessResponse(client, HttpMethod.DELETE, SERVICE_URL_SYSTEM_JVM, HttpStatus.METHOD_NOT_ALLOWED_405);
    }
}
