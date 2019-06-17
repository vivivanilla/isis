/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.testdomain.tests;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.commons.collections.Bin;
import org.apache.isis.core.integtestsupport.IntegrationTestJupiter;
import org.apache.isis.core.runtime.headless.HeadlessTransactionSupport;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.testdomain.jdo.Inventory;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.JdoTestModule;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;

class JdoBootstrappingTest extends IntegrationTestJupiter {

    public JdoBootstrappingTest() {
        super(new JdoTestModule()
                
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.cache.level2.mode", "ENABLE_SELECTIVE")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.cache.level2.type", "none")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.identifier.case", "MixedCase")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.persistenceByReachabilityAtCommit", "false")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.schema.autoCreateAll", "true")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.schema.validateConstraints", "true")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.schema.validateTables", "true")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionDriverName", "org.h2.Driver")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionPassword", "")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL", "jdbc:h2:mem:test")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionUserName", "sa")
                .withConfigurationProperty("isis.reflector.explicitAnnotations.action", "true")
                .withConfigurationProperty("isis.reflector.facet.cssClass.patterns", "delete.*:btn-danger")
                .withConfigurationProperty("isis.reflector.facet.cssClassFa.patterns", "new.*:fa-plus,add.*:fa-plus-square,create.*:fa-plus,update.*:fa-edit,delete.*:fa-trash,find.*:fa-search,list.*:fa-list")
                .withConfigurationProperty("isis.reflector.validator.allowDeprecated", "false")
                .withConfigurationProperty("isis.reflector.validator.explicitObjectType", "true")
                .withConfigurationProperty("isis.reflector.validator.mixinsOnly", "true")
                .withConfigurationProperty("isis.reflector.validator.noParamsOnly", "true")
                .withConfigurationProperty("isis.reflector.validator.serviceActionsOnly", "true")
                .withConfigurationProperty("isis.services.translation.po.mode", "disable")
                
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.schema.autoCreateAll","true")
                .withConfigurationProperty("isis.persistor.datanucleus.impl.datanucleus.identifier.case","MixedCase")
                .withConfigurationProperty("isis.objects.editing", "false"));
    }

    @Inject IsisSessionFactory isisSessionFactory;
    @Inject HeadlessTransactionSupport transactions;
    @Inject FixtureScripts fixtureScripts;
    @Inject RepositoryService repository;
    
    @BeforeEach
    void beforeEach() {
        System.out.println("================== START ====================");
    }
    
    void setUp() {
        
//        isisSessionFactory.openSession(new InitialisationSession());
//        
//        transactions.beginTransaction();
        
        System.out.println("================== RUN FIXTURE 1 ====================");
        
//        // cleanup
//        fixtureScripts.runBuilderScript(
//                JdoTestDomainPersona.PurgeAll.builder());
        
        System.out.println("================== RUN FIXTURE 2 ====================");

        // given
        fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.InventoryWith1Book.builder());
        
        System.out.println("================== FIXTURES DONE ====================");
        
//        transactions.endTransaction();
//        
//        isisSessionFactory.closeSession();

    }
    
    @Test
    void sampleInventoryShouldBeSetUp() {
        
        setUp();

//    	isisSessionFactory.openSession(new InitialisationSession());
//    	
//    	transactions.beginTransaction();
    	
    	val inventories = Bin.ofCollection(repository.allInstances(Inventory.class)); 
		assertEquals(1, inventories.size());
		
		val inventory = inventories.getSingleton().get();
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(1, inventory.getProducts().size());
        
        val product = inventory.getProducts().iterator().next();
        assertEquals("Sample Book", product.getName());
        
//        transactions.endTransaction();
//        
//        isisSessionFactory.closeSession();
        
    }

        
}
