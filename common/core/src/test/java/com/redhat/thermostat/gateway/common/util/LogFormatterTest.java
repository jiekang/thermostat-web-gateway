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

package com.redhat.thermostat.gateway.common.util;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;

public class LogFormatterTest {

    private static String logMessage = "a message";
    private static Level logLevel = Level.INFO;

    @Test
    public void testLogMessage() {
        LogRecord logRecord = mock(LogRecord.class);
        when(logRecord.getThrown()).thenReturn(null);
        when(logRecord.getLevel()).thenReturn(logLevel);
        when(logRecord.getSourceClassName()).thenReturn("some.random.class.TestClass");
        when(logRecord.getMessage()).thenReturn(logMessage);

        String expected = logLevel.toString() + " - s.r.c.TestClass: " + logMessage + "\n";
        String output = new LogFormatter().format(logRecord);
        assertEquals(expected, output);
    }

    @Test
    public void testLogMessageException() {
        String exceptionMsg = "exception message";
        Exception exception = new Exception(exceptionMsg);

        LogRecord logRecord = mock(LogRecord.class);
        when(logRecord.getThrown()).thenReturn(null);
        when(logRecord.getLevel()).thenReturn(logLevel);
        when(logRecord.getSourceClassName()).thenReturn("some.random.class.TestClass");
        when(logRecord.getMessage()).thenReturn(logMessage);
        when(logRecord.getThrown()).thenReturn(exception);

        String headerMsg = logLevel.toString() + " - s.r.c.TestClass: " + logMessage + "\n" +
                           "  Caused by:\n" +
                           "  java.lang.Exception: " + exceptionMsg;
        String output = new LogFormatter().format(logRecord);
        assertTrue(output.startsWith(headerMsg));
    }
}
