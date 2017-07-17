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

package com.redhat.thermostat.gateway.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.core.Response;

import org.junit.Test;

public class HttpResponseExceptionHandlerTest {

    private static void assertResponse(ServiceException serviceException, Response response) {
        assertNotNull(response);
        Response expectedResponse = serviceException.buildResponse();
        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getEntity(), response.getEntity());
    }

    @Test
    public void testWithNoLoadedExceptions() {
        HttpResponseExceptionHandler exceptionHandler = new HttpResponseExceptionHandler();
        Response response = exceptionHandler.generateResponseForException(new Exception());
        assertResponse(ServiceException.UNEXPECTED_ERROR, response);

        response = exceptionHandler.generateResponseForException(new NullPointerException());
        assertResponse(ServiceException.UNEXPECTED_ERROR, response);

        response = exceptionHandler.generateResponseForException(new ClassCastException());
        assertResponse(ServiceException.UNEXPECTED_ERROR, response);
    }

    @Test
    public void testWithLoadedExceptions() {
        HttpResponseExceptionHandler exceptionHandler = new HttpResponseExceptionHandler();
        Response response = exceptionHandler.generateResponseForException(new ClassCastException());
        assertResponse(ServiceException.UNEXPECTED_ERROR, response);

        exceptionHandler.add(ClassCastException.class, ServiceException.EXPECTED_JSON_ARRAY);

        response = exceptionHandler.generateResponseForException(new ClassCastException());
        assertResponse(ServiceException.EXPECTED_JSON_ARRAY, response);

        // Shouldn't affect the default one.
        response = exceptionHandler.generateResponseForException(new NullPointerException());
        assertResponse(ServiceException.UNEXPECTED_ERROR, response);
    }

    @Test
    public void testChangeExceptions() {
        HttpResponseExceptionHandler exceptionHandler = new HttpResponseExceptionHandler();
        Response response = exceptionHandler.generateResponseForException(new ClassCastException());
        assertResponse(ServiceException.UNEXPECTED_ERROR, response);

        exceptionHandler.add(ClassCastException.class, ServiceException.EXPECTED_JSON_ARRAY);

        response = exceptionHandler.generateResponseForException(new ClassCastException());
        assertResponse(ServiceException.EXPECTED_JSON_ARRAY, response);

        exceptionHandler.add(ClassCastException.class, ServiceException.DATABASE_UNAVAILABLE);

        response = exceptionHandler.generateResponseForException(new ClassCastException());
        assertResponse(ServiceException.DATABASE_UNAVAILABLE, response);
    }

    @Test(expected = NullPointerException.class)
    public void testNoNullsAllowedAddFirstArg() {
        new HttpResponseExceptionHandler().add(null, ServiceException.UNEXPECTED_ERROR);
    }

    @Test(expected = NullPointerException.class)
    public void testNoNullsAllowedAddSecondArg() {
        new HttpResponseExceptionHandler().add(Exception.class, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNoNullsAllowedAddBothArgs() {
        new HttpResponseExceptionHandler().add(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNoNullsResponseNullException() {
        new HttpResponseExceptionHandler().generateResponseForException(null);
    }
}
