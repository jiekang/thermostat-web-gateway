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

package com.redhat.thermostat.gateway.common.core.model;

import com.redhat.thermostat.gateway.common.core.jaxrs.InvalidParameterValueException;

public class LimitParameter {

    private final Integer limit;

    public LimitParameter(Integer limit) {
        this.limit = limit;
    }

    public Integer getValue() {
        return limit;
    }

    public static LimitParameter valueOf(String rawParam) {
        if (rawParam == null) {
            return null;
        }
        Integer intVal = Integer.valueOf(rawParam);
        if (intVal < 0) {
            // JAX RS throws 404 on illegal parameter types by default, make it throw
            // 400 instead.
            throw new InvalidParameterValueException("Limit value must not be negative");
        }
        return new LimitParameter(intVal);
    }
}