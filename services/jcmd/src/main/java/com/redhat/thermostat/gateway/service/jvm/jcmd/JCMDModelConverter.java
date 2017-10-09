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

package com.redhat.thermostat.gateway.service.jvm.jcmd;

import com.google.gson.Gson;
import com.redhat.thermostat.lang.schema.models.Timestamp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class JCMDModelConverter {

    // TODO: we need to export this model from the agent somehow, models
    // this is basically a copy of
    // com.redhat.thermostat.jcmd.backend.internal.model.GCStatsModel
    private static class RawModel {
        private Timestamp timestamp;
        private String payload;
    }

    public static Model createModel(String json, String systemId, String jvmId) {

        Model model = new Model();
        model.jvmId = jvmId;
        model.systemId = systemId;

        RawModel rawModel = new Gson().fromJson(json, RawModel.class);
        model.timestamp = rawModel.timestamp.get();

        BufferedReader reader = new BufferedReader(new StringReader(rawModel.payload));
        try {
            List<String> headers = new ArrayList<>(Arrays.asList(reader.readLine().split("\\s+|,")));
            headers.add("systemId");
            headers.add("jvmId");

            model.headers = headers;

            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith("Total") || line.startsWith(headers.get(0)) ||
                    line.contains("%"))
                {
                    // we reached end, the last line is the same as the first
                    // one, the last two lines are some simple
                    // stats we can infer from the other numbers
                    break;
                }
                // we need to take care for the last block, since it may contain
                // a legitimate whitespace
                // note: we depend on the fact that the last block is *always*
                // ClassName,ClassLoader, while this should not change across
                // releases, if it does we need a smarter parsing code
                // it may be worth to revisit this code anyway to make it
                // less dependent on the input
                String[] blocks = line.trim().split(",");
                String classLoaderField = blocks[1];
                List<String> classData = new ArrayList<>(Arrays.asList(blocks[0].split("\\s+")));
                String className = classData.get(classData.size() - 1);

                classData.add(classLoaderField);

                classData.add(systemId);
                classData.add(jvmId);

                model.data.put(className, classData);
            }

        } catch (IOException ignored) {
            ignored.printStackTrace();
        }

        return model;
    }
}
