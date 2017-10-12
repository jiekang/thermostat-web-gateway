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

package com.redhat.thermostat.gateway.service.jvm.memory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpMethod;

import com.redhat.thermostat.gateway.tests.keycloak.BasicKeycloakIntegrationTestSuite;
import com.redhat.thermostat.gateway.tests.utils.EndpointDefinition;

public class JvmMemoryServiceKeycloakIntegrationTest extends BasicKeycloakIntegrationTestSuite {
    private static final String serviceName = "jvm-memory";
    private static final String versionNumber = "0.0.3";

    private static final String postData =
            "[{\"timeStamp\":{\"$numberLong\":\"1501104816287\"},\"metaspaceMaxCapacity\":{\"$numberLong\":\"1094713344\"},\"metaspaceMinCapacity\":{\"$numberLong\":\"0\"},\"metaspaceCapacity\":{\"$numberLong\":\"21807104\"},\"metaspaceUsed\":{\"$numberLong\":\"19997080\"},\"generations\":[{\"name\":\"new\",\"capacity\":{\"$numberLong\":\"149422080\"},\"maxCapacity\":{\"$numberLong\":\"1369964544\"},\"collector\":\"PSScavenge\",\"spaces\":[{\"index\":0,\"name\":\"eden\",\"capacity\":{\"$numberLong\":\"65011712\"},\"maxCapacity\":{\"$numberLong\":\"1368915968\"},\"used\":{\"$numberLong\":\"10296232\"}},{\"index\":1,\"name\":\"s0\",\"capacity\":{\"$numberLong\":\"8912896\"},\"maxCapacity\":{\"$numberLong\":\"456654848\"},\"used\":{\"$numberLong\":\"0\"}},{\"index\":2,\"name\":\"s1\",\"capacity\":{\"$numberLong\":\"10485760\"},\"maxCapacity\":{\"$numberLong\":\"456654848\"},\"used\":{\"$numberLong\":\"0\"}}]},{\"name\":\"old\",\"capacity\":{\"$numberLong\":\"73924608\"},\"maxCapacity\":{\"$numberLong\":\"2740453376\"},\"collector\":\"PSParallelCompact\",\"spaces\":[{\"index\":0,\"name\":\"old\",\"capacity\":{\"$numberLong\":\"73924608\"},\"maxCapacity\":{\"$numberLong\":\"2740453376\"},\"used\":{\"$numberLong\":\"8617920\"}}]}]}]";
    private static final String putData = "{\"set\":{\"a\":\"b\"}}";

    public JvmMemoryServiceKeycloakIntegrationTest() {
        super(serviceName, versionNumber, serviceName);
    }


    @Override
    protected List<EndpointDefinition> getEndpointList() {
        List<EndpointDefinition> endpointDefinitions = new ArrayList<>();
        endpointDefinitions.add(new EndpointDefinition(HttpMethod.GET, "/jvms/jid1", EndpointDefinition.NO_BODY));
        endpointDefinitions.add(new EndpointDefinition(HttpMethod.GET, "/systems/sid1/jvms/jid1", EndpointDefinition.NO_BODY));
        endpointDefinitions.add(new EndpointDefinition(HttpMethod.POST, "/systems/sid1/jvms/jid1", postData));
        endpointDefinitions.add(new EndpointDefinition(HttpMethod.PUT, "/systems/sid1/jvms/jid1", putData));
        endpointDefinitions.add(new EndpointDefinition(HttpMethod.DELETE, "/systems/sid1/jvms/jid1", EndpointDefinition.NO_BODY));
        return endpointDefinitions;
    }
}
