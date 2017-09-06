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

package com.redhat.thermostat.gateway.tests.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for building a response set. It is designed to do method
 * chaining with the set results. For example, if you want to set two of the
 * following:
 * 1) myKey.0.obj: "setFirstElement"
 * 2) myStr: "hi"
 *
 * Then you can do it as follows:
 *     <tt>new ResponseSetWrapper()</tt>
 *     <tt>.key("myKey").index(0).key("obj").value("setFirstElement")</tt>
 *     <tt>.key("myStr").value("hi")</tt>
 *
 * You can then get the string with <tt>.toSetString()</tt>. This would
 * return:
 * <tt>{"set":{"myKey.0.obj":"setFirstElement","myStr":"hi"}}</tt>
 *
 * If you want to nest an object or do something without quotes, then
 * <tt>valueNoQuotes()</tt> will do that.
 *
 * Finally, it should be noted that <tt>.key("1")</tt> and <tt>.index(1)</tt>
 * do the same thing, but it just indicates more clearly that you're aiming to
 * get an index from some array.
 *
 * Note that getting the string does not unset the internal state, so it's
 * possible to get the string and then continue adding more keys.
 *
 * The intent is that after each 'value' call, a key/value pair is deployed as
 * an element that will be written in the final <tt>.toSetString()</tt> call.
 *
 * As a final note: Multiple keys can be entered, so you can do something like:
 *     <tt>wrapper.key("a").value(1).key("a").value(2)</tt>
 * the result will be {"set": {"a": 1, "a", 2}} which we must support since
 * there are unit test cases that require duplicates.
 */
public class ResponseSetWrapper {

    private List<String> keyValues = new ArrayList<>();
    private String currentKey = "";

    private void prefixKeyWithAppropriateCharacter() {
        if (currentKey.isEmpty()) {
            // Since keys are always quoted strings, they must begin with quotes.
            currentKey += "\"";
        } else {
            currentKey += ".";
        }
    }

    public ResponseSetWrapper key(String key) {
        prefixKeyWithAppropriateCharacter();
        currentKey += key;
        return this;
    }

    public ResponseSetWrapper index(int index) {
        prefixKeyWithAppropriateCharacter();
        currentKey += index;
        return this;
    }

    public ResponseSetWrapper value(String str) {
        return value(str, true);
    }

    public ResponseSetWrapper valueNoQuotes(String str) {
        return value(str, false);
    }

    public ResponseSetWrapper value(int i) {
        return value(Integer.toString(i), false);
    }

    private ResponseSetWrapper value(String str, boolean addQuotes) {
        if (currentKey.isEmpty()) {
            throw new IllegalStateException("Cannot finish a value with no key being set");
        }
        keyValues.add(String.format(addQuotes ? "%s\":\"%s\"" : "%s\":%s", currentKey, str));
        currentKey = "";
        return this;
    }

    public String toSetString() {
        if (keyValues.isEmpty()) {
            return "{\"set\":{}}";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{\"set\":{");
        for (int i = 0; i < keyValues.size() - 1; i++) {
            stringBuilder.append(keyValues.get(i));
            stringBuilder.append(",");
        }
        stringBuilder.append(keyValues.get(keyValues.size() - 1));
        stringBuilder.append("}}");
        return stringBuilder.toString();
    }
}
