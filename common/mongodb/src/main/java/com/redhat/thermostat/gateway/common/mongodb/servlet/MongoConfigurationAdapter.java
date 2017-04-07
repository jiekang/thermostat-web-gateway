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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.redhat.thermostat.gateway.common.core.Configuration;
import com.redhat.thermostat.gateway.common.mongodb.configuration.MongoConfiguration;

class MongoConfigurationAdapter implements Configuration {

    private final Configuration config;

    MongoConfigurationAdapter(Configuration config) {
        this.config = config;
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> adaptedConfig = adaptConfig();
        return Collections.unmodifiableMap(adaptedConfig);
    }

    private Map<String, String> adaptConfig() {
        Map<String, String> result = new HashMap<>();
        Map<String, String> totalConfig = config.asMap();
        for (MongoConfiguration item: MongoConfiguration.values()) {
            if (totalConfig.containsKey(item.name())) {
                result.put(item.name(), totalConfig.get(item.name()));
            }
        }
        return result;
    }
}
