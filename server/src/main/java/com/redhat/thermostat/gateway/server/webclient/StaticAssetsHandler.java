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

package com.redhat.thermostat.gateway.server.webclient;

import java.io.File;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;

public class StaticAssetsHandler {

    private static final String WEB_CLIENT_ASSETS_FOLDER = "web-client";
    private static final String WEB_CLIENT_CONTEXT_PATH = "/" + WEB_CLIENT_ASSETS_FOLDER;

    private final EnvHelper envHelper;

    public StaticAssetsHandler() {
        this(new EnvHelper());
    }

    // package-private for test-overrides
    StaticAssetsHandler(EnvHelper helper) {
        this.envHelper = helper;
    }

    public ContextHandler create() {
        ContextHandler apiContext = new ContextHandler();
        apiContext.setContextPath(WEB_CLIENT_CONTEXT_PATH);
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        File webClientDir = getWebClientAssetsDir();
        resourceHandler.setResourceBase(webClientDir.getAbsolutePath());
        apiContext.setHandler(resourceHandler);
        return apiContext;
    }

    private File getWebClientAssetsDir() {
        String gatewayHome = envHelper.getEnv(GlobalConstants.GATEWAY_HOME_ENV);
        if (gatewayHome == null) {
            throw new RuntimeException("THERMOSTAT_GATEWAY_HOME environment variable not set");
        }
        return new File(gatewayHome, WEB_CLIENT_ASSETS_FOLDER);
    }

    public static class EnvHelper {
        public String getEnv(String var) {
            return System.getenv(var);
        }
    }
}
