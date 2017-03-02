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

package com.redhat.thermostat.server.core.internal.storage.mongo.response;

public class DocumentBuilder {

    private final StringBuilder contentBuilder;

    public DocumentBuilder(String content) {
        contentBuilder = new StringBuilder();
        contentBuilder.append("{ \"obj\" : ");
        contentBuilder.append(content);
        contentBuilder.append("}");
    }

    /**
     * Adds tags to the JSON content
     * {/[ ... }/] -> {/[ ... ,tags:["admin",...]}/]
     * @param tags the tags to add
     * @return the JSON string with tags attached
     */
    public DocumentBuilder addTags(String... tags) {
        StringBuilder tagBuilder = new StringBuilder();
        tagBuilder.append(",\"tags\":[\"admin\"");
        for (String tag : tags) {
            tagBuilder.append(",\"").append(tag).append("\"");
        }
        tagBuilder.append("]");

        contentBuilder.insert(contentBuilder.length() - 1, tagBuilder.toString());
        return this;
    }

    /**
     * Add key-value pair to the JSON content
     * {/[ ... }/] -> {/[ ... ,"key:"admin"}/]
     *
     * @param key the key
     * @param value the value
     * @return the JSON string with key-value pair attached
     */
    public DocumentBuilder addId(String key, String value) {
        if (key != null && value != null) {
            String kvPair = ",\"" + key + "\":\"" + value + "\"";
            contentBuilder.insert(contentBuilder.length() - 1, kvPair);
        }
        return this;
    }

    public String build() {
        return contentBuilder.toString();
    }
}
