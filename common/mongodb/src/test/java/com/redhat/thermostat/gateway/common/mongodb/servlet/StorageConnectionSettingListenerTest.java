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

package com.redhat.thermostat.gateway.common.mongodb.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;
import com.redhat.thermostat.gateway.common.mongodb.configuration.MongoConfiguration;

public class StorageConnectionSettingListenerTest {

    private ServletContext ctxt;
    private ServletContextEvent evt;

    @Before
    public void setup() {
        evt = mock(ServletContextEvent.class);
        ctxt = mock(ServletContext.class);
        when(evt.getServletContext()).thenReturn(ctxt);
        when(ctxt.getInitParameter(eq(GlobalConstants.GATEWAY_HOME_KEY))).thenReturn(getTestGatewayRoot());
        when(ctxt.getInitParameter(eq(GlobalConstants.SERVICE_NAME_KEY))).thenReturn("foo-service");
    }

    @Test
    public void canGetConfigFromServletContext() {
        StorageConnectionSettingListener listener = new StorageConnectionSettingListener();
        Map<String, String> actual = listener.getMongoStorageConfig(ctxt);
        assertEquals("foo", actual.get(MongoConfiguration.MONGO_DB.name()));
        assertEquals("foo-user", actual.get(MongoConfiguration.MONGO_USERNAME.name()));
        assertEquals("foo-password", actual.get(MongoConfiguration.MONGO_PASSWORD.name()));
        assertEquals("3", actual.get(MongoConfiguration.MONGO_SERVER_TIMEOUT.name()));
        assertEquals("mongodb://localhost:21793", actual.get(MongoConfiguration.MONGO_URL.name()));
    }

    @Test
    public void contextInitializedAddsStorageAsAttribute() {
        StorageConnectionSettingListener listener = new StorageConnectionSettingListener();
        listener.contextInitialized(evt);
        verify(ctxt).setAttribute(eq(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE), isA(ThermostatMongoStorage.class));
    }

    private String getTestGatewayRoot() {
        URL rootUrl = StorageConnectionSettingListener.class.getResource("/gateway-root");
        return decodeFilePath(rootUrl);
    }

    private String decodeFilePath(URL url) {
        try {
            // Spaces are encoded as %20 in URLs. Use URLDecoder.decode() so
            // as to handle cases like that.
            return URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 not supported, huh?");
        }
    }
}
