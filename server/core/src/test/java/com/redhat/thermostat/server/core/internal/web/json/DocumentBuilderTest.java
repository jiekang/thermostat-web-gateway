package com.redhat.thermostat.server.core.internal.web.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DocumentBuilderTest {
    @Test
    public void testAddTags() {
        String content = "{\"a\":\"b\"}";
        String[] tags = {"x", "y"};

        String result = new DocumentBuilder(content).addTags(tags).build();

        String expected = "{ \"obj\" : {\"a\":\"b\"},\"tags\":[\"admin\",\"x\",\"y\"]}";
        assertEquals(expected, result);
    }

    @Test
    public void testAddOneTag() {
        String content = "{\"a\":\"b\"}";
        String[] tags = {"x"};

        String result = new DocumentBuilder(content).addTags(tags).build();

        String expected = "{ \"obj\" : {\"a\":\"b\"},\"tags\":[\"admin\",\"x\"]}";
        assertEquals(expected, result);
    }

    @Test
    public void testAddZeroTags() {
        String content = "{\"a\":\"b\"}";

        String result = new DocumentBuilder(content).addTags().build();

        String expected = "{ \"obj\" : {\"a\":\"b\"},\"tags\":[\"admin\"]}";
        assertEquals(expected, result);
    }
}
