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

package com.redhat.thermostat.gateway.common.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

abstract class BasicConfiguration implements Configuration {

    private static final String PROPERTIES_PREFIX = "properties|";

    /**
     * Load config into a map. If a key has a special value starting with {@link #PROPERTIES_PREFIX} its
     * value is interpreted to be a properties file next to {@code configFile}. In that case
     * that properties file is loaded in and the value replaced by the properties' contents.
     *
     * @param configFile The config file to read in.
     * @param basePath The parent directory of {@code configFile}.
     * @return The configuration as a map.
     */
    protected static Map<String, Object> loadConfig(String configFile, String basePath) {
        Properties props = new Properties();
        File globalConfig = new File(configFile);
        try (FileInputStream fis = new FileInputStream(globalConfig)) {
            props.load(fis);
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> config = new HashMap<>();
        for (Entry<Object, Object> entry: props.entrySet()) {
            // If a key starts with the special prefix its value is interpreted
            // as a file next to configFile. In that case we read the values in
            // recursively.
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key.startsWith(PROPERTIES_PREFIX) && key.length() > PROPERTIES_PREFIX.length()) {
                String replaceKey = key.substring(PROPERTIES_PREFIX.length());
                String recursiveConfigFile = Paths.get(basePath, value).toFile().getAbsolutePath();
                Map<String, Object> replaceValue = loadConfig(recursiveConfigFile, basePath);
                config.put(replaceKey, replaceValue);
            } else {
                config.put(key, entry.getValue());
            }
        }
        return config;
    }
}
