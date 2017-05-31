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

package com.redhat.thermostat.gateway.server.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.thermostat.gateway.common.util.OS;
import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ConfigurationFactory;
import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;
import com.redhat.thermostat.gateway.server.services.WebArchiveServiceBuilder.EnvHelper;
import com.redhat.thermostat.gateway.server.services.WebArchiveServiceBuilder.PathHelper;

public class WebArchiveServiceBuilderTest {

    private EnvHelper envHelper;
    private PathHelper pathHelper;
    private ConfigurationFactory configFactory;

    @Before
    public void setup() {
        envHelper = mock(EnvHelper.class);
        pathHelper = mock(PathHelper.class);
        when(pathHelper.isAbsolute(anyString())).thenReturn(true);
        configFactory = mock(ConfigurationFactory.class);
        when(configFactory.createServiceConfiguration(anyString())).thenReturn(mock(Configuration.class));
    }

    @Test
    public void testBuildWebArchiveCoreServices() {

        WebArchiveServiceBuilder serviceBuilder = new WebArchiveServiceBuilder(configFactory, envHelper, pathHelper);
        Configuration configuration = mock(Configuration.class);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("/context0", "serviceName0");
        configMap.put("/context1", "serviceName1");

        when(configuration.asMap()).thenReturn(configMap);
        serviceBuilder.setConfiguration(configuration);

        List<CoreService> services = serviceBuilder.build();
        assertEquals(2, services.size());

        assertTrue(services.get(0) instanceof WebArchiveCoreService);
        assertTrue(services.get(1) instanceof WebArchiveCoreService);
    }

    @Test
    public void canResolveRelativeServiceWarFile() {
        String mockGwHome = "i-do-not-exist-gw-home";
        EnvHelper customEnv = mock(EnvHelper.class);
        when(customEnv.getEnv(eq(GlobalConstants.GATEWAY_HOME_ENV))).thenReturn(mockGwHome);
        WebArchiveServiceBuilder serviceBuilder = new WebArchiveServiceBuilder(configFactory, customEnv, new PathHelper());
        String result = serviceBuilder.getAbsolutePathForService("foobar");
        String expected = new File(new File(mockGwHome, "services"), "foobar").getAbsolutePath();
        assertEquals(expected, result);
    }

    @Test
    public void keepsAbsoluteServiceWarFile() {
        EnvHelper env = mock(EnvHelper.class);
        WebArchiveServiceBuilder serviceBuilder = new WebArchiveServiceBuilder(configFactory, env, new PathHelper());
        String orig = OS.IS_UNIX ? "/abs/path" : "C:\\abs\\path";
        String result = serviceBuilder.getAbsolutePathForService(orig);
        assertEquals(orig, result);
    }
}
