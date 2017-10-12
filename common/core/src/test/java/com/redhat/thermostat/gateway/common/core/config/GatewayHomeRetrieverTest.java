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

package com.redhat.thermostat.gateway.common.core.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.config.GatewayHomeRetriever.EnvHelper;
import com.redhat.thermostat.gateway.common.core.config.GatewayHomeRetriever.PropertyHelper;
import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;

public class GatewayHomeRetrieverTest {

    private static final String TEST_GW_HOME_ENV = "path/to/test/gw/home";
    private static final String TEST_GW_HOME_PROP = "path/to/test/gw/home/prop";
    private GatewayHomeRetriever retriever;
    private PropertyHelper propHelper;


    @Before
    public void setup() {
        EnvHelper envHelper = mock(EnvHelper.class);
        when(envHelper.getEnv(eq(GlobalConstants.GATEWAY_HOME_ENV))).thenReturn(TEST_GW_HOME_ENV);
        propHelper = mock(PropertyHelper.class);
        retriever = new GatewayHomeRetriever(envHelper, propHelper);
    }

    @Test
    public void canGetGatewayHomeFromEnv() {
        when(propHelper.getProperty(eq(GlobalConstants.GATEWAY_HOME_ENV), eq(TEST_GW_HOME_ENV))).thenReturn(TEST_GW_HOME_ENV);
        String actual = retriever.getGatewayHome();
        assertEquals(TEST_GW_HOME_ENV, actual);
    }

    @Test
    public void canGetGatewayHomeFromProperty() {
        when(propHelper.getProperty(eq(GlobalConstants.GATEWAY_HOME_ENV), any(String.class))).thenReturn(TEST_GW_HOME_PROP);
        String actual = retriever.getGatewayHome();
        assertEquals(TEST_GW_HOME_PROP, actual);
    }

    @Test(expected = RuntimeException.class)
    public void noGwHomeThrowsException() {
        GatewayHomeRetriever noEnvAndProp = new GatewayHomeRetriever(mock(EnvHelper.class), mock(PropertyHelper.class));
        noEnvAndProp.getGatewayHome();
    }

}
