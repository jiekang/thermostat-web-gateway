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

package com.redhat.thermostat.wp.service;

import org.eclipse.jetty.client.api.ContentResponse;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

/**
 */
@Path("/")
public class WhitePagesHttpHandler {

    @GET
    @Path("jvms")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response listJVMs(final @Context ServletContext context) {

        final String reply[] = { "Thermostat White Pages Service" };

        try {
            WebClient.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<VM> jvms = new ArrayList<>();
                        JVMParser parser = new JVMParser();

                        List<String> gateways = WPConfigurationKeys.JVM_GATEWAY_URLS.get(context);
                        for (String gateway : gateways) {
                            ContentResponse content = WebClient.GET(gateway);
                            String contentResponse = content.getContentAsString();

                            parser.parse(contentResponse, gateway, jvms);
                        }

                        reply[0] = parser.toJson(jvms);

                    } catch (Exception e) {
                        e.printStackTrace();
                        reply[0] = e.getMessage();
                    }
                }
            });

        } catch (Exception e) {
            return Response.status(Status.EXPECTATION_FAILED).entity(e.getMessage() + "\n").build();
        }

        return Response.status(Response.Status.OK).entity(reply[0] + "\n").build();
    }
}
