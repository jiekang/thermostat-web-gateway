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

package com.redhat.thermostat.gateway.tests.keycloak;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.gateway.tests.utils.EndpointDefinition;
import com.redhat.thermostat.gateway.tests.utils.keycloak.DefaultKeycloakUsers;
import com.redhat.thermostat.gateway.tests.utils.keycloak.KeycloakUserCredentials;

public abstract class BasicKeycloakIntegrationTestSuite extends MongoKeycloakIntegrationTest {

    private static final String jsonFormat = "application/json";

    private final List<EndpointDefinition> getList = new ArrayList<>();
    private final List<EndpointDefinition> postList = new ArrayList<>();
    private final List<EndpointDefinition> putList = new ArrayList<>();
    private final List<EndpointDefinition> deleteList = new ArrayList<>();

    public BasicKeycloakIntegrationTestSuite(String serviceName, String versionNumber, String collectionName) {
        super(serviceName + "/" + versionNumber, collectionName);
        buildMethodLists(getEndpointList());
    }

    protected void buildMethodLists(List<EndpointDefinition> endpointList) {
        for (EndpointDefinition endpoint : endpointList) {
            switch (endpoint.getMethod()) {
                case GET:
                    getList.add(endpoint);
                    break;
                case POST:
                    postList.add(endpoint);
                    break;
                case PUT:
                    putList.add(endpoint);
                    break;
                case DELETE:
                    deleteList.add(endpoint);
                    break;
                default:
                    break;
            }
        }
    }

    protected abstract List<EndpointDefinition> getEndpointList();

    @Test
    public void testGetUnauthenticated() throws InterruptedException, ExecutionException, TimeoutException {
        verifyEndpointsWhenUnauthenticated(getList);
    }

    @Test
    public void testPostUnauthenticated() throws InterruptedException, ExecutionException, TimeoutException {
        verifyEndpointsWhenUnauthenticated(postList);
    }

    @Test
    public void testPutUnauthenticated() throws InterruptedException, ExecutionException, TimeoutException {
        verifyEndpointsWhenUnauthenticated(putList);
    }

    @Test
    public void testDeleteUnauthenticated() throws InterruptedException, ExecutionException, TimeoutException {
        verifyEndpointsWhenUnauthenticated(deleteList);
    }

    @Test
    public void testGetAuthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenAuthorized(getList, DefaultKeycloakUsers.USER_A.getCredentials());
    }

    @Test
    public void testGetUnauthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenUnauthorized(getList, DefaultKeycloakUsers.USER_B.getCredentials());
    }

    @Test
    public void testPostAuthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenAuthorized(postList, DefaultKeycloakUsers.USER_B.getCredentials());
    }

    @Test
    public void testPostUnauthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenUnauthorized(postList, DefaultKeycloakUsers.USER_C.getCredentials());
    }

    @Test
    public void testPutAuthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenAuthorized(putList, DefaultKeycloakUsers.USER_C.getCredentials());
    }

    @Test
    public void testPutUnauthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenUnauthorized(putList, DefaultKeycloakUsers.USER_D.getCredentials());
    }

    @Test
    public void testDeleteAuthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenAuthorized(deleteList, DefaultKeycloakUsers.USER_D.getCredentials());
    }

    @Test
    public void testDeleteUnauthorized() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpointsWhenUnauthorized(deleteList, DefaultKeycloakUsers.USER_A.getCredentials());
    }

    private void verifyEndpointsWhenUnauthenticated(List<EndpointDefinition> endpoints) throws InterruptedException, ExecutionException, TimeoutException {
        for (EndpointDefinition endpoint : endpoints) {
            ContentResponse response = httpRequestUtil.buildRequest(baseResourceUrl + endpoint.getEndpointUrl(), endpoint.getMethod()).send();

            verifyResponse(endpoint, response, 401);
            assertEquals(401, response.getStatus());
        }
    }

    private void verifyEndpointsWhenAuthorized(List<EndpointDefinition> endpoints, KeycloakUserCredentials keycloakUserCredentials) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpoints(endpoints, keycloakUserCredentials, 200);
    }

    private void verifyEndpointsWhenUnauthorized(List<EndpointDefinition> endpoints, KeycloakUserCredentials keycloakUserCredentials) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        verifyEndpoints(endpoints, keycloakUserCredentials, 403);
    }

    private void verifyEndpoints(List<EndpointDefinition> endpoints, KeycloakUserCredentials keycloakUserCredentials, int expectedStatus) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        for (EndpointDefinition endpoint : endpoints) {
            HttpMethod method = endpoint.getMethod();
            Request request = httpRequestUtil.buildRequest(baseResourceUrl + endpoint.getEndpointUrl(), method);
            if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
                request = request.content(new StringContentProvider(endpoint.getBody()), jsonFormat);
            }

            ContentResponse response = httpRequestUtil.sendKeycloakHttpRequest(request, keycloakUserCredentials);

            verifyResponse(endpoint, response, expectedStatus);
        }
    }

    private void verifyResponse(EndpointDefinition endpoint, ContentResponse response, int expectedStatus) {
        if (expectedStatus != response.getStatus()) {
            System.out.println("Endpoint: " + endpoint.getMethod() + " : " + endpoint.getEndpointUrl());
            System.out.println("Expected Status: " + expectedStatus);
            System.out.println("Actual Status: " + response.getStatus());
        }
        assertEquals(endpoint.getEndpointUrl(), expectedStatus, response.getStatus());
    }
}
