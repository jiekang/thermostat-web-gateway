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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;
import com.redhat.thermostat.gateway.service.commands.channel.endpoints.CommandChannelAgentEndpointHandler;
import com.redhat.thermostat.gateway.service.commands.channel.endpoints.CommandChannelClientEndpointHandler;
import com.redhat.thermostat.gateway.service.commands.channel.endpoints.RealmAuthorizerConfigurator;

public class SocketRegistrationListenerTest {

    private static final String VERSION = "1.0.1";
    private ServletContextEvent event;
    private ServletContext context;
    private ServerContainer serverContainer;

    @Before
    public void setUp() {
        serverContainer = mock(ServerContainer.class);
        context = mock(ServletContext.class);
        when(context.getAttribute(eq("javax.websocket.server.ServerContainer"))).thenReturn(serverContainer);
        when(context.getInitParameter(eq(GlobalConstants.SERVICE_VERSION_KEY))).thenReturn(VERSION);
        event = mock(ServletContextEvent.class);
        when(event.getServletContext()).thenReturn(context);
    }

    @Test
    public void contextInitializedAddsSocketEndpoints() throws DeploymentException {
        SocketRegistrationListener listener = new SocketRegistrationListener();
        listener.contextInitialized(event);
        ArgumentCaptor<ServerEndpointConfig> configCaptor = ArgumentCaptor.forClass(ServerEndpointConfig.class);
        verify(serverContainer, times(2)).addEndpoint(configCaptor.capture());
        List<ServerEndpointConfig> configs = configCaptor.getAllValues();
        ServerEndpointConfig agentConfig = configs.get(0);
        ServerEndpointConfig clientConfig = configs.get(1);
        assertEquals(CommandChannelAgentEndpointHandler.class, agentConfig.getEndpointClass());
        assertEquals(CommandChannelClientEndpointHandler.class, clientConfig.getEndpointClass());
        assertEquals("expected config to be added", 1, agentConfig.getUserProperties().size());
        assertEquals("expected config to be added", 1, clientConfig.getUserProperties().size());
        assertEquals("/1.0.1/actions/{action}/systems/{systemId}/agents/{agentId}/jvms/{jvmId}/sequence/{seqId}", clientConfig.getPath());
        assertEquals("/1.0.1/systems/{systemId}/agents/{agentId}", agentConfig.getPath());
        assertTrue(agentConfig.getConfigurator() instanceof RealmAuthorizerConfigurator);
        assertTrue(clientConfig.getConfigurator() instanceof RealmAuthorizerConfigurator);
    }
}
