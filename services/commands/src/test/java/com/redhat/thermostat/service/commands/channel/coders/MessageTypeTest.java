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

package com.redhat.thermostat.service.commands.channel.coders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.redhat.thermostat.service.commands.channel.model.Message.MessageType;

public class MessageTypeTest {

    @Test
    public void canGetMessageTypeFromInt() {
       int[] types = new int[] { 100, 1, 2 };
       MessageType[] expected = new MessageType[] {
               MessageType.RESPONSE,
               MessageType.AGENT_REQUEST,
               MessageType.CLIENT_REQUEST,
       };
       for (int i = 0; i < types.length; i++) {
           MessageType actual = MessageType.fromInt(types[i]);
           assertEquals(expected[i], actual);
       }
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalIntThrowsException() {
        int unknownType = 392;
        MessageType.fromInt(unknownType);
    }
}
