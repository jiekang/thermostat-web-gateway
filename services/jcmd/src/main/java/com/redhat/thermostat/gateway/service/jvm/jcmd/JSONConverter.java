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

import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONConverter {

    class JCMDEntry {
        long timestamp;
        String key;
        String value;
    }

    private GsonBuilder builder;
    JSONConverter() {
        builder = new GsonBuilder();
        // leave commented out for debugging
        //builder.setPrettyPrinting();
        builder.registerTypeAdapter(HashMap.class, new ModelAdapter());
    }

    public String convert(Model model) {
        Map<String, List<JCMDEntry>> jsonMapping = new HashMap<>();
        for (String key : model.data.keySet()) {
            List<String> objectData = model.data.get(key);
            int i = 0;

            List<JCMDEntry> entries = new ArrayList<>();
            for (String fieldEntry : objectData) {
                JCMDEntry jcmdEntry = new JCMDEntry();
                jcmdEntry.key = model.headers.get(i++);
                jcmdEntry.value = fieldEntry;

                jcmdEntry.timestamp = model.timestamp;

                entries.add(jcmdEntry);
            }
            jsonMapping.put(key, entries);
        }

        return builder.create().toJson(jsonMapping);
    }

    private class ModelAdapter extends TypeAdapter<Object> {

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            Map<String, List<JCMDEntry>> jsonObject  = (Map<String, List<JCMDEntry>>) value;

            out.beginArray();

            for (String name : jsonObject.keySet()) {
                out.beginObject();

                for (JCMDEntry property : jsonObject.get(name)) {

                    out.name(property.key);

                    if (property.value.matches("-?\\d+(\\.\\d+)?")) {
                        out.beginObject();
                        out.name("$numberLong");
                        out.value(property.value);
                        out.endObject();
                    } else {
                        out.value(property.value);
                    }
                }

                // the timestamp is not part of the properties so we add
                // manually here
                {
                    out.name("timeStamp");
                    out.beginObject();
                    out.name("$numberLong");
                    out.value("" + jsonObject.get(name).get(0).timestamp);
                    out.endObject();
                }
                // end: timeStamp

                out.endObject();
            }

            out.endArray();
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            throw new UnsupportedOperationException("read() not yet implemented");
        }
    }
}
