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

package com.redhat.thermostat.service.commands.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.redhat.thermostat.service.commands.channel.WebSocketResponse.ResponseType;

public class ResponseTest {

    @Test
    public void testFromMessage() {
        String msg = "__rid__=1\n\nOK";
        WebSocketResponse expected = new WebSocketResponse(1, ResponseType.OK);
        WebSocketResponse actual = WebSocketResponse.fromMessage(msg);
        assertEquals(expected, actual);
    }

    @Test
    public void testToStringMsg() {
        String expected = "__rid__=3232\n\nAUTH_FAIL";
        WebSocketResponse candidate = new WebSocketResponse(3232, ResponseType.AUTH_FAIL);
        assertEquals(expected, candidate.asStringMesssage());
    }

    @Test
    public void testEquals() {
        WebSocketResponse first = new WebSocketResponse(1, ResponseType.AUTH_FAIL);
        WebSocketResponse second = new WebSocketResponse(1, ResponseType.AUTH_FAIL);
        assertTrue(first.equals(second));
        assertTrue(second.equals(first));
        assertTrue("multiple calls", first.equals(second));

        WebSocketResponse third = new WebSocketResponse(2, ResponseType.AUTH_FAIL);
        assertFalse("sequence numbers don't match", third.equals(first));

        WebSocketResponse fourth = new WebSocketResponse(1, ResponseType.ERROR);
        assertFalse("response type different", fourth.equals(first));

        // null case
        assertFalse(first.equals(null));

        // unrelated object
        assertFalse(first.equals("foobar"));
    }
}
