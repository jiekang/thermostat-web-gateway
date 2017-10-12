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

package com.redhat.thermostat.gateway.tests.utils.keycloak;

import java.util.HashMap;
import java.util.Map;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ConfigurationFactory;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration;
import com.redhat.thermostat.gateway.common.core.config.ServiceConfiguration;
import com.redhat.thermostat.gateway.common.mongodb.configuration.MongoConfiguration;

public class KeycloakTestConfigurationFactory extends ConfigurationFactory {

    public static final int TEST_PORT = 40000;
    public static final int MONGOD_TEST_PORT = 42000;

    public KeycloakTestConfigurationFactory() {
        super();
    }

    @Override
    public Configuration createServiceConfiguration(String serviceName) {
        Map<String, Object> configMap = new HashMap<>(super.createServiceConfiguration(serviceName).asMap());
        configMap.put(ServiceConfiguration.ConfigurationKey.SECURITY_KEYCLOAK.name(), "true");
        configMap.put(ServiceConfiguration.ConfigurationKey.KEYCLOAK_CONFIG.name(), KeycloakServerCredentials.KEYCLOAK_JSON);
        configMap.put(MongoConfiguration.MONGO_URL.name(), "mongodb://127.0.0.1:" + MONGOD_TEST_PORT);
        return new KeycloakTestConfiguration(configMap);
    }

    @Override
    public Configuration createGlobalConfiguration() {
        final Map<String, Object> configMap = new HashMap<>(super.createGlobalConfiguration().asMap());
        configMap.put(GlobalConfiguration.ConfigurationKey.PORT.name(), Integer.toString(TEST_PORT));
        return new Configuration() {
            @Override
            public Map<String, Object> asMap() {
                return configMap;
            }
        };
    }

    @Override
    public Configuration createGlobalServicesConfig() {
        return super.createGlobalServicesConfig();
    }

    private class KeycloakTestConfiguration implements Configuration {
        private final Map<String, Object> configMap;

        KeycloakTestConfiguration(Map<String, Object> configMap) {
            this.configMap = configMap;
        }

        @Override
        public Map<String, Object> asMap() {
            return configMap;
        }
    }
}
