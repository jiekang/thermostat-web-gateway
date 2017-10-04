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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * A wrapper for content that the server responds with. The primary use of this
 * class is to be able to easily construct a response/metadata object, and to
 * be able to compare json strings with one another regardless of map ordering.
 * This means content like {"response":[{a: b, c: d}]} will be considered a
 * match to {"response":[{c: d, a: b}]}.
 */
public class ContentWrapper {

    private static final Gson gson = new Gson();

    private LinkedHashMap<String, Object> elements = new LinkedHashMap<>();

    /**
     * Creates an empty content wrapper with only an empty response array
     * present.
     */
    public ContentWrapper() {
        elements.put("response", new ArrayList<JsonElement>());
    }

    /**
     * Adds a list of objects in the order provided to the response array.
     * Order of the elements matters since these are inserted into the array.
     * You do not need to provide the wrapping array yourself.
     * @param objects The elements to add to the response array list.
     * @return The current object.
     */
    @SuppressWarnings("unchecked")
    public ContentWrapper addToResponse(Object ...objects) {
        ArrayList<JsonElement> response = (ArrayList<JsonElement>) elements.get("response");
        response.addAll(gson.fromJson("{\"elementList\":" + gson.toJson(objects) + "}", JsonElementList.class).elementList);
        return this;
    }

    /**
     * Adds the objects to the metadata internal map. You do not need to pass
     * in a Map as it will do the wrapping for you. If you want to have a map
     * like {metadata:{a: b}} then you would just call addToMetadata("a", "b").
     * @param objects The objects to add to the metadata map.
     * @return The current object.
     */
    @SuppressWarnings("unchecked")
    public ContentWrapper addToMetadata(Object ...objects) {
        if (!elements.containsKey("metaData")) {
            elements.put("metaData", new HashMap<String, JsonElement>());
        }
        HashMap<String, JsonElement> metaData = (HashMap<String, JsonElement>) elements.get("metaData");
        HashMap map = Map.of(objects);
        metaData.putAll(gson.fromJson("{\"elementMap\":" + gson.toJson(map) + "}", JsonElementMap.class).elementMap);
        return this;
    }

    /**
     * Checks if a string response matches the elements in this object. This
     * makes sure they match even if the mapping order is not the same. Thus,
     * the following would return true if our response builder contained the
     * following:
     *      [{"a": "b", "c": "d"}]
     * and it was tested with:
     *      "{\"response\":[{\"a\":\"b\",\"c\":\"d\"}]}
     * or
     *      "{\"response\":[{\"c\":\"d\",\"a\":\"b\"}]}
     * Note that array ordering is required, so {response:[[1,2]]} is not the
     * same as {response:[[2,1]]}. This goes for any nested array in the
     * response to be checked.
     * @param jsonString The string to check.
     * @throws AssertionError If the match fails. This is the same as what you
     * get when JUnit throws.
     */
    public void matchJsonOrThrow(String jsonString) {
        ContentWrapper other = gson.fromJson("{\"elements\":" + jsonString + "}", ContentWrapper.class);
        String thisString = gson.toJson(this);
        String otherString = gson.toJson(other);

        JsonParser parser = new JsonParser();
        JsonElement expectedElements = parser.parse(thisString);
        JsonElement actualElements = parser.parse(otherString);
        assertEquals(expectedElements, actualElements);
    }

    /**
     * Gets the element in Json form. The "response" field is always returned
     * even if it's empty, but other ones (ex: metadata) will not be added to
     * the list if there are no elements.
     * @return A string in the json format.
     */
    public String toJson() {
        return gson.toJson(elements);
    }

    /**
     * Returns the array for the "response" element in this wrapper.
     * @return A json-like string of the data, for example: {"response":[1,"hi"]}
     * would return [1,"hi"].
     * @throws IllegalStateException If there is no loaded data for the response
     * in this element.
     */
    public String getJsonResponseArray() {
        return gson.toJson(elements.get("response"));
    }

    // The following two classes mimic what Java 9 would have added, but with a
    // slight twist on what Map.of() does by accepting a variable args amount.
    public static class Array {
        public static List<Object> of(Object ...values) {
            return Arrays.asList(values);
        }
    }

    public static class Map {
        public static HashMap of(Object ...values) {
            HashMap<Object, Object> map = new HashMap<>();
            if (values != null) {
                if (values.length % 2 != 0) {
                    throw new IllegalStateException("Got odd number of elements, a key has no value");
                }
                for (int i = 0; i < values.length; i += 2) {
                    map.put(values[i], values[i + 1]);
                }
            }
            return map;
        }

        public static HashMap ofNumberLong(long longValue) {
            return Map.of("$numberLong", Long.toString(longValue));
        }
    }

    // A small class designed to get Gson to write a list as a list of
    // 'JsonElement's so we can do recursive comparisons easily.
    private class JsonElementList {
        private List<JsonElement> elementList;
    }

    private class JsonElementMap {
        private java.util.Map<String, JsonElement> elementMap;
    }
}
