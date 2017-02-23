package com.redhat.thermostat.server.core.internal.web.json;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DocumentBuilderTest {
    @Test
    public void testAddTags() {
        String content = "{\"a\":\"b\"}";
        String[] tags = {"x", "y"};

        String result = DocumentBuilder.addTags(content, tags);

        String expected = "{\"a\":\"b\",\"tags\":[\"admin\",\"x\",\"y\"]}";
        assertEquals(expected, result);
    }
}
