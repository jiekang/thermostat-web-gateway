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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.config.GlobalConfiguration;
import com.redhat.thermostat.gateway.server.services.CoreService;
import com.redhat.thermostat.gateway.server.services.CoreServiceBuilder;

public class CoreServerBuilderTest {

    @Test
    public void testBuild() {
        CoreServiceBuilder serviceBuilder = mock(CoreServiceBuilder.class);
        List<CoreService> serviceList = new ArrayList<>();
        CoreService service = mock(CoreService.class);
        ServletContextHandler servletContextHandler = mock(ServletContextHandler.class);
        when(servletContextHandler.getServer()).thenReturn(mock(Server.class));
        when(service.createServletContextHandler(any(Server.class))).thenReturn(servletContextHandler);
        serviceList.add(service);
        when(serviceBuilder.build()).thenReturn(serviceList);

        Map<String, Object> configMap = new HashMap<>();
        String ip = "127.0.0.1";
        String port = "8080";
        configMap.put(GlobalConfiguration.ConfigurationKey.IP.name(), ip);
        configMap.put(GlobalConfiguration.ConfigurationKey.PORT.name(), port);
        Configuration configuration = mock(Configuration.class);
        when(configuration.asMap()).thenReturn(configMap);


        CoreServerBuilder builder = new CoreServerBuilder();
        builder.setServerConfiguration(configuration);
        builder.setServiceBuilder(serviceBuilder);

        Server server = builder.build();

        ContextHandlerCollection handler = (ContextHandlerCollection) server.getHandler();
        // Expects 2 handlers, 1 service mocked above and 1 swagger ui handler
        assertEquals(2, handler.getHandlers().length);

        ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        assertEquals(connector.getPort(), Integer.parseInt(port));
        assertEquals(connector.getHost(), ip);
    }
}
