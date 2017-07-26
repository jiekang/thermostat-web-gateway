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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.servlet.GatewayHomeSettingContextListener;
import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;
import com.redhat.thermostat.gateway.common.mongodb.ThermostatMongoStorage;

public class StorageConnectionSettingListener extends GatewayHomeSettingContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        super.contextDestroyed(event);
        ServletContext ctx = event.getServletContext();
        synchronized (ctx) {
            ctx.setAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE, null);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        super.contextInitialized(event);
        ServletContext ctx = event.getServletContext();
        synchronized (ctx) {
            Map<String, String> config = getMongoStorageConfig(ctx);
            ThermostatMongoStorage storage = new ThermostatMongoStorage(config);
            ctx.setAttribute(ServletContextConstants.MONGODB_CLIENT_ATTRIBUTE, storage);
        }
    }

    Map<String, String> getMongoStorageConfig(ServletContext ctx) {
        Configuration serviceConfig = (Configuration) ctx.getAttribute(GlobalConstants.SERVICE_CONFIG_KEY);
        if (serviceConfig == null) {
            throw new IllegalStateException("No service configuration for service: " + ctx.getInitParameter(GlobalConstants.SERVICE_NAME_KEY));
        }
        Configuration mongoConfiguration = new MongoConfigurationAdapter(serviceConfig);
        return convert(mongoConfiguration.asMap());
    }

    private Map<String, String> convert(Map<String, Object> orig) {
        Map<String, String> converted = new HashMap<>();
        for (Entry<String, Object> entry: orig.entrySet()) {
            converted.put(entry.getKey(), (String)entry.getValue());
        }
        return converted;
    }

}
