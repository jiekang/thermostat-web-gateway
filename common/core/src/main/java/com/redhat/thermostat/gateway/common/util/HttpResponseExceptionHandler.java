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

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Accepts exceptions after being told what response to make for the provided
 * exceptions, and creates the appropriate response. It is designed such that
 * even if you don't load it with exceptions, it will still handle all of the
 * exceptions that are children (or the same class as) Exception.
 */
public class HttpResponseExceptionHandler {

    private static final ServiceException DEFAULT_SERVICE_EXCEPTION = ServiceException.UNEXPECTED_ERROR;

    private Map<Class<? extends Exception>, ServiceException> classToResponseMessage = new HashMap<>();

    /**
     * Adds an exception class to be tracked by this object. If the class was
     * already defined, it will replace it.
     * @param cls The class for which the response will be returned.
     * @param serviceException The service exception type.
     * @return Itself (for chaining).
     * @throws NullPointerException If any argument is null.
     */
    public HttpResponseExceptionHandler add(Class<? extends Exception> cls, ServiceException serviceException) {
        Objects.requireNonNull(cls);
        Objects.requireNonNull(serviceException);
        classToResponseMessage.put(cls, serviceException);
        return this;
    }

    /**
     * Generates the response from the exception. If the exception is not known
     * to the class (because it was never specified by .add()) then it will
     * return a generic INTERNAL_SERVER_ERROR status with no message attached.
     * @param exception The exception to get the error message for.
     * @return The response for the exception.
     * @throws NullPointerException If the argument is null.
     */
    public Response generateResponseForException(Exception exception) {
        Objects.requireNonNull(exception);
        ServiceException serviceException = DEFAULT_SERVICE_EXCEPTION;
        if (classToResponseMessage.containsKey(exception.getClass())) {
            serviceException = classToResponseMessage.get(exception.getClass());
        }
        return serviceException.buildResponse();
    }
}
