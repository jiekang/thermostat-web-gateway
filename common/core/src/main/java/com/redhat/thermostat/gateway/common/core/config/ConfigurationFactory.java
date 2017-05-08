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

import java.util.Map;

/**
 * Configuration factory for creating relevant configuration instances.
 *
 */
public class ConfigurationFactory {

    private final String gatewayHome;
    private final GlobalConfiguration globalConfig;

    public ConfigurationFactory(String gatewayHome) {
        this.gatewayHome = gatewayHome;
        this.globalConfig = new GlobalConfiguration(gatewayHome);
    }

    /**
     * Creates the specific service configuration for the named service.
     *
     * @param serviceName The name of the service.
     *
     * @return The specific service configuration.
     */
    public Configuration createServiceConfiguration(String serviceName) {
        Configuration serviceConfig = new ServiceConfiguration(gatewayHome, serviceName);
        return new ConfigurationMerger(globalConfig, serviceConfig);
    }

    /**
     * Creates the global server (servlet container) configuration.
     *
     * @return The server configuration.
     */
    public Configuration createGlobalConfiguration() {
        return globalConfig;
    }

    /**
     * Creates the global configuration that specifies which services shall
     * get deployed in the server.
     *
     * @return The to-be-deployed services configuration.
     */
    public Configuration createGlobalServicesConfig() {
        return new Configuration() {

            @SuppressWarnings("unchecked")
            @Override
            public Map<String, Object> asMap() {
                return (Map<String, Object>)globalConfig.asMap().get(GlobalConfiguration.ConfigurationKey.SERVICES.name());
            }

        };
    }
}
