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

package com.redhat.thermostat.wp.service;

import com.google.gson.JsonSyntaxException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 */
public class JVMParserTest {

    private static final String SOURCE_URL = "resource:/test-data.txt";

    private static final String TEST_DATA = "/test-data.txt";

    private static String DATA;
    private static List<String> EXPECTED = new ArrayList<>();
    private static String INVALID;
    private static String INVALID2;

    private static String JSON_EXPORT;

    @BeforeClass
    public static void init() throws Exception {
        InputStream testData = JVMParserTest.class.getResourceAsStream(TEST_DATA);
        BufferedReader reader = new BufferedReader(new InputStreamReader(testData));
        DATA = reader.readLine();

        // pass through the first "end-of-section"
        reader.readLine();

        String expected;
        while ((expected = reader.readLine()).compareTo("end-of-section") != 0) {
            EXPECTED.add(expected);
        }

        INVALID = reader.readLine();

        // skip next "end-of-section" tag
        reader.readLine();

        INVALID2 = reader.readLine();

        // skip next "end-of-section" tag
        reader.readLine();

        JSON_EXPORT = reader.readLine();

        reader.close();
    }

    @Test
    public void parseValidResponse() throws Exception {
        List<VM> result = new ArrayList<>();

        JVMParser parser = new JVMParser();
        parser.parse(DATA, SOURCE_URL, result);
        assertEquals(EXPECTED.size(), result.size());

        int size = EXPECTED.size();
        for (int i = 0; i < size; i++) {
            assertEquals(EXPECTED.get(i), result.get(i).toString());
        }
    }

    @Test(expected = JsonSyntaxException.class)
    public void parseInvalidResponse() throws Exception {
        List<VM> jvms = new ArrayList<>();

        JVMParser parser = new JVMParser();
        parser.parse(INVALID, SOURCE_URL, jvms);
    }

    @Test
    public void parseEmptyDataResponse() throws Exception {
        List<VM> result = new ArrayList<>();

        JVMParser parser = new JVMParser();
        parser.parse(INVALID2, SOURCE_URL, result);
        assertEquals(0, result.size());
    }

    @Test
    public void toJson() throws Exception {
        List<VM> jvms = new ArrayList<>();

        JVMParser parser = new JVMParser();
        parser.parse(DATA, SOURCE_URL, jvms);

        String result = parser.toJson(jvms);
        assertEquals(JSON_EXPORT, result);
    }
}
