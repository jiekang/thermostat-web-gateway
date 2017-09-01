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

package com.redhat.thermostat.gateway.tests.util;

import static org.junit.Assert.assertEquals;

import com.redhat.thermostat.gateway.tests.utils.ResponseSetWrapper;
import org.junit.Test;

public class ResponseSetWrapperTest {

    @Test
    public void testSingleKey() {
        ResponseSetWrapper setWrapper = new ResponseSetWrapper().key("k").value(1);
        assertEquals("{\"set\":{\"k\":1}}", setWrapper.toSetString());

        setWrapper = new ResponseSetWrapper().key("k").value("v");
        assertEquals("{\"set\":{\"k\":\"v\"}}", setWrapper.toSetString());

        setWrapper = new ResponseSetWrapper().key("k").valueNoQuotes("v");
        assertEquals("{\"set\":{\"k\":v}}", setWrapper.toSetString());
    }

    @Test
    public void testMultiKey() {
        ResponseSetWrapper setWrapper = new ResponseSetWrapper()
                .key("k1")
                .key("k2")
                .index(42)
                .key("042")
                .key("k3")
                .value("value");
        assertEquals("{\"set\":{\"k1.k2.42.042.k3\":\"value\"}}", setWrapper.toSetString());
    }

    @Test
    public void testNestedObject() {
        ResponseSetWrapper setWrapper = new ResponseSetWrapper()
                .key("hi")
                .valueNoQuotes("{\"a\":1}");
        assertEquals("{\"set\":{\"hi\":{\"a\":1}}}", setWrapper.toSetString());
    }

    @Test
    public void testMultipleOfTheSameKeyAllowed() {
        ResponseSetWrapper setWrapper = new ResponseSetWrapper()
                .key("k").value(1)
                .key("k").value(2);
        assertEquals("{\"set\":{\"k\":1,\"k\":2}}", setWrapper.toSetString());
    }
}
