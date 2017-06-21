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

package com.redhat.thermostat.gateway.common.mongodb.response;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class MongoMetaDataResponseBuilderTest {

    private MongoMetaDataResponseBuilder.MetaBuilder mongoMetaDataResponseBuilder;
    private final String fakeUrl = "http://127.0.0.1:8080/fake/endpoint";

    @Before
    public void setup() {
        mongoMetaDataResponseBuilder = new MongoMetaDataResponseBuilder.MetaBuilder();
    }

    @Test
    public void testBuildGetSomeParams() {
        mongoMetaDataResponseBuilder.payloadCount(5).count(10).next(fakeUrl);
        String output = mongoMetaDataResponseBuilder.build().toString();
        String expected = "{\"payloadCount\":5,\"count\":10,\"next\":\"http://127.0.0.1:8080/fake/endpoint\"}";
        assertEquals(expected, output);
    }

    @Test
    public void buildGetAllParams() {
        mongoMetaDataResponseBuilder
                .payloadCount(5)
                .count(10)
                .next(fakeUrl + "/5")
                .prev(fakeUrl + "/0")
                .elapsed(5000)
                .first(fakeUrl + "/0")
                .last(fakeUrl + "/10")
                .matchCount(10)
                .insertCount(0);

        String output = mongoMetaDataResponseBuilder.build().toString();
        String expected = "{\"payloadCount\":5,\"count\":10,\"prev\":\"http://127.0.0.1:8080/fake/endpoint/0\"," +
                "\"next\":\"http://127.0.0.1:8080/fake/endpoint/5\",\"first\":\"http://127.0.0.1:8080/fake/endpoint/0\"" +
                ",\"last\":\"http://127.0.0.1:8080/fake/endpoint/10\",\"insertCount\":0,\"matchCount\":10," +
                "\"elapsed\":5000}";
        assertEquals(expected, output);
    }

    @Test
    public void testInitializeWithNullValue() {
        mongoMetaDataResponseBuilder
                .payloadCount(null)
                .count(null)
                .next(null)
                .prev(null)
                .elapsed(null)
                .first(null)
                .last(null)
                .matchCount(null)
                .insertCount(null);

        String output = mongoMetaDataResponseBuilder.build().toString();
        String expected = "{}";
        assertEquals(expected, output);
    }
}
