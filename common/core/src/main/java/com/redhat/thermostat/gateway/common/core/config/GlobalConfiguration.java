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

import java.util.Collections;
import java.util.Map;

public class GlobalConfiguration extends BasicConfiguration {

    private final CommonPaths paths;
    private final Map<String, Object> map;

    // This should remain package private. Use ConfigurationFactory to get an instance.
    GlobalConfiguration(String gatewayHome) {
        paths = new CommonPaths(gatewayHome);
        map = loadConfig(paths.getGlobalConfigFilePath(), paths.getConfigDir());
    }

    @Override
    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(map);
    }

    public enum ConfigurationKey {
        /**
         * The listen address of the servlet container server.
         */
        IP,
        /**
         * The listen port of the servlet container server.
         */
        PORT,
        /**
         * The services file name (next to {@code global-config.properties})
         * specifying which microservices the servlet container shall deploy.
         *
         */
        SERVICES,
        /**
         * Specifies whether or not the swagger UI handler should get
         * created under context path /doc
         */
        WITH_SWAGGER_UI,
        /**
         * Specifies whether or not a static resource handler, at
         * context path /web-client, for built web-client assets
         * should get created.
         */
        WITH_WEB_CLIENT
    }

}