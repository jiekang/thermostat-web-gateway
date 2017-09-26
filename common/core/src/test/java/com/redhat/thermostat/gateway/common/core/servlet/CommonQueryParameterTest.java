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

package com.redhat.thermostat.gateway.common.core.servlet;

import com.redhat.thermostat.gateway.common.core.model.LimitParameter;
import com.redhat.thermostat.gateway.common.core.model.OffsetParameter;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CommonQueryParameterTest {

    private static final int LIMIT = 11;
    private static final int OFFSET = 55;
    private static final String SORT = "s1";
    private static final String QUERIES = "q2";
    private static final String INCLUDES = "i3";
    private static final String EXCLUDES = "x4";

    private static final LimitParameter LIMITP = new LimitParameter(LIMIT);
    private static final OffsetParameter OFFSETP = new OffsetParameter(OFFSET);

    private static final boolean RETURN_METADATA = true;
    private static final Boolean RETURN_METADATAP = RETURN_METADATA;

    @Test
    public void testCreate1() {
        final CommonQueryParams qp1 = new CommonQueryParams(LIMIT, OFFSET, SORT, QUERIES, INCLUDES, EXCLUDES, RETURN_METADATA);
        assertEquals(LIMIT, qp1.getLimit());
        assertEquals(OFFSET, qp1.getOffset());
        assertEquals(SORT, qp1.getSort());
        assertEquals(QUERIES, qp1.getQueries());
        assertEquals(INCLUDES, qp1.getIncludes());
        assertEquals(EXCLUDES, qp1.getExcludes());
        assertEquals(RETURN_METADATA, qp1.isReturnMetadata());
    }

    @Test
    public void testCreate2() {
        final CommonQueryParams qp1 = new CommonQueryParams(LIMITP, OFFSETP, SORT, QUERIES, INCLUDES, EXCLUDES, RETURN_METADATAP);
        assertEquals(LIMIT, qp1.getLimit());
        assertEquals(OFFSET, qp1.getOffset());
        assertEquals(SORT, qp1.getSort());
        assertEquals(QUERIES, qp1.getQueries());
        assertEquals(INCLUDES, qp1.getIncludes());
        assertEquals(EXCLUDES, qp1.getExcludes());
        assertEquals(RETURN_METADATA, qp1.isReturnMetadata());
    }

    @Test
    public void testBuildParams() {
        final CommonQueryParams qp1 = new CommonQueryParams(LIMIT, OFFSET, SORT, QUERIES, INCLUDES, EXCLUDES, RETURN_METADATA);
        Map<String, String> pmap = qp1.buildParams();
        assertEquals(LIMIT, Integer.parseInt(pmap.get(RequestParameters.LIMIT)));
        assertEquals(OFFSET, Integer.parseInt(pmap.get(RequestParameters.OFFSET)));
        assertEquals(SORT, pmap.get(RequestParameters.SORT));
        assertEquals(QUERIES, pmap.get(RequestParameters.QUERY));
        assertEquals(INCLUDES, pmap.get(RequestParameters.INCLUDE));
        assertEquals(EXCLUDES, pmap.get(RequestParameters.EXCLUDE));
        assertEquals(RETURN_METADATA, Boolean.parseBoolean(pmap.get(RequestParameters.METADATA)));
    }


}
