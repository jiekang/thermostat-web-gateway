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

package com.redhat.thermostat.web.setup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;

public class TimedTestSetup {
    protected static final Map<String, List<Long>> times = new HashMap<>();

    @AfterClass
    public static void afterClassTimedTestSetup() throws Exception {
        if (times.size() > 0) {
            for (Map.Entry<String, List<Long>> time : times.entrySet()) {
                List<Long> values = time.getValue();
                double sum = 0;
                long max = values.get(0);
                long min = max;
                for (long t : values) {
                    sum += t;
                    if (max < t) {
                        max = t;
                    }
                    if (min > t) {
                        min = t;
                    }
                }
                double average = (sum - max - min) / values.size();

                System.out.println(time.getKey());
                System.out.println("Average: " + (long) average);
                System.out.println("Max: " + max);
                System.out.println("Min: " + min);
                System.out.println("Sum: " + (long) sum);
                System.out.println();
            }
        }

    }
}
