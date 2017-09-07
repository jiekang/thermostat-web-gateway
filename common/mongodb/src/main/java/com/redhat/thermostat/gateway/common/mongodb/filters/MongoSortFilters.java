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

import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;

public class MongoSortFilters {

    public static final Character DESCENDING_SORT = '-';
    public static final Character ASCENDING_SORT = '+';

    public static Bson createSortObject(String sort) {
        BasicDBObject sortObject = new BasicDBObject();
        if (sort != null) {
            String[] items = sort.split(",");
            for (String item : items) {
                if (item.charAt(0) == ASCENDING_SORT) {
                    sortObject.append(item.substring(1), 1);
                } else if (item.charAt(0) == DESCENDING_SORT) {
                    sortObject.append(item.substring(1), -1);
                }
            }
        }
        return sortObject;
    }
}
