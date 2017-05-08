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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

public class MongoResponseBuilderTest {

    private MongoResponseBuilder mongoResponseBuilder;

    @Before
    public void setup() {
        mongoResponseBuilder = new MongoResponseBuilder();
    }

    @Test
    public void testBuildGetResponse() {
        Document d1 = Document.parse("{\"hello\" : \"blob\"}");
        Document d2 = Document.parse("{\"a\" : {\"blob\" : [\"hi\"]}}");
        final List<Document> list = new ArrayList<>();
        list.add(d1);
        list.add(d2);

        FindIterable<Document> iterable = new TestFindIterable<>(list);

        String output = mongoResponseBuilder.buildGetResponseString(iterable);
        String expected = "{ \"response\" : [{ \"hello\" : \"blob\" },{ \"a\" : { \"blob\" : [\"hi\"] } }] }";
        assertEquals(expected, output);
    }

    @Test
    public void testBuildEmptyGetResponse() {
        FindIterable<Document> iterable = new TestFindIterable<>(Collections.<Document>emptyList());

        String output = mongoResponseBuilder.buildGetResponseString(iterable);
        String expected = "{ \"response\" : [] }";
        assertEquals(expected, output);
    }

    private class TestFindIterable<T> implements FindIterable<T> {

        private final List<T> list;

        public TestFindIterable(List<T> list) {
            this.list = list;
        }

        @Override
        public MongoCursor<T> iterator() {
            return null;
        }

        @Override
        public T first() {
            return null;
        }

        @Override
        public <U> MongoIterable<U> map(Function<T, U> mapper) {
            return null;
        }

        @Override
        public void forEach(Block<? super T> block) {
            for (T item : list) {
                block.apply(item);
            }
        }

        @Override
        public <A extends Collection<? super T>> A into(A target) {
            return null;
        }

        @Override
        public FindIterable<T> filter(Bson filter) {
            return null;
        }

        @Override
        public FindIterable<T> limit(int limit) {
            return null;
        }

        @Override
        public FindIterable<T> skip(int skip) {
            return null;
        }

        @Override
        public FindIterable<T> maxTime(long maxTime, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public FindIterable<T> maxAwaitTime(long maxAwaitTime, TimeUnit timeUnit) {
            return null;
        }

        @Override
        public FindIterable<T> modifiers(Bson modifiers) {
            return null;
        }

        @Override
        public FindIterable<T> projection(Bson projection) {
            return null;
        }

        @Override
        public FindIterable<T> sort(Bson sort) {
            return null;
        }

        @Override
        public FindIterable<T> noCursorTimeout(boolean noCursorTimeout) {
            return null;
        }

        @Override
        public FindIterable<T> oplogReplay(boolean oplogReplay) {
            return null;
        }

        @Override
        public FindIterable<T> partial(boolean partial) {
            return null;
        }

        @Override
        public FindIterable<T> cursorType(CursorType cursorType) {
            return null;
        }

        @Override
        public FindIterable<T> batchSize(int batchSize) {
            return null;
        }
    }
}
