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

package com.redhat.thermostat.gateway.common.mongodb.filters;

import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.lte;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.size;
import static com.mongodb.client.model.Filters.or;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonParseException;

import com.redhat.thermostat.gateway.common.mongodb.keycloak.KeycloakFields;

public class MongoRequestFilters {

    private static final Pattern operatorPattern = Pattern.compile("(<=|>=|<|>|==|!=)");

    public static Bson buildEq(final String key, final String value) {
        return eq(key, value);
    }

    public static Bson buildAnd(final Bson a1, final Bson a2) {
        return and(a1, a2);
    }

    public static Bson buildOr(final Bson a1, final Bson a2) {
        return or(a1, a2);
    }

    public static List<Bson> buildQueriesList(List<String> queries) {
        final List<Bson> filters = new ArrayList<>();
        for (String filter : queries) {
            final Matcher m = operatorPattern.matcher(filter);
            if (m.find()) {
                String key = filter.substring(0, m.start());
                String comparator = filter.substring(m.start(), m.end());
                String valueInput = filter.substring(m.end());

                Object value;

                try {
                    value = Double.parseDouble(valueInput);
                } catch (Exception e) {
                    if (valueInput.startsWith("\"") && valueInput.endsWith("\"")) {
                        value = valueInput.substring(1, valueInput.length()-1);
                    } else {
                        value = valueInput;
                    }
                }

                switch (comparator) {
                    case "<=":
                        filters.add(lte(key, value));
                        break;
                    case ">=":
                        filters.add(gte(key, value));
                        break;
                    case "==":
                        filters.add(eq(key, value));
                        break;
                    case "!=":
                        filters.add(ne(key, value));
                        break;
                    case ">":
                        filters.add(gt(key, value));
                        break;
                    case "<":
                        filters.add(lt(key, value));
                        break;
                }
            } else {
                throw new JsonParseException("No relation found in: " + filter);
            }
        }
        return filters;
    }

    public static List<Bson> buildQueriesList(final String queryStr) {
        if (queryStr == null || queryStr.isEmpty()) {
            return Collections.EMPTY_LIST;
        } else {
            final List<String> queries = Arrays.asList(queryStr.split(","));
            final List<Bson> filters = buildQueriesList(queries);
            return filters;
        }
    }

    public static Bson buildQueriesFilter(final List<String> queries) {
        return and(buildQueriesList(queries));
    }

    public static Bson buildQueriesFilter(final String queryStr) {
        return and(buildQueriesList(queryStr));
    }

    public static Bson buildRealmsFilter(Set<String> realms) {
        return and(all(KeycloakFields.REALMS_KEY, realms), size(KeycloakFields.REALMS_KEY, realms.size()));
    }

    public static Bson buildQuery(List<String> clientQueries, Set<String> realms) {
        Bson query = null;

        if (clientQueries != null && !clientQueries.isEmpty()) {
            query = MongoRequestFilters.buildQueriesFilter(clientQueries);
        }

        if (realms != null && realms.size() > 0) {
            Bson realmsQuery = MongoRequestFilters.buildRealmsFilter(realms);
            if (query != null) {
                query = and(query, realmsQuery);
            } else {
                query = realmsQuery;
            }
        }

        if (query == null) {
            query = new Document();
        }

        return query;
    }
}
