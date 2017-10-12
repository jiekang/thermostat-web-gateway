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
import static org.junit.Assert.assertNotNull;

import com.google.gson.JsonSyntaxException;
import org.junit.Test;

public class MongoGsonFactoryTest {

    private static final String NUMBER_LONG_POJO_STRING = "{\"someInt\":42,\"someLong\":{\"$numberLong\":\"12345\"},\"someString\":\"long\"}";
    private static final String NUMBER_LONG_NONOBJECT_POJO_STRING = "{\"someInt\":42,\"someLong\":\"12345\",\"someString\":\"long\"}";

    @Test
    public void instanceNotNull() {
        assertNotNull(MongoGsonFactory.getGson());
    }

    @Test
    public void gsonHasNumberLong() {
        String actual = MongoGsonFactory.getGson().toJson(new NumberLongPojo(42, 12345L, "long"));
        assertEquals(NUMBER_LONG_POJO_STRING, actual);
    }

    @Test
    public void gsonReadsNumberLong() {
        NumberLongPojo numberLongPojo = MongoGsonFactory.getGson().fromJson(NUMBER_LONG_POJO_STRING, NumberLongPojo.class);
        assertEquals(42, numberLongPojo.someInt);
        assertEquals(new Long(12345L), numberLongPojo.someLong);
        assertEquals("long", numberLongPojo.someString);
    }

    @Test
    public void gsonHasNumberLongPrimitive() {
        String actual = MongoGsonFactory.getGson().toJson(new NumberLongPojoWithPrimitive(42, 12345L, "long"));
        assertEquals(NUMBER_LONG_POJO_STRING, actual);
    }

    @Test
    public void gsonReadsNumberLongPrimitive() {
        NumberLongPojoWithPrimitive numberLongPojo = MongoGsonFactory.getGson().fromJson(NUMBER_LONG_POJO_STRING, NumberLongPojoWithPrimitive.class);
        assertEquals(42, numberLongPojo.someInt);
        assertEquals(12345L, numberLongPojo.someLong);
        assertEquals("long", numberLongPojo.someString);
    }

    @Test(expected = JsonSyntaxException.class)
    public void gsonReadFailsWithNoObjectWrapper() {
        MongoGsonFactory.getGson().fromJson(NUMBER_LONG_NONOBJECT_POJO_STRING, NumberLongPojo.class);
    }

    @Test(expected = JsonSyntaxException.class)
    public void gsonReadFailsWithNoObjectWrapperPrimitive() {
        MongoGsonFactory.getGson().fromJson(NUMBER_LONG_NONOBJECT_POJO_STRING, NumberLongPojoWithPrimitive.class);
    }

    private class NumberLongPojo {
        private int someInt;
        private Long someLong;
        private String someString;

        private NumberLongPojo(int someInt, long someLong, String someString) {
            this.someInt = someInt;
            this.someLong = someLong;
            this.someString = someString;
        }
    }

    private class NumberLongPojoWithPrimitive {
        private int someInt;
        private long someLong;
        private String someString;

        private NumberLongPojoWithPrimitive(int someInt, long someLong, String someString) {
            this.someInt = someInt;
            this.someLong = someLong;
            this.someString = someString;
        }
    }
}
