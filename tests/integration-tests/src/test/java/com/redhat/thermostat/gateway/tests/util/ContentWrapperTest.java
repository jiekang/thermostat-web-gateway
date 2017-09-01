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

package com.redhat.thermostat.gateway.tests.util;

import static com.redhat.thermostat.gateway.tests.utils.ContentWrapper.Array;
import static com.redhat.thermostat.gateway.tests.utils.ContentWrapper.Map;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;

import com.redhat.thermostat.gateway.tests.utils.ContentWrapper;
import com.redhat.thermostat.gateway.tests.utils.HttpTestUtil;
import org.junit.Test;

public class ContentWrapperTest {

    @Test
    public void testEmptyResponseBuilder() {
        assertEquals("{\"response\":[]}", HttpTestUtil.EMPTY_RESPONSE);
    }

    @Test
    public void testResponseBuilderData() {
        ContentWrapper contentWrapper = new ContentWrapper().addToResponse(1, 3, "hi", Map.of("a", "b"));
        assertEquals("[1,3,\"hi\",{\"a\":\"b\"}]", contentWrapper.getJsonResponseArray());
    }

    @Test
    public void testSimpleResponseBuilder() {
        ContentWrapper contentWrapper = new ContentWrapper().addToResponse(
                Map.of("answer", 42,
                       "a", "b"),
                Array.of(1, 2, 3)
        );

        assertThat(contentWrapper.toJson(), anyOf(
                equalTo("{\"response\":[{\"answer\":42,\"a\":\"b\"},[1,2,3]]}"),
                equalTo("{\"response\":[{\"a\":\"b\",\"answer\":42},[1,2,3]]}")));

        String expectedResponse = "{\"response\":[{\"a\":\"b\",\"answer\":42},[1,2,3]]}";
        contentWrapper.matchJsonOrThrow(expectedResponse);
    }

    @Test
    public void testShouldMatchItself() {
        ContentWrapper contentWrapper = new ContentWrapper().addToResponse(Map.of("answer", 42), Array.of(1, 2, "hello"), "yes");
        contentWrapper.matchJsonOrThrow(contentWrapper.toJson());
    }

    @Test
    public void testMapOrderDoesNotMatter() {
        ContentWrapper contentWrapper = new ContentWrapper().addToResponse(Map.of("a", 1, "b", 2));
        String permutationOne = "{\"response\":[{\"a\":1,\"b\":2}]}";
        String permutationTwo = "{\"response\":[{\"b\":2,\"a\":1}]}";
        contentWrapper.matchJsonOrThrow(permutationOne);
        contentWrapper.matchJsonOrThrow(permutationTwo);
    }

    @Test
    public void testLargeAmountOfObjects() {
        ContentWrapper contentWrapper = new ContentWrapper().addToResponse(
                Map.of("answer", 42,
                       "key", Map.of("something", new TestPOJO(0, -12345678, "hi"),
                                     1, new LinkedList<TestPOJO>())),
                Array.of(1,
                         Array.of('a', 'b', "c"),
                         "hello",
                         Array.of()),
                new TestPOJO(42, -2, "hi"),
                1,
                new ArrayList<Integer>(),
                ""
        );

        String expected = "{\"response\":[{" +
                              "\"answer\":42," +
                              "\"key\":{" +
                                  "\"1\":[]," +
                                  "\"something\":{" +
                                      "\"first\":0," +
                                      "\"second\":-12345678," +
                                      "\"third\":\"hi\"}" +
                                  "}" +
                              "}," +
                              "[1,[\"a\",\"b\",\"c\"],\"hello\",[]]," +
                              "{" +
                                  "\"first\":42," +
                                  "\"second\":-2," +
                                  "\"third\":\"hi\"}," +
                              "1," +
                              "[]," +
                              "\"\"" +
                          "]}";

        assertEquals(expected, contentWrapper.toJson());
    }

    @Test
    public void testCanAddNewElementsToTheBuilder() {
        ContentWrapper contentWrapper = new ContentWrapper().addToResponse(Map.of("answer", 42));
        assertEquals("{\"response\":[{\"answer\":42}]}", contentWrapper.toJson());

        contentWrapper.addToResponse(Array.of("yes", 0));
        assertEquals("{\"response\":[{\"answer\":42},[\"yes\",0]]}", contentWrapper.toJson());
    }

    @Test
    public void testAddingMetadata() {
        ContentWrapper contentWrapper = new ContentWrapper().addToMetadata("answer", 42, 1, 2);
        assertThat(contentWrapper.toJson(), anyOf(
                equalTo("{\"response\":[],\"metaData\":{\"answer\":42,\"1\":2}}"),
                equalTo("{\"response\":[],\"metaData\":{\"1\":2,\"answer\":42}}")));
    }

    @Test
    public void testAddingBothResponseAndMetadata() {
        ContentWrapper contentWrapper = new ContentWrapper()
                .addToResponse("a", 1)
                .addToMetadata("answer", 42, "array", Array.of(1, 2, 3));
        assertThat(contentWrapper.toJson(), anyOf(
                equalTo("{\"response\":[\"a\",1],\"metaData\":{\"answer\":42,\"array\":[1,2,3]}}"),
                equalTo("{\"response\":[\"a\",1],\"metaData\":{\"array\":[1,2,3],\"answer\":42}}")));

    }

    // Used to make sure objects are converted properly.
    private class TestPOJO {
        private int first;
        private int second;
        private String third;

        private TestPOJO(int first, int second, String third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }
}
