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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * Sets the gateway home servlet context attribute variable by:
 * <ol>
 *  <li>Looking at the init parameter com.redhat.thermostat.gateway.HOME
 *      from web.xml</li>
 *  <li>Looking at the environment variable THERMOSTAT_GATEWAY_HOME</li>
 * </ol>
 *
 * in that order. The latter overrides the former. When using this listener
 * then users can be assured that {@link GlobalConstants#GATEWAY_HOME_KEY} will
 * be set as a servlet context attribute.
 */
public class GatewayHomeSettingContextListener implements ServletContextListener {

    private final EnvHelper envHelper;

    public GatewayHomeSettingContextListener() {
        this(new EnvHelper());
    }

    GatewayHomeSettingContextListener(EnvHelper helper) {
        this.envHelper = helper;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        String envGwHome = envHelper.getEnv(GlobalConstants.GATEWAY_HOME_ENV);
        synchronized(ctx) {
            String webXmlGwHome = ctx.getInitParameter(GlobalConstants.GATEWAY_HOME_KEY);
            String gwHome;
            if (envGwHome != null) {
                gwHome = envGwHome;
            } else if (webXmlGwHome != null) {
                gwHome = webXmlGwHome;
            } else {
                String msg = "Gateway home not defined. " +
                             "Neither via THERMOSTAT_GATEWAY_HOME env var nor via web.xml";
                throw new IllegalStateException(msg);
            }
            ctx.setAttribute(GlobalConstants.GATEWAY_HOME_KEY, gwHome);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        synchronized (ctx) {
            ctx.setAttribute(GlobalConstants.GATEWAY_HOME_KEY, null);
        }
    }

    static class EnvHelper {

        String getEnv(String variable) {
            return System.getenv(variable);
        }

    }

}
