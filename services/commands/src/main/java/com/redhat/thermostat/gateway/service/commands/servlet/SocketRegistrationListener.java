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

package com.redhat.thermostat.gateway.service.commands.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import com.redhat.thermostat.gateway.common.core.config.Configuration;
import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;
import com.redhat.thermostat.gateway.common.util.LoggingUtil;
import com.redhat.thermostat.gateway.service.commands.channel.endpoints.CommandChannelAgentEndpointHandler;
import com.redhat.thermostat.gateway.service.commands.channel.endpoints.CommandChannelClientEndpointHandler;
import com.redhat.thermostat.gateway.service.commands.channel.endpoints.CommandChannelEndpointHandlerFactory;

public class SocketRegistrationListener implements ServletContextListener {

    private static final Logger logger = LoggingUtil.getLogger(SocketRegistrationListener.class);
    private static final String SERVER_CONTAINER_ATTR = "javax.websocket.server.ServerContainer";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctxt = sce.getServletContext();
        Configuration serviceConfig = (Configuration)ctxt.getAttribute(GlobalConstants.SERVICE_CONFIG_KEY);
        String version = ctxt.getInitParameter(GlobalConstants.SERVICE_VERSION_KEY);
        ServerContainer container = (ServerContainer)ctxt.getAttribute(SERVER_CONTAINER_ATTR);
        String agentPath = buildPathWithVersion(version, CommandChannelAgentEndpointHandler.PATH);
        String clientPath = buildPathWithVersion(version, CommandChannelClientEndpointHandler.PATH);
        logger.log(Level.CONFIG, "Setting up agent (receiver) web-socket endpoint at: " + agentPath);
        logger.log(Level.CONFIG, "Setting up client (initiator) web-socket endpoint at: " + clientPath);
        CommandChannelEndpointHandlerFactory configFactory = new CommandChannelEndpointHandlerFactory();
        ServerEndpointConfig agentConf = configFactory.createEndpointConfig(CommandChannelAgentEndpointHandler.class,
                                                                            agentPath,
                                                                            serviceConfig);
        ServerEndpointConfig clientConf = configFactory.createEndpointConfig(CommandChannelClientEndpointHandler.class,
                                                                             clientPath,
                                                                             serviceConfig);
        try {
            container.addEndpoint(agentConf);
            container.addEndpoint(clientConf);
        } catch (DeploymentException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // no-op
    }

    private String buildPathWithVersion(final String version, final String path) {
        return "/" + version  + path;
    }

}
