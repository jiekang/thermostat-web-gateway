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

import static org.junit.Assert.assertEquals;

public class JSONConverterTest {
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

    private static final String EXPECTED = "[{\"Index\":{\"$numberLong\":\"3\"},\"Super\":{\"$numberLong\":\"-1\"},\"InstCount\":{\"$numberLong\":\"471\"},\"InstBytes\":{\"$numberLong\":\"154720\"},\"KlassBytes\":{\"$numberLong\":\"464\"},\"annotations\":{\"$numberLong\":\"0\"},\"CpAll\":{\"$numberLong\":\"0\"},\"MethodCount\":{\"$numberLong\":\"0\"},\"Bytecodes\":{\"$numberLong\":\"0\"},\"MethodAll\":{\"$numberLong\":\"0\"},\"ROAll\":{\"$numberLong\":\"24\"},\"RWAll\":{\"$numberLong\":\"568\"},\"Total\":{\"$numberLong\":\"592\"},\"ClassName\":\"[B\",\"ClassLoader\":\"NULL class_loader\",\"systemId\":\"0x4A\",\"jvmId\":{\"$numberLong\":\"42\"},\"timeStamp\":{\"$numberLong\":\"1505992968442\"}},{\"Index\":{\"$numberLong\":\"1\"},\"Super\":{\"$numberLong\":\"-1\"},\"InstCount\":{\"$numberLong\":\"6126\"},\"InstBytes\":{\"$numberLong\":\"398176\"},\"KlassBytes\":{\"$numberLong\":\"464\"},\"annotations\":{\"$numberLong\":\"0\"},\"CpAll\":{\"$numberLong\":\"0\"},\"MethodCount\":{\"$numberLong\":\"0\"},\"Bytecodes\":{\"$numberLong\":\"0\"},\"MethodAll\":{\"$numberLong\":\"0\"},\"ROAll\":{\"$numberLong\":\"24\"},\"RWAll\":{\"$numberLong\":\"568\"},\"Total\":{\"$numberLong\":\"592\"},\"ClassName\":\"[C\",\"ClassLoader\":\"NULL class_loader\",\"systemId\":\"0x4A\",\"jvmId\":{\"$numberLong\":\"42\"},\"timeStamp\":{\"$numberLong\":\"1505992968442\"}},{\"Index\":{\"$numberLong\":\"2\"},\"Super\":{\"$numberLong\":\"48\"},\"InstCount\":{\"$numberLong\":\"1614\"},\"InstBytes\":{\"$numberLong\":\"182816\"},\"KlassBytes\":{\"$numberLong\":\"632\"},\"annotations\":{\"$numberLong\":\"0\"},\"CpAll\":{\"$numberLong\":\"20824\"},\"MethodCount\":{\"$numberLong\":\"130\"},\"Bytecodes\":{\"$numberLong\":\"4973\"},\"MethodAll\":{\"$numberLong\":\"37080\"},\"ROAll\":{\"$numberLong\":\"20768\"},\"RWAll\":{\"$numberLong\":\"39544\"},\"Total\":{\"$numberLong\":\"60312\"},\"ClassName\":\"java.lang.Class\",\"ClassLoader\":\"NULL class_loader\",\"systemId\":\"0x4A\",\"jvmId\":{\"$numberLong\":\"42\"},\"timeStamp\":{\"$numberLong\":\"1505992968442\"}}]";

    private static Model MODEL = JCMDModelConverter.createModel(RAW_PAYLOAD, "0x4A", "42");

    @Test
    public void add() throws Exception {
        JSONConverter handler = new JSONConverter();
        String json = handler.convert(MODEL);
        assertEquals(EXPECTED, json);
    }
}
