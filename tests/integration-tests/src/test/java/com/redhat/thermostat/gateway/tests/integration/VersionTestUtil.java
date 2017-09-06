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

package com.redhat.thermostat.gateway.tests.integration;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VersionTestUtil {

    public static void testAllVersions(final String url, final String implVersioStr, final String path) throws Exception {

        final int[] implVersion = extractVersion(implVersioStr);

        // must be a string of the form NNN.NNN.NNN
        assertEquals(3, implVersion.length);

        // no vesrion can be greater than 3 digits
        assertTrue(implVersion[0] >= 0 && implVersion[0] < 1000);
        assertTrue(implVersion[1] >= 0 && implVersion[1] < 1000);
        assertTrue(implVersion[2] >= 0 && implVersion[2] < 1000);

        // test with query string
        HttpClient client = IntegrationTest.createAndStartHttpClient();

        List<String> goodVersions = makeGoodVersions(implVersion);
        for (final String v : goodVersions) {
            testGoodVersion(client, url + '/' + v + path);
        }

        // both bad URLs and mismatched versions will give a 404,
        // but the mismatched version will also give a body saying
        // the API versuion is invalid (syntax error) or unimplemented (usually too high)

        List<String> badVersions = makeBadVersions(implVersion);
        for (final String v : badVersions) {
            testInvalidVersion(client, url + '/' + v + path);
        }

        List<String> highVersions = makeHighVersions(implVersion);
        for (final String v : highVersions) {
            testTooHighVersion(client, url + '/' + v + path);
        }

        client.stop();
    }

    private static void testInvalidVersion(final HttpClient client, final String url) throws Exception {
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(response.getStatus(), 404);
    }

    private static void testTooHighVersion(final HttpClient client, final String url) throws Exception {
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(response.getStatus(), 404);
        assertTrue(response.getReason().contains("not implemented"));
    }

    private static void testGoodVersion(final HttpClient client, final String url) throws Exception {
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(response.getStatus(), 200);
    }

    private static List<String> makeBadVersions(final int[] implVersion) {
        final ArrayList<String> list = new ArrayList<>();
        list.add("");
        list.add(".");
        list.add("..");
        list.add("...");
        list.add("0");
        list.add("000" + implVersion[0] + "." + implVersion[1] + "." + implVersion[2]);
        list.add("000" + implVersion[0] + ".000" + implVersion[1] + "." + implVersion[2]);
        list.add("000" + implVersion[0] + "." + implVersion[1] + ".000" + implVersion[2]);
        list.add("000" + implVersion[0] + ".000" + implVersion[1] + "000." + implVersion[2]);
        list.add("000" + implVersion[0] + ".");
        list.add("000" + implVersion[0] + ".000");
        return list;
    }

    private static List<String> makeHighVersions(final int[] implVersion) {
        final ArrayList<String> list = new ArrayList<>();
        list.add("" + implVersion[0] + "." + implVersion[1] + "." + (implVersion[2] + 1));
        list.add("" + implVersion[0] + "." + (implVersion[1] + 1));
        list.add("" + (implVersion[0] + 1) + "." + implVersion[1]);
        list.add("0.0." + (implVersion[2] + 1));
        return list;
    }

    private static List<String> makeGoodVersions(final int[] implVersion) {
        final ArrayList<String> list = new ArrayList<>();
        list.add("" + implVersion[0] + "." + implVersion[1] + "." + implVersion[2]);
        if (implVersion[2] > 0) {
            list.add("" + implVersion[0] + "." + implVersion[1] + "." + (implVersion[2] - 1));
        }
        list.add("" + implVersion[0] + "." + implVersion[1] + ".0");
        list.add("" + implVersion[0] + "." + implVersion[1] + ".00");
        list.add("" + implVersion[0] + "." + implVersion[1] + ".000");
        list.add("" + implVersion[0] + "." + implVersion[1]);
        return list;
    }

    private static int[] extractVersion(final String v) {
        final String[] vsa = v.split("[.]");
        final int[] va = new int[vsa.length];
        for (int i = 0; i < vsa.length; i++) {
            va[i] = Integer.parseInt(vsa[i]);
        }
        return va;
    }
}
