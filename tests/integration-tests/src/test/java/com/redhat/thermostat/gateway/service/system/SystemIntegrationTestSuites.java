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

package com.redhat.thermostat.gateway.service.system;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

import com.redhat.thermostat.gateway.tests.integration.SystemIntegrationTest;

public abstract class SystemIntegrationTestSuites<T> extends SystemIntegrationTest<T> {


    protected static final String TIMESTAMP_TOKEN = "$TIMESTAMP$";
    protected static final String AGENT_ID = getRandomSystemId();

    private String systemId;
    private String serviceURL;

    protected SystemIntegrationTestSuites(String serviceUrl, String collectionName) {
        super(serviceUrl, collectionName);
        this.serviceURL = serviceUrl;
        this.collectionName = collectionName;
    }

    protected abstract String createJSONTimeStamp(final long ts);

    protected abstract List<T> parse(ContentResponse contentResponse, final String expectedsystemId);

    @Before
    public void setupSystemId() {
        this.systemId = getRandomSystemId();
        this.timeStamp = getTimeStamp();
    }

    @Test
    public void testGetAll() throws InterruptedException, TimeoutException, ExecutionException {

        for (int i = 0; i < 3; i++) {
            post(systemId);
        }

        ContentResponse response = get(systemId);
        final List<T> list = parse(response, systemId);
        assertEquals(1, list.size());

        ContentResponse response2 = get(systemId, "?limit=2");
        final List<T> list2 = parse(response2, systemId);
        assertEquals(2, list2.size());

        ContentResponse response3 = get(systemId, "?limit=0");
        final List<T> list3 = parse(response3, systemId);
        assertEquals(3, list3.size());
    }

    @Test
    public void testGetAllFails() throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = client.newRequest(serviceURL).method(HttpMethod.GET).send();
        assertEquals(HTTP_404_NOTFOUND, response.getStatus());
    }

    @Test
    public void testGetUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemId = getRandomSystemId();
        getUnknown(systemId);
    }

    @Test
    public void testCreateOne() throws InterruptedException, TimeoutException, ExecutionException {
        final String systemId = getRandomSystemId();
        post(systemId);
        getKnown(systemId);
    }

    @Test
    public void testPutModifiesData() throws InterruptedException, TimeoutException, ExecutionException {
        final long timestamp = getTimeStamp();

        post(systemId);
        final ContentResponse response1 = getKnown(systemId);
        final List<T> list1 = parse(response1, systemId);
        assertEquals(1, list1.size());

        put(systemId, timestamp+1);

        final ContentResponse response2 = getKnown(systemId);
        final List<T> list2 = parse(response2, systemId);
        assertEquals(1, list2.size());
    }

    @Test
    public void testDeleteUnknown() throws InterruptedException, TimeoutException, ExecutionException {
        delete(systemId);
    }

    @Test
    public void testDeleteOne() throws InterruptedException, ExecutionException, TimeoutException {
        post(systemId);
        getKnown(systemId);
        delete(systemId);
        getUnknown(systemId);
    }
}
