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

package com.redhat.thermostat.gateway.common.mongodb.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.conversions.Bson;
import org.bson.json.JsonParseException;
import org.junit.Test;

import com.mongodb.MongoClient;

public class MongoRequestFiltersTest {

    @Test
    public void testLte() {
        String query = "a<=b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$lte").getValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
    }

    @Test
    public void testGte() {
        String query = "a>=b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$gte").getValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
    }

    @Test
    public void testEq() {
        String query = "a==b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getString("a").getValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.size());
    }

    @Test
    public void testNe() {
        String query = "a!=b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$ne").getValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
    }

    @Test
    public void testLt() {
        String query = "a<b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$lt").getValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
    }

    @Test
    public void testGt() {
        String query = "a>b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$gt").getValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
    }

    @Test
    public void testMultiple() {
        List<String> queries = new ArrayList<>();
        queries.add("a>b");
        queries.add("c<d");

        Bson filter = MongoRequestFilters.buildQueriesFilter(queries);
        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());

        assertEquals(2, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
        assertEquals(1, bsonDocument.getDocument("c").size());
        assertEquals("b", bsonDocument.getDocument("a").getString("$gt").getValue());
        assertEquals("d", bsonDocument.getDocument("c").getString("$lt").getValue());
    }

    @Test
    public void testNumberComparison() {
        String query = "a>25";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals(25, bsonDocument.getDocument("a").getNumber("$gt").intValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
    }

    @Test
    public void testStringComparison() {
        String query = "a>\"25\"";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("25", bsonDocument.getDocument("a").getString("$gt").getValue());
        assertEquals(1, bsonDocument.size());
        assertEquals(1, bsonDocument.getDocument("a").size());
    }

    @Test(expected=JsonParseException.class)
    public void testInvalid() {
        MongoRequestFilters.buildQueriesFilter(Collections.singletonList("a"));
    }

    @Test(expected=JsonParseException.class)
    public void testInvalidWithSymbols() {
        MongoRequestFilters.buildQueriesFilter(Collections.singletonList("a="));
    }

    @Test
    public void testBuildRealmsFilter() {
        Set<String> realms = new HashSet<>();
        realms.add("one");
        realms.add("two");

        Bson filter = MongoRequestFilters.buildRealmsFilter(realms);
        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());

        BsonDocument realmsDocument = bsonDocument.getDocument("realms");
        assertTrue(realmsDocument.containsKey("$all"));
        assertTrue(realmsDocument.containsKey("$size"));

        assertEquals(2, realmsDocument.get("$size").asInt32().getValue());

        BsonArray allDocument = realmsDocument.getArray("$all");
        assertEquals(2, allDocument.size());
        assertTrue(allDocument.contains(new BsonString("one")));
        assertTrue(allDocument.contains(new BsonString("two")));
    }
}
