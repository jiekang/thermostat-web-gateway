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

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    /**
     * Gets the shortened package name from a logging class. It's convenient to
     * read when my.class.package.ClassHere is m.c.p.ClassHere, so this takes a
     * list of class names from a log record's getSourceClassName() and gets
     * the shortened name as seen above (without the class at the end). This
     * means in the above example:<br><br>
     * <tt>my.class.package.ClassHere</tt><br><br>
     * causes the following to be returned (assuming it was .split()):<br><br>
     * <tt>m.c.p</tt><br><br>
     * It is up to the caller to put the dot and class after the return value.
     * @param fullLoggingClassNames The array of strings from splitting the
     *                              log record's getSourceClassName(). This
     *                              should not be null.
     * @return A string of the shortened name (without the trailing period and
     * class name). If the length of the array is <= 1, then an empty string is
     * returned.
     */
    private String getShortenedPackageNameFromSourceClass(String[] fullLoggingClassNames) {
        StringBuilder shortenedNameBuilder = new StringBuilder();

        if (fullLoggingClassNames.length > 1) {
            shortenedNameBuilder.append(fullLoggingClassNames[0].charAt(0));
            for (int i = 1; i < fullLoggingClassNames.length - 1; i++) {
                shortenedNameBuilder.append(".");
                shortenedNameBuilder.append(fullLoggingClassNames[i].charAt(0));
            }
        }

        return shortenedNameBuilder.toString();
    }

    @Override
    public synchronized String format(LogRecord record) {
        Throwable thrown = record.getThrown();
        Level level = record.getLevel();
        String[] fullLoggingClassNames = record.getSourceClassName().split("\\.");
        String trimmedPackagePrefix = getShortenedPackageNameFromSourceClass(fullLoggingClassNames);
        String loggingClassName = fullLoggingClassNames[fullLoggingClassNames.length - 1];
        String indent = "  ";

        StringBuilder sb = new StringBuilder();
        sb.append(level.toString());
        sb.append(" - ");
        sb.append(trimmedPackagePrefix);
        sb.append(".");
        sb.append(loggingClassName);
        sb.append(": ");
        sb.append(record.getMessage());
        sb.append("\n");
        while (thrown != null) {
            sb.append(indent);
            sb.append("Caused by:\n");
            sb.append(indent);
            sb.append(thrown.getClass().getCanonicalName());
            sb.append(": ");
            sb.append(thrown.getMessage());
            sb.append("\n");
            for (StackTraceElement stackTraceElement : thrown.getStackTrace()) {
                sb.append(indent);
                sb.append(stackTraceElement.toString());
                sb.append("\n");
            }

            thrown = thrown.getCause();
            indent = indent.concat("  ");
        }

        return sb.toString();
    }
}
