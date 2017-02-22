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

import com.mongodb.BasicDBList;
import com.mongodb.client.model.Filters;

public class RequestFilters {

    public static Bson buildGetFilter(List<String> tags) {
        return buildGetFilter(null, tags);
    }

    public static Bson buildGetFilter(String id, List<String> tags) {
        return buildGetFilter(id, tags);
    }

    public static Bson buildGetFilter(String systemId, String agentId, String jvmId, List<String> tags) {
        List<Bson> filters = new ArrayList<>();

        if (systemId != null && !(systemId.equals("all"))) {
            filters.add(eq("systemId", systemId));
        }

        if (agentId != null && !(agentId.equals("all"))) {
            filters.add(eq("agentId", agentId));
        }

        if (jvmId != null && !(jvmId.equals("all"))) {
            filters.add(eq("vmId", jvmId));
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                filters.add(or(Filters.exists("tags", false), eq("tags", tag)));
            }
        }

        return and(filters);
    }

    private static final String DELIM_SPLIT = "((?<=%1$s)|(?=%1$s))";

    public static Bson buildPostFilter(BasicDBList queries, String systemId, List<String> tags) {
        List<Bson> filters = new ArrayList<>();
        for (Object filter : queries) {
            String s[] = filter.toString().split(String.format(DELIM_SPLIT, "(>=|<=|=|>|<)"));

            //TODO: Set comparator, key, value from input of:
            // <string><comparator><string>
            // key:comparator:value

            String comparator = null;
            String key = null;
            String value = null;
            switch (comparator) {
                case "<=":
                    filters.add(lte(key, value));
                    break;
                case ">=":
                    filters.add(gte(key, value));
                    break;
                case "=":
                    filters.add(eq(key, value));
                    break;
                case ">":
                    filters.add(gt(key, value));
                    break;
                case "<":
                    filters.add(lt(key, value));
                    break;
            }
        }
    }
}
