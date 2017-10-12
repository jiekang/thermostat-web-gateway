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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.ConfigurationFactory;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilder;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilderFactory;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilderFactory.CoreServiceType;

public class Start implements Runnable {

    private Server server = null;
    private AbstractLifeCycle.AbstractLifeCycleListener listener = null;
    private final ConfigurationFactory factory;

    public Start() {
        this(null, new ConfigurationFactory());
    }

    public Start(AbstractLifeCycle.AbstractLifeCycleListener listener) {
        this(listener, new ConfigurationFactory());
    }

    public Start(AbstractLifeCycle.AbstractLifeCycleListener listener, ConfigurationFactory factory) {
        this.listener = listener;
        this.factory = factory;
    }

    public void run() {
        CoreServerBuilder serverBuilder = new CoreServerBuilder();
        setServerConfig(serverBuilder, factory);
        setServiceBuilder(serverBuilder, factory);

        server = serverBuilder.build();
        if (listener != null) {
            server.addLifeCycleListener(listener);
        }

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            server.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setServerConfig(CoreServerBuilder builder, ConfigurationFactory factory) {
        Configuration globalConfig = factory.createGlobalConfiguration();
        builder.setServerConfiguration(globalConfig);
    }

    private void setServiceBuilder(CoreServerBuilder builder, ConfigurationFactory factory) {
        Configuration globalServicesConfig = factory.createGlobalServicesConfig();
        CoreServiceBuilderFactory builderFactory = new CoreServiceBuilderFactory(factory);
        CoreServiceBuilder coreServiceBuilder = builderFactory.createBuilder(CoreServiceType.WEB_ARCHIVE);
        coreServiceBuilder.setConfiguration(globalServicesConfig);
        builder.setServiceBuilder(coreServiceBuilder);
    }

    public static void main(String[] args) {
        Start start = new Start();
        start.run();
    }


}
