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

package com.redhat.thermostat.gateway.service.system.network;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpMethod;

import com.redhat.thermostat.gateway.tests.keycloak.BasicKeycloakIntegrationTestSuite;
import com.redhat.thermostat.gateway.tests.utils.EndpointDefinition;

public class SystemNetworkKeycloakIntegrationTest extends BasicKeycloakIntegrationTestSuite {
    private static final String serviceName = "system-network";
    private static final String collectionName = "network-info";
    private static final String versionNumber = "0.0.1";

    private static final String postData = "[{\"agentId\":\"a06056f2-46bb-418f-9c07-35ebf2d67726\",\"timeStamp\":{\"$numberLong\":\"1501164373711\"},\"interfaces\":[{\"interfaceName\":\"veth1dd73a4\",\"displayName\":\"veth1dd73a4\",\"ip6Addr\":\"fe80:0:0:0:89f:deff:fe8a:da94%veth1dd73a4\"},{\"interfaceName\":\"virbr3\",\"displayName\":\"virbr3\",\"ip4Addr\":\"192.168.102.1\"},{\"interfaceName\":\"virbr0\",\"displayName\":\"virbr0\",\"ip4Addr\":\"192.168.124.1\"},{\"interfaceName\":\"virbr1\",\"displayName\":\"virbr1\",\"ip4Addr\":\"192.168.100.1\"},{\"interfaceName\":\"virbr2\",\"displayName\":\"virbr2\",\"ip4Addr\":\"192.168.101.1\"},{\"interfaceName\":\"docker0\",\"displayName\":\"docker0\",\"ip4Addr\":\"172.17.0.1\",\"ip6Addr\":\"fe80:0:0:0:42:bfff:feff:1d42%docker0\"},{\"interfaceName\":\"wlp3s0\",\"displayName\":\"wlp3s0\",\"ip4Addr\":\"10.193.245.52\",\"ip6Addr\":\"fe80:0:0:0:922e:1cff:fe29:365%wlp3s0\"},{\"interfaceName\":\"enp0s25\",\"displayName\":\"enp0s25\",\"ip4Addr\":\"10.15.17.134\",\"ip6Addr\":\"fe80:0:0:0:56ee:75ff:fe97:634e%enp0s25\"},{\"interfaceName\":\"lo\",\"displayName\":\"lo\",\"ip4Addr\":\"127.0.0.1\",\"ip6Addr\":\"0:0:0:0:0:0:0:1%lo\"}]}]";
    private static final String putData = "{\"set\":{\"a\":\"b\"}}";

    public SystemNetworkKeycloakIntegrationTest() {
        super(serviceName, versionNumber, collectionName);
    }

    @Override
    protected List<EndpointDefinition> getEndpointList() {
        List<EndpointDefinition> endpointDefinitions = new ArrayList<>();
        endpointDefinitions.add(new EndpointDefinition(HttpMethod.GET, "/systems/sid1", EndpointDefinition.NO_BODY));

        endpointDefinitions.add(new EndpointDefinition(HttpMethod.POST, "/systems/sid1", postData));

        endpointDefinitions.add(new EndpointDefinition(HttpMethod.PUT, "/systems/sid1", putData));

        endpointDefinitions.add(new EndpointDefinition(HttpMethod.DELETE, "/systems/sid1", EndpointDefinition.NO_BODY));

        return endpointDefinitions;
    }
}
