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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;

/**
 * Base class for integration tests with swagger API docs.
 *
 */
public abstract class ServiceIntegrationTest extends IntegrationTest {

    public ServiceIntegrationTest(String serviceUrl) {
        super(serviceUrl);
    }

    public abstract String getServiceVersion();

    public abstract String getServiceName();

    @Test(timeout = 5000)
    public void verifySwaggerYamlAccessible() throws ExecutionException, InterruptedException, TimeoutException {
        try {
            removeAuthentication(client); // be sure to perform get with no authentication
            String swaggerDocsUrl = getDocUrl();
            final Request request = client.newRequest(swaggerDocsUrl);
            ContentResponse response = request.method(HttpMethod.GET).send();
            assertEquals("Expected OK response (no auth should be required). URL was: " + swaggerDocsUrl, HttpStatus.OK_200, response.getStatus());
            String actual = response.getContentAsString();
            assertNotNull(actual);
            assertFalse(actual.isEmpty());
            assertTrue("service name expected to be part of swagger spec", actual.contains(getServiceName()));
            assertTrue("service version expected to be part of swagger spec", actual.contains(getServiceVersion()));
        } finally {
            addAuthentication(client);
        }
    }

    private String getDocUrl() {
        return baseUrl + "/" + getServiceName() + "/" + getServiceVersion() + "/doc/" + getServiceName() + "-swagger.yaml";
    }

}
