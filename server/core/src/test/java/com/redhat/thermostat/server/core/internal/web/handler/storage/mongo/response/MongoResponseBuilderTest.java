package com.redhat.thermostat.server.core.internal.web.handler.storage.mongo.response;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

import com.mongodb.Block;
import com.mongodb.CursorType;
import com.mongodb.Function;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

public class MongoResponseBuilderTest {

    @Test
    public void testBuildJsonResponseWithTime() {
        String documents = "\"response\" : [\"hello\", \"blob\"]";
        long elapsed = 10;
        String output = MongoResponseBuilder.buildJsonResponseWithTime(documents, elapsed);

        String expected = "{\"response\" : [\"hello\", \"blob\"],\"time\" : \"10\"}";
        assertEquals(expected, output);
    }

    @Test
    public void testBuildJsonDocument() {
        Document d1 = Document.parse("{\"hello\" : \"blob\"}");
        Document d2 = Document.parse("{\"a\" : {\"blob\" : [\"hi\"]}}");
        final List<Document> list = new ArrayList<>();
        list.add(d1);
        list.add(d2);

        FindIterable<Document> iterable = new TestFindIterable<>(list);

        String output = MongoResponseBuilder.buildJsonDocuments(iterable);
        String expected = "\"response\" : [{ \"hello\" : \"blob\" },{ \"a\" : { \"blob\" : [\"hi\"] } }]";
        assertEquals(expected, output);
    }

    private class TestFindIterable<T> implements FindIterable<T> {

        private final List<T> list;

        public TestFindIterable(List<T> list) {
            this.list = list;
        }

        @Override
        public MongoCursor<T> iterator() {
            return new MongoCursor<T>() {

                private int i = 0;

                @Override
                public void close() {

                }

                @Override
                public boolean hasNext() {
                    return i < list.size();
                }

                @Override
                public T next() {
                    i++;
                    return list.get(i - 1);
                }

                @Override
                public void remove() {

                }

                @Override
                public T tryNext() {
                    return null;
                }

                @Override
                public ServerCursor getServerCursor() {
                    return null;
                }

                @Override
                public ServerAddress getServerAddress() {
                    return null;
                }
            };
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
