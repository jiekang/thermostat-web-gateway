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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.servlet.GatewayHomeSettingContextListener.EnvHelper;

public class GatewayHomeSettingContextListenerTest {

    private ServletContext ctxt;
    private ServletContextEvent evt;
    private EnvHelper mockEnv;

    @Before
    public void setup() {
        evt = mock(ServletContextEvent.class);
        ctxt = mock(ServletContext.class);
        when(evt.getServletContext()).thenReturn(ctxt);
        mockEnv = mock(EnvHelper.class);
    }

    @Test
    public void initializedSetsGatewayHomeAsSCAfromEnv() {
        String gatewayHome = "bar-gw-home-from-env";
        when(mockEnv.getEnv(eq(GlobalConstants.GATEWAY_HOME_ENV))).thenReturn(gatewayHome);
        GatewayHomeSettingContextListener listener = new GatewayHomeSettingContextListener(mockEnv);
        listener.contextInitialized(evt);
        verify(ctxt).setAttribute(eq(GlobalConstants.GATEWAY_HOME_KEY), eq(gatewayHome));
    }

    @Test
    public void initializedSetsGatewayHomeAsSCAfromWebXml() {
        String gatewayHome = "foo-gw-home";
        when(ctxt.getInitParameter(eq(GlobalConstants.GATEWAY_HOME_KEY))).thenReturn(gatewayHome);
        GatewayHomeSettingContextListener listener = new GatewayHomeSettingContextListener(mockEnv);
        listener.contextInitialized(evt);
        verify(ctxt).setAttribute(eq(GlobalConstants.GATEWAY_HOME_KEY), eq(gatewayHome));
    }

    @Test
    public void envOverridesWebXml() {
        String webXmlGwHome = "foo-gw-home";
        String envGwHome = "bar-gw-home-from-env";
        when(ctxt.getInitParameter(eq(GlobalConstants.GATEWAY_HOME_KEY))).thenReturn(webXmlGwHome);
        when(mockEnv.getEnv(eq(GlobalConstants.GATEWAY_HOME_ENV))).thenReturn(envGwHome);
        GatewayHomeSettingContextListener listener = new GatewayHomeSettingContextListener(mockEnv);
        listener.contextInitialized(evt);
        verify(ctxt).setAttribute(eq(GlobalConstants.GATEWAY_HOME_KEY), eq(envGwHome));
    }

    @Test
    public void destroyedRemovesAttribute() {
        GatewayHomeSettingContextListener listener = new GatewayHomeSettingContextListener(mockEnv);
        listener.contextDestroyed(evt);
        verify(ctxt).setAttribute(eq(GlobalConstants.GATEWAY_HOME_KEY), eq(null));
    }
}
