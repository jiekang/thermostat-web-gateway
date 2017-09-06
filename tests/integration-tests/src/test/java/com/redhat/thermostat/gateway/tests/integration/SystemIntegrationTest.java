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

package com.redhat.thermostat.gateway.tests.integration;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class SystemIntegrationTest<T> extends MongoIntegrationTest {

    protected static final int HTTP_200_OK = 200;
    protected static final int HTTP_404_NOTFOUND = 404;
    protected static final String TIMESTAMP_TOKEN = "$TIMESTAMP$";
    protected static final String AGENT_ID = getRandomSystemId();

    protected long timeStamp = java.lang.System.nanoTime();
    protected String systemId;
    private String serviceURL;

    protected SystemIntegrationTest(String serviceUrl, String collectionName) {
        super(serviceUrl, collectionName);
        this.serviceURL = serviceUrl;
        this.collectionName = collectionName;
    }

    protected abstract String createJSONTimeStamp(final long ts);

    protected abstract List<T> parse(ContentResponse contentResponse, final String expectedsystemId);

    protected long getTimeStamp() {
        timeStamp += 1;
        return timeStamp;
    }

    protected static String getRandomSystemId() {
        return UUID.randomUUID().toString();
    }

    private String createJSONTimeStamp() {
        return createJSONTimeStamp(getTimeStamp());
    }

    protected ContentResponse post(final String systemId) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(serviceURL + "/systems/" + systemId);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        request.content(new StringContentProvider("[" + createJSONTimeStamp() + "]"));
        ContentResponse response = request.method(HttpMethod.POST).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    protected ContentResponse getSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(resourceUrl + "/systems/" + systemid).method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    protected ContentResponse deleteSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(resourceUrl + "/systems/" + systemid).method(HttpMethod.DELETE).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    protected ContentResponse get(final String systemId) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(serviceURL + "/systems/" + systemId).method(HttpMethod.GET).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    protected ContentResponse get(final String systemId, final String query) throws InterruptedException, ExecutionException, TimeoutException {
        final Request rq = client.newRequest(serviceURL + "/systems/" + systemId + query);
        rq.method(HttpMethod.GET);
        ContentResponse response = rq.send();
        assertEquals(HTTP_200_OK, response.getStatus());
        return response;
    }

    protected ContentResponse put(final String systemId, final long ts) throws InterruptedException, ExecutionException, TimeoutException {
        final Request request = client.newRequest(serviceURL + "/systems/" + systemId);
        request.header(HttpHeader.CONTENT_TYPE, "application/json");
        final String contentStr = createJSONTimeStamp(ts);
        request.content(new StringContentProvider("{ \"set\" : " +contentStr + "}"));
        ContentResponse response = request.method(HttpMethod.PUT).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    protected ContentResponse delete(final String systemId) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = client.newRequest(serviceURL + "/systems/" + systemId).method(HttpMethod.DELETE).send();
        assertEquals(HTTP_200_OK, response.getStatus());
        final String expected = "";
        assertEquals(expected, response.getContentAsString());
        return response;
    }

    protected ContentResponse getUnknown(final String systemId) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = get(systemId);
        assertTrue(parse(response, systemId).isEmpty());
        return response;
    }

    protected ContentResponse getKnown(final String systemId) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = get(systemId);
        assertEquals(1, parse(response, systemId).size());
        return response;
    }


    protected ContentResponse getUnknownSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = getSystemInfo(systemid);
        assertTrue(parse(response, systemid).isEmpty());
        return response;
    }

    protected ContentResponse getKnownSystemInfo(final String systemid) throws InterruptedException, ExecutionException, TimeoutException {
        ContentResponse response = getSystemInfo(systemid);
        assertEquals(1, parse(response, systemid).size());
        return response;
    }
}
