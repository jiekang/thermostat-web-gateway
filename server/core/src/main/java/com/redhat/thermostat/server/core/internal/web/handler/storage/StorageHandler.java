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

package com.redhat.thermostat.server.core.internal.web.handler.storage;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.SecurityContext;

public interface StorageHandler {
    void getSystems(SecurityContext context, AsyncResponse asyncResponse, String namespace, String offset, String limit, String sort);

    void putSystems(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace);

    void postSystems(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String offset, String limit, String sort);

    void deleteSystems(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace);

    void getSystem(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId);

    void putSystem(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace);

    void deleteSystem(SecurityContext context, AsyncResponse asyncResponse, String namespace);

    void getAgents(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId, String offset, String limit, String sort);

    void putAgents(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId);


    void postAgents(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String offset, String limit, String sort);


    void deleteAgents(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId);

    void getAgent(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId, String agentId);

    void putAgent(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId);

    void deleteAgent(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId);

    void getJvms(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String vmId, String offset, String limit, String sort);

    void putJvms(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId);

    void postJvms(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String offset, String limit, String sort);

    void deleteJvms(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId);

    void getJvm(SecurityContext securityContext, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String jvmId);

    void putJvm(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String jvmId);

    void deleteJvm(String body, SecurityContext context, AsyncResponse asyncResponse, String namespace, String systemId, String agentId, String jvmId);
}
