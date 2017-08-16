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

package com.redhat.thermostat.gateway.common.core.servlet;

import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceVersionFilterTest {

    @Test
    public void testValidFilterInit() throws ServletException {

        final ServiceVersionFilter filter = createFilter("11.22.33");

        final int[] va = filter.getImplVersion();

        assertNotNull(va);
        assertEquals(3, va.length);

        assertEquals(11, va[0]);
        assertEquals(22, va[1]);
        assertEquals(33, va[2]);
    }

    private void testExtract(final String in, final int o1, final int o2, final int o3) {
        final int[] oa = ServiceVersionFilter.extractVersion(in);
        assertEquals(3, oa.length);
        assertEquals(o1, oa[0]);
        assertEquals(o2, oa[1]);
        assertEquals(o3, oa[2]);
    }

    @Test
    public void testExtractor() {
        testExtract("00.0.0", 0, 0, 0);
        testExtract("11.22.33", 11, 22, 33);

        final int[] oa2 = ServiceVersionFilter.extractVersion("9.4");
        assertEquals(2, oa2.length);
        assertEquals(9, oa2[0]);
        assertEquals(4, oa2[1]);

        final int[] oa1 = ServiceVersionFilter.extractVersion("2345");
        assertEquals(1, oa1.length);
        assertEquals(2345, oa1[0]);
    }

    @Test
    public void testBadExtract() {
        try {
            ServiceVersionFilter.extractVersion("junk");
            fail("should have caught the syntax error");
        } catch (NumberFormatException ignored) {
        }
    }

    @Test
    public void testValidNumericMatches() throws ServletException {
        assertNumericMatch("0.0.5", "0.0.5");
        assertNumericMatch("0.0.5", "0.0.4");
        assertNumericMatch("0.0.5", "0.0.0");
        assertNumericMatch("0.0.5", "0.0");
        assertNumericMatch("6.3.5", "6.3.5");
        assertNumericMatch("6.3.5", "6.3.4");
        assertNumericMatch("6.3.005", "6.003.4");
        assertNumericMatch("6.3.5", "6.3.0");
        assertNumericMatch("6.3.5", "6.3");
    }

    @Test
    public void testInvalidNumericMatches() throws ServletException {
        assertNumericMismatch("0.0.5", "0.0.6");
        assertNumericMismatch("0.0.5", "0.0.100");
        assertNumericMismatch("0.0.5", "0.0.50");
        assertNumericMismatch("0.0.5", "0.1");
        assertNumericMismatch("6.3.5", "6.3.6");
        assertNumericMismatch("6.3.5", "6.4.4");
        assertNumericMismatch("6.3.5", "7.0.0");
        assertNumericMismatch("6.3.5", "6.010");
    }

    @Test
    public void testRegExNoEmpty() throws ServletException {

        // these test that at least two numbers are specified
        assertRegExMismatch("6.3.");
        assertRegExMismatch("6.");
        assertRegExMismatch("0.0.");
        assertRegExMismatch("0.");
        assertRegExMismatch("..0");
        assertRegExMismatch("0..0");
        assertRegExMismatch(".0.0");
        assertRegExMismatch("0.1.");
        assertRegExMismatch("1.");
        assertRegExMismatch("1.1.");
        assertRegExMismatch("6.4.");
        assertRegExMismatch("6..0");
        assertRegExMismatch("010.");
        assertRegExMismatch(".");
        assertRegExMismatch("..");
        assertRegExMismatch("...");
        assertRegExMismatch(".");
    }

    @Test
    public void testRegEx1To3Digits() throws ServletException {
        assertRegExMismatch("0000.0.5");
        assertRegExMismatch("0.0000.4");
        assertRegExMismatch("0.0.0000");
        assertRegExMismatch("0000.0000");
        assertRegExMismatch("6.3.0005");
        assertRegExMismatch("6.0003.4");
        assertRegExMismatch("0006.003.4");
        assertRegExMismatch("6.00000000003");
    }

    @Test
    public void testAtLeast2Matches() throws ServletException {
        // these test that at least two numbers are specified
        assertRegExMismatch("6");
        assertRegExMismatch("0");
        assertRegExMismatch("1");
        assertRegExMismatch("1.");
        assertRegExMismatch("7");
    }

    @Test
    public void testValid() throws ServletException, IOException {
        ensureValidRewrite("3.4.5", "/api/3.4", "/bar?q=44.55.66");
        ensureValidRewrite("3.4.5", "/api/3.4.2", "/foo");
        ensureValidRewrite("3.4.5", "/api/3.4.2", "");
        ensureValidRewrite("3.4.5", "/api/3.4", "");


        ensureValidNoRewrite("3.5.5", "/api/3.5.5", "");
        ensureValidNoRewrite("3.7.5", "/api/3.7.5", "/");
        ensureValidNoRewrite("3.7.5", "/api/3.7.5", "/path");
        ensureValidNoRewrite("3.7.5", "/api/3.7.5", "/newpath?query=/bar/5.6.7");
    }

    @Test
    public void testInvalid() throws ServletException, IOException {
        ensureInvalid("3.5.5", "/api/3");
        ensureInvalid("3.4.5", "/api/3/");

        ensureInvalid("3.4.5", "/");
        ensureInvalid("3.4.5", "/fred");

        ensureInvalid("3.4.5", "/api/3.4.6");
        ensureInvalid("3.4.5", "/api/3.5");
        ensureInvalid("3.6.5", "/api/4");
        ensureInvalid("3.4.5", "/api/4.4.2/foo");
        ensureInvalid("3.4.5", "/api/3.5/bar?q=44.55.66");
        ensureInvalid("3.4.5", "/api/030/");
    }


    private void assertNumericMatch(final String impl, final String req) throws ServletException {
        final int[] implV = ServiceVersionFilter.extractVersion(impl);
        final int[] reqV = ServiceVersionFilter.extractVersion(req);
        assertTrue(ServiceVersionFilter.matchingVersion(reqV, implV));
        assertRegExMatch(req);
    }

    private void assertNumericMismatch(final String impl, final String req) throws ServletException {
        final int[] implV = ServiceVersionFilter.extractVersion(impl);
        final int[] reqV = ServiceVersionFilter.extractVersion(req);
        assertFalse(ServiceVersionFilter.matchingVersion(reqV, implV));
    }

    private void assertRegExMatch(final String req) throws ServletException {
        final ServiceVersionFilter filter = createFilter("0.0.0");
        final Matcher matcher = filter.getMatcher("/thing/" + req);
        assertTrue(matcher.find());
    }

    private void assertRegExMismatch(final String req) throws ServletException {
        final ServiceVersionFilter filter = createFilter("0.0.0");
        final Matcher matcher = filter.getMatcher("/thing/" + req);
        assertFalse(matcher.find());
    }

    // passes if the version number was changed
    private void ensureValidRewrite(final String implVersion, final String uri, final String path) throws ServletException, IOException {
        final ServiceVersionFilter filter = createFilter(implVersion);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(uri + path);
        final RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(anyString())).thenReturn(rd);
        final HttpServletResponse resp = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        filter.doFilter(req, resp, chain);
        verify(req).getRequestDispatcher("/" + implVersion + path);
        verify(rd).forward(any(ServletRequest.class), any(ServletResponse.class));
    }

    // passes if the version number was valid and unchanged.
    // currently we always rewrite; so these pass even if the URL was rewritten
    private void ensureValidNoRewrite(final String implVersion, final String uri, final String path) throws ServletException, IOException {
        ensureValidRewrite(implVersion, uri, path);
    }

    // passes if the version number was invalid
    private void ensureInvalid(final String implVersion, final String uri) throws ServletException, IOException {
        final ServiceVersionFilter filter = createFilter(implVersion);
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(uri);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(req, resp, chain);
        verify(resp).sendError(eq(404), anyString());
    }

    private ServiceVersionFilter createFilter(final String impl) throws ServletException {
        final FilterConfig cfg = mock(FilterConfig.class);
        when(cfg.getInitParameter("version")).thenReturn(impl);
        final ServiceVersionFilter filter = new ServiceVersionFilter();
        filter.init(cfg);
        return filter;
    }
}
