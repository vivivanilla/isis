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
package org.apache.isis.core.metamodel.facetapi;

import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;

import junit.framework.TestCase;

public class FacetAbstractTest extends TestCase {

    public static interface FooFacet extends Facet {
    }

    public static interface BarFacet extends Facet {
    }

    public static class ConcreteFacet extends FacetAbstract {
        public ConcreteFacet(final Class<? extends Facet> facetType, final FacetHolder holder) {
            super(facetType, holder);
        }

    }

    private FacetAbstract fooFacet;
    private FacetHolder facetHolder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        facetHolder = FacetHolder.forTesting(MetaModelContext_forTesting.buildDefault());
        fooFacet = new ConcreteFacet(FooFacet.class, facetHolder);
        new ConcreteFacet(BarFacet.class, facetHolder);
        facetHolder.addFacet(fooFacet);
    }

    @Override
    protected void tearDown() throws Exception {
        fooFacet = null;
        facetHolder = null;
        super.tearDown();
    }

    public void testFacetType() {
        assertEquals(FooFacet.class, fooFacet.facetType());
    }

    public void testGetFacetHolder() {
        assertEquals(facetHolder, fooFacet.getFacetHolder());
    }

    public void testToString() {
        assertEquals("FacetAbstractTest.ConcreteFacet[type=FacetAbstractTest.FooFacet; precedence=DEFAULT]",
                fooFacet.toString());
    }

}
