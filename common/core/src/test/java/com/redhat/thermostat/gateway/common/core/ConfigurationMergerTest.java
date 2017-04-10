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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationMergerTest {

    private Configuration globalConfig;
    private Configuration serviceConfig;

    @Before
    public void setup() {
        globalConfig = mock(Configuration.class);
        serviceConfig = mock(Configuration.class);
    }

    @Test
    public void canGetGlobalConfigOnly() {
        boolean globalOnly = true;
        mergedOneConfigOnlyTest(globalOnly);
    }

    @Test
    public void canGetServiceConfigOnly() {
        boolean globalOnly = false;
        mergedOneConfigOnlyTest(globalOnly);
    }

    private void mergedOneConfigOnlyTest(boolean globalOnly) {
        Map<String, Object> expected = new HashMap<>();
        expected.put("foo", "foo-val");
        expected.put("some", "value");
        Map<String, Object> global;
        Map<String, Object> service;
        if (globalOnly) {
            global = expected;
            service = Collections.emptyMap();
        } else {
            service = expected;
            global = Collections.emptyMap();
        }
        when(globalConfig.asMap()).thenReturn(global);
        when(serviceConfig.asMap()).thenReturn(service);
        Configuration config = new ConfigurationMerger(globalConfig, serviceConfig);
        Map<String, Object> actual = config.asMap();
        assertEquals(expected, actual);
    }


    @Test
    public void canGetMergedWithGlobalOnlyKey() {
        mergedWithXOnlyKey(true, "bar-val");
    }

    @Test
    public void canGetMergedWithServiceOnlyKey() {
        // expect "foo" to not be overriden by larger global set.
        mergedWithXOnlyKey(false, "foo-val");
    }

    private void mergedWithXOnlyKey(boolean globalHasOnlyKey, String overrideVal) {
        Map<String, Object> moreKeys = new HashMap<>();
        moreKeys.put("foo", "foo-val");
        moreKeys.put("some", "value");
        Map<String, Object> lessKeysWithOverride = new HashMap<>();
        lessKeysWithOverride.put("foo", overrideVal);
        Map<String, Object> global;
        Map<String, Object> service;
        if (globalHasOnlyKey) {
            global = moreKeys;
            service = lessKeysWithOverride;
        } else {
            global = lessKeysWithOverride;
            service = moreKeys;
        }

        when(globalConfig.asMap()).thenReturn(global);
        when(serviceConfig.asMap()).thenReturn(service);
        Configuration config = new ConfigurationMerger(globalConfig, serviceConfig);
        Map<String, Object> actual = config.asMap();
        Map<String, Object> expected = new HashMap<>();
        expected.put("foo", overrideVal);
        expected.put("some", "value");
        assertEquals(expected, actual);
    }
}
