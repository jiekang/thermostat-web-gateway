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

package com.redhat.thermostat.gateway.service.jvm.jcmd;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JCMDModelConverterTest {

    private static String RAW_PAYLOAD =
            "{\"timestamp\":{\"timestamp\":\"1505992968442\"},"               +
            "\"payload\":\"Index Super InstCount InstBytes KlassBytes "       +
            "annotations   CpAll MethodCount Bytecodes MethodAll   ROAll   "  +
            "RWAll   Total ClassName,ClassLoader\\n    1    -1      6126    " +
            "398176        464           0       0           0         "      +
            "0         0      24     568     592 [C,NULL class_loader\\n    " +
            "2    48      1614    182816        632           0   "           +
            "20824         130      4973     37080   20768   39544   "        +
            "60312 java.lang.Class,NULL class_loader\\n    3    -1       "    +
            "471    154720        464           0       0           "         +
            "0         0         0      24     568     592 "                  +
            "[B,NULL class_loader\\n"                                         +
            "Index Super InstCount InstBytes KlassBytes annotations   "       +
            "CpAll MethodCount Bytecodes MethodAll   ROAll   RWAll   "        +
            "Total ClassName,ClassLoader\\n\"}";

    @Test
    public void testModelCreation() {

        Model model = JCMDModelConverter.createModel(RAW_PAYLOAD, "0x4A", "42");
        assertTrue(model != null);
        assertEquals(3, model.data.size());

        assertTrue(model.data.containsKey("[C"));
        assertTrue(model.data.containsKey("[B"));
        assertTrue(model.data.containsKey("java.lang.Class"));

        int i = 0;
        assertEquals("Index", model.headers.get(i++));
        assertEquals("Super", model.headers.get(i++));
        assertEquals("InstCount", model.headers.get(i++));
        assertEquals("InstBytes", model.headers.get(i++));
        assertEquals("KlassBytes", model.headers.get(i++));
        assertEquals("annotations", model.headers.get(i++));
        assertEquals("CpAll", model.headers.get(i++));
        assertEquals("MethodCount", model.headers.get(i++));
        assertEquals("Bytecodes", model.headers.get(i++));
        assertEquals("MethodAll", model.headers.get(i++));
        assertEquals("ROAll", model.headers.get(i++));
        assertEquals("RWAll", model.headers.get(i++));
        assertEquals("Total", model.headers.get(i++));
        assertEquals("ClassName", model.headers.get(i++));
        assertEquals("ClassLoader", model.headers.get(i++));
        assertEquals("systemId", model.headers.get(i++));
        assertEquals("jvmId", model.headers.get(i++));

        i = 0;
        List<String> data = model.data.get("[C");
        assertEquals("1", data.get(i++));
        assertEquals("-1", data.get(i++));
        assertEquals("6126", data.get(i++));
        assertEquals("398176", data.get(i++));
        assertEquals("464", data.get(i++));
        assertEquals("0", data.get(i++));
        assertEquals("0", data.get(i++));
        assertEquals("0", data.get(i++));
        assertEquals("0", data.get(i++));
        assertEquals("0", data.get(i++));
        assertEquals("24", data.get(i++));
        assertEquals("568", data.get(i++));
        assertEquals("592", data.get(i++));
        assertEquals("[C", data.get(i++));
        assertEquals("NULL class_loader", data.get(i++));
        assertEquals("0x4A", data.get(i++));
        assertEquals("42", data.get(i++));
    }
}
