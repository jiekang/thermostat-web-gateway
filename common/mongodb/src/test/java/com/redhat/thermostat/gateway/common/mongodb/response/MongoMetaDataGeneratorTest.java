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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

import com.redhat.thermostat.gateway.common.mongodb.executor.MongoDataResultContainer;

public class MongoMetaDataGeneratorTest {

    private MongoMetaDataGenerator fullGenerator;
    private MongoMetaDataResponseBuilder.MetaBuilder response;

    @Before
    public void setup() {
        HttpServletRequest requestInfo = mock(HttpServletRequest.class);
        when(requestInfo.getRequestURL()).thenReturn(new StringBuffer("127.0.0.1:8080/base/"));
        when(requestInfo.getQueryString()).thenReturn("l=2&o=2&m=true");

        MongoDataResultContainer container = new MongoDataResultContainer();
        container.setRemainingNumQueryDocuments(1);
        container.setGetReqCount(4);

        fullGenerator = new MongoMetaDataGenerator(2, 2, "", "test1==b", "", requestInfo, container);
        response = new MongoMetaDataResponseBuilder.MetaBuilder();
    }

    @Test
    public void testSetCounts() {
        fullGenerator.setDocAndPayloadCount(response);
        String output = response.build().toString();
        String expected = "{\"payloadCount\":0,\"count\":4}";

        assertEquals(expected, output);
    }

    @Test
    public void testSetPrev() {
        fullGenerator.setPrev(response);
        String output = response.build().toString();

        // {"prev":"127.0.0.1:8080/base/?m=true&l=2&o=0"}
        String expected = "{\"prev\":\"127.0.0.1:8080/base/?m\\u003dtrue\\u0026l\\u003d2\\u0026o\\u003d0\"}";

        assertEquals(expected, output);
    }

    @Test
    public void testSetNext() {
        fullGenerator.setNext(response);
        String output = response.build().toString();

        // {"payloadCount":1,"next":"127.0.0.1:8080/base/?o=4&l=1&m=true"}//{"payloadCount":1,"next":"127.0.0.1:8080/base/?o=4&l=1&m=true"}
        String expected = "{\"payloadCount\":1,\"next\":\"127.0.0.1:8080/base/?o\\u003d4\\u0026l\\u003d1\\u0026m\\u003dtrue\"}";

        assertEquals(expected, output);
    }

}
