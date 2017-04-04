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

package com.redhat.thermostat.gateway.server;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.eclipse.jetty.server.Server;

import com.redhat.thermostat.gateway.common.core.Configuration;
import com.redhat.thermostat.gateway.common.core.ConfigurationFactory;
import com.redhat.thermostat.gateway.common.core.GlobalConfiguration;

public class Start {

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Expected 1 and only one init param: THERMOSTAT_GATEWAY_HOME");
        }
        String gatewayHome = args[0];

        ConfigurationFactory factory = new ConfigurationFactory(gatewayHome);
        CoreServerBuilder serverBuilder = new CoreServerBuilder();
        setListenConfig(serverBuilder, factory);
        addServices(serverBuilder, factory);

        Server server = serverBuilder.build();

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setListenConfig(CoreServerBuilder builder, ConfigurationFactory factory) {
        Configuration globalConfig = factory.createGlobalConfiguration();
        Map<String, String> serverConfigMap = globalConfig.asMap();
        String listenAddress = serverConfigMap.get(GlobalConfiguration.ConfigurationKey.IP.toString());
        builder.setListenAddress(listenAddress);
        int port = Integer.parseInt(serverConfigMap.get(GlobalConfiguration.ConfigurationKey.PORT.toString()));
        builder.setListenPort(port);
    }

    private static void addServices(CoreServerBuilder builder, ConfigurationFactory factory) {
        Configuration globalServicesConfig = factory.createGlobalServicesConfig();

        Map<String, String> configProperties = globalServicesConfig.asMap();

        for (Map.Entry<String, String> entry : configProperties.entrySet()) {
            Path warPath = Paths.get(entry.getValue());
            builder.addWebapp(entry.getKey(), warPath);
        }

    }
}
