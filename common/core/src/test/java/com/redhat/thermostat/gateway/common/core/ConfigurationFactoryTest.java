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

package com.redhat.thermostat.gateway.common.core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationFactoryTest extends ConfigurationTest {

    private ConfigurationFactory factory;
    private Map<String, Object> services;

    @Before
    public void setup() {
        factory = new ConfigurationFactory(getTestRoot());
        services = new HashMap<String, Object>();
        services.put("/service1", "/path/to/microservice.war");
    }

    @Test
    public void canGetMergedConfigForService() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("foo", "service-value"); // override from service config
        expected.put("bar", "baz"); // global only config
        expected.put("test", "me"); // service only config
        expected.put("SERVICES", services);
        Configuration config = factory.createServiceConfiguration("test-service");
        assertEquals(expected, config.asMap());
    }

    @Test
    public void canGetGlobalServicesConfig() {
        Configuration globalServicesConfig = factory.createGlobalServicesConfig();
        Map<String, Object> expected = new HashMap<>();
        expected.put("/service1", "/path/to/microservice.war");

        assertEquals(expected, globalServicesConfig.asMap());
    }

    @Test
    public void canGetGlobalConfig() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("foo", "bar");
        expected.put("bar", "baz");
        expected.put("SERVICES", services);
        Configuration globalConfig = factory.createGlobalConfiguration();
        assertEquals(expected, globalConfig.asMap());
    }
}
