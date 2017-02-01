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

package com.redhat.thermostat.server.core.internal.web.filters;

import static com.mongodb.client.model.Filters.*;

import java.util.ArrayList;
import java.util.List;

import org.bson.conversions.Bson;

import com.mongodb.client.model.Filters;

public class RequestFilters {

    public static Bson buildGetFilter(String agentId, List<String> tags) {
        return buildGetFilter(agentId, null, tags, null, null);
    }

    public static Bson buildGetFilter(String agentId, List<String> tags, String maxTimestamp, String minTimestamp) {
        return buildGetFilter(agentId, null, tags, maxTimestamp, minTimestamp);
    }

        /**
         * Builds a filter suitable for get requests
         * @param agentId an agentId to match
         * @param vmId a vmId to match
         * @param maxTimestamp a maximum timestamp
         * @param minTimestamp a minimum timestamp
         * @return the Bson filter
         */
    private static Bson buildGetFilter(String agentId, String vmId, List<String> tags, String maxTimestamp, String minTimestamp) {

        List<Bson> filters = new ArrayList<>();

        if (agentId != null && !agentId.equals("all")) {
            filters.add(eq("agentId", agentId));
        }

        if (vmId != null && !vmId.equals("all")) {
            filters.add(eq("vmId", vmId));
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                filters.add(or(Filters.exists("tags", false), eq("tags", tag)));
            }
        }

        if (maxTimestamp != null) {
            filters.add(lte("timeStamp", Long.valueOf(maxTimestamp)));
        }
        if (minTimestamp != null) {
            filters.add(gte("timeStamp", Long.valueOf(minTimestamp)));
        }

        return and(filters);
    }
}
