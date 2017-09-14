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

package com.redhat.thermostat.gateway.tests.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.thermostat.gateway.tests.utils.keycloak.KeycloakAccessToken;
import com.redhat.thermostat.gateway.tests.utils.keycloak.KeycloakUserCredentials;

public class HttpRequestUtil {
    /**
     * GET request type.
     */
    public static final String GET = HttpMethod.GET.asString();
    /**
     * PUT request type.
     */
    public static final String PUT = HttpMethod.PUT.asString();
    /**
     * POST request type.
     */
    public static final String POST = HttpMethod.POST.asString();
    /**
     * DELETE request type.
     */
    public static final String DELETE = HttpMethod.DELETE.asString();

    private static final String KEYCLOAK_TOKEN_SERVICE = "/auth/realms/__REALM__/protocol/openid-connect/token";
    private static final String KEYCLOAK_CONTENT_TYPE = "application/x-www-form-urlencoded";

    // Static to reuse credentials between tests as much as possible
    private static final Map<KeycloakUserCredentials, KeycloakAccessToken> tokenMap = new HashMap<>();

    private HttpClient client;
    private Gson gson = new GsonBuilder().create();

    public HttpRequestUtil(HttpClient client) {
        this.client = client;
    }

    public Request buildRequest(String url, HttpMethod method) {
        return client.newRequest(url).method(method);
    }

    public ContentResponse sendKeycloakHttpRequest(Request request, KeycloakUserCredentials credentials) throws InterruptedException, ExecutionException, TimeoutException, IOException {
        request.header("Authorization", "Bearer " + getAccessToken(credentials));
        return request.send();
    }

    private String getAccessToken(KeycloakUserCredentials credentials) throws IOException {
        if (!tokenMap.containsKey(credentials)) {
            KeycloakAccessToken keycloakAccessToken = acquireKeycloakToken(credentials);
            if (keycloakAccessToken != null) {
                tokenMap.put(credentials, keycloakAccessToken);
                return keycloakAccessToken.getAccessToken();
            } else {
                throw new IOException("Unable to acquire Keycloak token. Is Keycloak server running?");
            }
        }

        KeycloakAccessToken keycloakAccessToken = tokenMap.get(credentials);
        if (isKeycloakTokenExpired(keycloakAccessToken)) {
            keycloakAccessToken = refreshKeycloakToken(credentials, keycloakAccessToken);

            if (keycloakAccessToken == null) {
                keycloakAccessToken = acquireKeycloakToken(credentials);
            }

            tokenMap.put(credentials, keycloakAccessToken);

            if (keycloakAccessToken == null) {
                throw new IOException("Keycloak token expired and attempt to refresh and reacquire Keycloak token failed.");
            }

        }
        return keycloakAccessToken.getAccessToken();
    }

    private boolean isKeycloakTokenExpired(KeycloakAccessToken keycloakAccessToken) {
        return System.nanoTime() > TimeUnit.NANOSECONDS.convert(keycloakAccessToken.getExpiresIn(), TimeUnit.SECONDS) + keycloakAccessToken.getAcquireTime();
    }

    private KeycloakAccessToken acquireKeycloakToken(KeycloakUserCredentials credentials) {
        return requestKeycloakToken(getKeycloakAccessPayload(credentials), credentials);
    }

    private KeycloakAccessToken refreshKeycloakToken(KeycloakUserCredentials credentials, KeycloakAccessToken keycloakAccessToken) {
        return requestKeycloakToken(getKeycloakRefreshPayload(credentials, keycloakAccessToken), credentials);
    }

    private KeycloakAccessToken requestKeycloakToken(String payload, KeycloakUserCredentials credentials) {
        String keycloakUrl = credentials.getUrl();
        String keycloakRealm = credentials.getRealm();
        String url = keycloakUrl + KEYCLOAK_TOKEN_SERVICE.replace("__REALM__", keycloakRealm);
        Request request = client.newRequest(url);
        request.content(new StringContentProvider(payload), KEYCLOAK_CONTENT_TYPE);
        request.method(HttpMethod.POST);

        try {
            ContentResponse response = request.send();
            if (response.getStatus() == HttpStatus.OK_200) {

                String content = response.getContentAsString();

                KeycloakAccessToken keycloakAccessToken = gson.fromJson(content, KeycloakAccessToken.class);
                keycloakAccessToken.setAcquireTime(System.nanoTime());

                return keycloakAccessToken;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String getKeycloakAccessPayload(KeycloakUserCredentials credentials) {
        return "grant_type=password&client_id=" + credentials.getClient() +
                "&username=" + credentials.getUsername() +
                "&password=" + credentials.getPassword();
    }

    private String getKeycloakRefreshPayload(KeycloakUserCredentials credentials, KeycloakAccessToken keycloakAccessToken) {
        return "grant_type=refresh_token&client_id=" + credentials.getClient() +
                "&refresh_token=" + keycloakAccessToken.getRefreshToken();
    }
}
