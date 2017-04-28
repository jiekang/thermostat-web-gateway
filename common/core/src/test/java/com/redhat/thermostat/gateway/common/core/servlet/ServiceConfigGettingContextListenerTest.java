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

package com.redhat.thermostat.gateway.common.core.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ConfigurationTest;
import com.redhat.thermostat.gateway.common.core.servlet.GatewayHomeSettingContextListener.EnvHelper;

public class ServiceConfigGettingContextListenerTest extends ConfigurationTest {

    private ServletContext ctxt;
    private ServletContextEvent evt;
    private EnvHelper mockEnv;
    private String testRoot;

    @Before
    public void setup() {
        evt = mock(ServletContextEvent.class);
        ctxt = mock(ServletContext.class);
        when(evt.getServletContext()).thenReturn(ctxt);
        mockEnv = mock(EnvHelper.class);
        testRoot = getTestRoot();
        when(mockEnv.getEnv(eq(GlobalConstants.GATEWAY_HOME_ENV))).thenReturn(testRoot);
    }

    @Test
    public void canGetServiceConfigViaListener() {
        String serviceName = "test-service";
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("foo", "service-value");
        expected.put("test", "me");
        expected.put("bar", "baz");
        Map<String, Object> servicesConfig = new HashMap<String, Object>();
        servicesConfig.put("/service1", "/path/to/microservice.war");
        expected.put("SERVICES", servicesConfig);
        ServiceConfigGettingContextListener listener = new ServiceConfigGettingContextListener(mockEnv);
        listener.contextInitialized(evt);
        verify(ctxt).setAttribute(eq(GlobalConstants.GATEWAY_HOME_KEY), eq(testRoot));
        when(ctxt.getAttribute(eq(GlobalConstants.GATEWAY_HOME_KEY))).thenReturn(testRoot);
        Configuration config = listener.getServiceConfig(serviceName);
        Map<String, Object> actual = config.asMap();
        assertEquals(expected, actual);
    }
}
