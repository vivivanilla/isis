/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.metamodel.facets.value;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.core.metamodel.valuesemantics.BigDecimalValueSemantics;

public class BigDecimalValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<BigDecimal> {

    private BigDecimalValueSemantics value;
    private BigDecimal bigDecimal;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        bigDecimal = new BigDecimal("34132.199");
        allowMockAdapterToReturn(bigDecimal);

        setSemantics(value = new BigDecimalValueSemantics());
    }

    @Test
    public void testParseValidString() throws Exception {
        final Object newValue = value.parseTextRepresentation(null, "2142342334");
        assertEquals(new BigDecimal(2142342334L), newValue);
    }

    @Test
    public void testParseInvalidString() throws Exception {
        try {
            value.parseTextRepresentation(null, "214xxx2342334");
            fail();
        } catch (final TextEntryParseException expected) {
        }
    }

    @Test
    public void testTitleOf() {
        assertEquals("34,132.199", value.titlePresentation(null, bigDecimal));
    }

}
