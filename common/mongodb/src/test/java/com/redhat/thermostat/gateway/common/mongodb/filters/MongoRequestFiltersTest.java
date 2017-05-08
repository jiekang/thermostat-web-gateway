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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Test;

import com.mongodb.MongoClient;

public class MongoRequestFiltersTest {

    @Test
    public void testLte() {
        String query = "a<=b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$lte").getValue());
    }

    @Test
    public void testGte() {
        String query = "a>=b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$gte").getValue());
    }

    @Test
    public void testEq() {
        String query = "a==b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getString("a").getValue());
    }

    @Test
    public void testNe() {
        String query = "a!=b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$ne").getValue());
    }

    @Test
    public void testLt() {
        String query = "a<b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$lt").getValue());
    }

    @Test
    public void testGt() {
        String query = "a>b";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("b", bsonDocument.getDocument("a").getString("$gt").getValue());
    }

    @Test
    public void testMultiple() {
        List<String> queries = new ArrayList<>();
        queries.add("a>b");
        queries.add("c<d");

        Bson filter = MongoRequestFilters.buildQueriesFilter(queries);
        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());

        assertEquals("b", bsonDocument.getDocument("a").getString("$gt").getValue());
        assertEquals("d", bsonDocument.getDocument("c").getString("$lt").getValue());
    }

    @Test
    public void testNumberComparison() {
        String query = "a>25";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals(25, bsonDocument.getDocument("a").getNumber("$gt").intValue());
    }

    @Test
    public void testStringComparison() {
        String query = "a>\"25\"";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));

        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals("25", bsonDocument.getDocument("a").getString("$gt").getValue());
    }
    @Test
    public void testInvalid() {
        String query = "a";
        Bson filter = MongoRequestFilters.buildQueriesFilter(Collections.singletonList(query));
        BsonDocument bsonDocument = filter.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        assertEquals(0, bsonDocument.getArray("$and").size());
    }
}
