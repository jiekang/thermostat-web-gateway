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

import javax.servlet.Filter;
import javax.servlet.FilterChain;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This filter takes an init parameter 'version', which represents the implemented version of the API.
 * Incoming requests are checked against that version.
 * If there is no incoming version - then error
 * If there is a version, then it must be compatible (via semantic versioning).
 *
 * The URI is passed on to the servlet without the version number.
 *
 * The web.xml would look like
 *
     <servlet>
        <servlet-name>SystemInfoServlet</servlet-name>
        <servlet-class>org.glassfish.jersey.servlet.ServletContainer</servlet-class>
        ...
    </servlet>

    <filter>
        <filter-name>ServiceVersionFilter</filter-name>
        <filter-class>com.redhat.thermostat.gateway.common.core.servlet.ServiceVersionFilter</filter-class>
        <init-param>
            <param-name>version</param-name>
            <param-value>0.0.1</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>ServiceVersionFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <servlet-mapping>
        <servlet-name>SystemInfoServlet</servlet-name>
        <url-pattern>/*</url-pattern>
    </servlet-mapping>
 */
public class ServiceVersionFilter implements Filter {

    private int[] implVersion;
    private String outVersion;
    private static final Pattern versionRegex = Pattern.compile("^(/[^/]+)/(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|\\d{1,3}\\.\\d{1,3})(/.*)*$");

    @Override
    public void init(FilterConfig config) throws ServletException {
        outVersion = '/' + config.getInitParameter("version");
        implVersion = extractVersion(config.getInitParameter("version"));
    }

    // separated out for testing purposes
    Matcher getMatcher(final String uri) {
        return versionRegex.matcher(uri);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final String requestURI = request.getRequestURI();
        final Matcher matcher = getMatcher(requestURI);

        if (matcher.find()) {

            final String requestedVersionStr = matcher.group(2); // API version requested
            final String group3 = matcher.group(3);
            final String requestedURIPath = group3 != null ? group3 : ""; // rest of URI

            final int[] inVersion;
            try {
                inVersion = extractVersion(requestedVersionStr);
            } catch (NumberFormatException ignored) {
                HttpServletResponse response = (HttpServletResponse)(res);
                response.sendError(404, "API version " + requestedVersionStr + " is invalid");
                return;
            }

            // assumptions:
            // all request URLS start with /apiname/version/path
            // version is an integer, optionally followed with sub version and sub-subversion ('4.8', '5.3', or '0.0.2')

            if (!matchingVersion(inVersion, implVersion)) {
                HttpServletResponse response = (HttpServletResponse)(res);
                response.sendError(404, "API version " + requestedVersionStr + " is not implemented");
                return;
            }

            // The outURL mustn't contain the prefix, even though it's in the input URL
            // The version is verified and consumed in this filter
            final String filteredURI = outVersion + requestedURIPath;
            // avoid loops if filter didn't change anything - this is never the case since the redesign
            if (requestedURIPath.equals(filteredURI)) {
                chain.doFilter(req, res);
            } else {
                req.getRequestDispatcher(filteredURI).forward(req, res);
            }
        } else {
            // if the incoming URL doesn't match the regext patterm, it has no version number.
            HttpServletResponse response = (HttpServletResponse)(res);
            response.sendError(404, "URI is missing API version");
        }
    }

    @Override
    public void destroy() {
        //
    }

    static boolean matchingVersion(final int[] in, final int[] impl) {
        if (in.length > impl.length)
            return false;
        if (in.length < 2)
            return false;
        for (int i = 0; i < in.length; i++) {
            if (i < (in.length - 1)) {
                if (in[i] != impl[i])
                    return false;
            } else {
                if (in[i] > impl[i])
                    return false;
            }
        }
        return true;
    }

    static int[] extractVersion(final String v) {
        final String[] vsa = v.split("[.]");
        final int[] va = new int[vsa.length];
        for (int i = 0; i < vsa.length; i++) {
            va[i] = Integer.parseInt(vsa[i]);
        }
        return va;
    }

    // for testing
    int[] getImplVersion() {
        return implVersion;
    }
}