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
package org.apache.isis.core.metamodel.facets.object;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessClassContext;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

import lombok.val;

public class ViewModelSemanticCheckingFacetFactoryTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);


    @Mock @JUnitRuleMockery2.Ignoring
    private ServiceInjector mockServicesInjector;

    private MetaModelContext metaModelContext;
    private ViewModelSemanticCheckingFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {

        val configuration = new IsisConfiguration(null);
        configuration.getApplib().getAnnotation().getViewModel().getValidation().getSemanticChecking().setEnable(true);

        metaModelContext = MetaModelContext_forTesting.builder()
                .configuration(configuration)
                .programmingModelFactory(mmc->new ProgrammingModelAbstract(mmc) {})
                .build();

        facetFactory = new ViewModelSemanticCheckingFacetFactory(metaModelContext);
    }

    @Test
    public void whenValidAnnotatedDomainObjectAndDomainObjectLayout() throws Exception {

        @org.apache.isis.applib.annotation.DomainObject
        @org.apache.isis.applib.annotation.DomainObjectLayout
        class ValidAnnotatedDomainObjectAndDomainObjectLayout {
        }

        val validationFailures = processThenValidate(ValidAnnotatedDomainObjectAndDomainObjectLayout.class);
        assertThat(validationFailures.getNumberOfFailures(), is(0));
    }

    // -- HELPER

    private ValidationFailures processThenValidate(final Class<?> cls) {

        val holder = FacetHolder.forTesting(metaModelContext);
        facetFactory.process(ProcessClassContext.forTesting(cls, null, holder));

        return metaModelContext.getSpecificationLoader().getOrAssessValidationResult();
    }


}