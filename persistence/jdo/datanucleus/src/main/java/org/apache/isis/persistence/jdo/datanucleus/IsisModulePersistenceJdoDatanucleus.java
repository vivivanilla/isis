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
package org.apache.isis.persistence.jdo.datanucleus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;
import javax.jdo.JDOException;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.annotations.PersistenceCapable;
import javax.sql.DataSource;

import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.beans.aoppatch.TransactionInterceptorFactory;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.transaction.changetracking.EntityChangeTracker;
import org.apache.isis.persistence.jdo.datanucleus.changetracking.JdoLifecycleListener;
import org.apache.isis.persistence.jdo.datanucleus.config.DatanucleusSettings;
import org.apache.isis.persistence.jdo.datanucleus.dialect.DnJdoDialect;
import org.apache.isis.persistence.jdo.datanucleus.entities.DnEntityStateProvider;
import org.apache.isis.persistence.jdo.datanucleus.jdosupport.JdoSupportServiceDefault;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForByteId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForCharId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForCharIdentity;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForDatastoreId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForDatastoreUniqueLongId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForIntId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForLongId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForObjectId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForShortId;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForStringId;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionLong;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionTimestamp;
import org.apache.isis.persistence.jdo.datanucleus.mixins.Persistable_downloadJdoMetadata;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForByteIdentity;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForDatastoreIdImpl;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForIntIdentity;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForLongIdentity;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForObjectIdentity;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForShortIdentity;
import org.apache.isis.persistence.jdo.datanucleus.metamodel.facets.entity.IdStringifierForStringIdentity;
import org.apache.isis.persistence.jdo.integration.IsisModulePersistenceJdoIntegration;
import org.apache.isis.persistence.jdo.provider.config.JdoEntityDiscoveryListener;
import org.apache.isis.persistence.jdo.spring.integration.JdoDialect;
import org.apache.isis.persistence.jdo.spring.integration.JdoTransactionManager;
import org.apache.isis.persistence.jdo.spring.integration.LocalPersistenceManagerFactoryBean;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // modules
    IsisModulePersistenceJdoIntegration.class,

    // @Component's
    DnEntityStateProvider.class,

    IdStringifierForDatastoreIdImpl.class, // datastore identity
    IdStringifierForDatastoreUniqueLongId.class,
    IdStringifierForDatastoreId.class,
    IdStringifierForShortIdentity.class, // application-defined PK, javax.jdo.identity
    IdStringifierForLongIdentity.class,
    IdStringifierForIntIdentity.class,
    IdStringifierForByteIdentity.class,
    IdStringifierForCharIdentity.class,
    IdStringifierForStringIdentity.class,
    IdStringifierForObjectIdentity.class,
    IdStringifierForShortId.class,  // application-defined PK, org.datanucleus.identity
    IdStringifierForLongId.class,
    IdStringifierForIntId.class,
    IdStringifierForByteId.class,
    IdStringifierForCharId.class,
    IdStringifierForStringId.class,
    IdStringifierForObjectId.class,

    // @Mixin's
    Persistable_datanucleusVersionLong.class,
    Persistable_datanucleusVersionTimestamp.class,
    Persistable_downloadJdoMetadata.class,

    // @Service's
    JdoSupportServiceDefault.class,

})
@EnableConfigurationProperties(DatanucleusSettings.class)
@Log4j2
public class IsisModulePersistenceJdoDatanucleus {

    /**
     * Conveniently registers this dialect as a {@link PersistenceExceptionTranslator} with <i>Spring</i>.
     * @see PersistenceExceptionTranslator
     * @see JdoDialect
     */
    @Qualifier("jdo-dialect")
    @Bean
    public DnJdoDialect getDnJdoDialect(final DataSource dataSource) {
        return new DnJdoDialect(dataSource);
    }

    private static boolean ignore(final Class<?> entityType) {
        try {
            if(entityType.isAnonymousClass() || entityType.isLocalClass() || entityType.isMemberClass() || entityType.isInterface()) {
                return true;
            }
            val persistenceCapable = entityType.getAnnotation(PersistenceCapable.class);
            return persistenceCapable == null; // ignore if doesn't have @PersistenceCapable
        } catch (NoClassDefFoundError ex) {
            return true;
        }
    }

    @Qualifier("local-pmf-proxy")
    @Bean
    public LocalPersistenceManagerFactoryBean getLocalPersistenceManagerFactoryBean(
            final IsisConfiguration isisConfiguration,
            final DataSource dataSource,
            final MetaModelContext metaModelContext,
            final EventBusService eventBusService,
            final Provider<EntityChangeTracker> entityChangeTrackerProvider,
            final IsisBeanTypeRegistry beanTypeRegistry,
            final DatanucleusSettings dnSettings) {

        _Assert.assertNotNull(dataSource, "a datasource is required");

        final Set<String> classNamesNotEnhanced = new LinkedHashSet<>();

        autoCreateSchemas(dataSource, isisConfiguration);

        val lpmfBean = new LocalPersistenceManagerFactoryBean() {
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(final java.util.Map<?,?> props) {
                val pu = createDefaultPersistenceUnit(beanTypeRegistry);
                val pmf = new JDOPersistenceManagerFactory(pu, props);
                pmf.setConnectionFactory(dataSource);
                integrateWithApplicationLayer(metaModelContext, eventBusService, entityChangeTrackerProvider, pmf);
                return pmf;
            }
            @Override
            protected PersistenceManagerFactory newPersistenceManagerFactory(final String name) {
                val pmf = super.newPersistenceManagerFactory(name);
                pmf.setConnectionFactory(dataSource); //might be too late, anyway, not sure if this is ever called
                integrateWithApplicationLayer(metaModelContext, eventBusService, entityChangeTrackerProvider, pmf);
                return pmf;
            }
        };
        lpmfBean.setJdoPropertyMap(dnSettings.getAsProperties());
        return lpmfBean;
    }

    @Qualifier("transaction-aware-pmf-proxy")
    @Bean @Primary
    public TransactionAwarePersistenceManagerFactoryProxy getTransactionAwarePersistenceManagerFactoryProxy(
            final MetaModelContext metaModelContext,
            final @Qualifier("local-pmf-proxy") LocalPersistenceManagerFactoryBean localPmfBean,
            final IsisBeanTypeRegistry beanTypeRegistry,
            final List<JdoEntityDiscoveryListener> jdoEntityDiscoveryListeners,
            final DatanucleusSettings dnSettings) {

        val pmf = localPmfBean.getObject(); // created once per application lifecycle

        notifyJdoEntityDiscoveryListeners(pmf, beanTypeRegistry, jdoEntityDiscoveryListeners, dnSettings);

        val tapmfProxy = new TransactionAwarePersistenceManagerFactoryProxy(metaModelContext);
        tapmfProxy.setTargetPersistenceManagerFactory(pmf);
        tapmfProxy.setAllowCreate(false);
        return tapmfProxy;
    }


    @Qualifier("jdo-platform-transaction-manager")
    @Bean @Primary
    public JdoTransactionManager getTransactionManager(
            final @Qualifier("jdo-dialect") JdoDialect jdoDialect,
            final @Qualifier("local-pmf-proxy") LocalPersistenceManagerFactoryBean localPmfBean) {

        val pmf = localPmfBean.getObject(); // created once per application lifecycle
        val txManager = new JdoTransactionManager(pmf);
        txManager.setJdoDialect(jdoDialect);
        return txManager;
    }

    /**
     * AOP PATCH
     * @implNote works only with patch package 'org.apache.isis.core.config.beans.aoppatch'
     */
    @Bean @Primary
    @SuppressWarnings("serial")
    public TransactionInterceptorFactory getTransactionInterceptorFactory() {
        return ()->new TransactionInterceptor() {
            @Override @SneakyThrows
            protected void completeTransactionAfterThrowing(final TransactionInfo txInfo, final Throwable ex) {
                super.completeTransactionAfterThrowing(txInfo, ex);

                if(ex instanceof RuntimeException) {
                    val txManager = txInfo.getTransactionManager();
                    if(txManager instanceof JdoTransactionManager) {
                        val jdoDialect = ((JdoTransactionManager)txManager).getJdoDialect();
                        if(jdoDialect instanceof PersistenceExceptionTranslator) {
                            val translatedEx = ((PersistenceExceptionTranslator)jdoDialect)
                                    .translateExceptionIfPossible((RuntimeException)ex);

                            if(translatedEx!=null) {
                                throw translatedEx;
                            }

                        }

                        if(ex instanceof JDOException) {
                            val translatedEx = jdoDialect.translateException((JDOException)ex);

                            if(translatedEx!=null) {
                                throw translatedEx;
                            }
                        }
                    }
                }
            }
        };
    }

    // -- HELPER

    private static void notifyJdoEntityDiscoveryListeners(
            final PersistenceManagerFactory pmf,
            final IsisBeanTypeRegistry beanTypeRegistry,
            final List<JdoEntityDiscoveryListener> jdoEntityDiscoveryListeners,
            final DatanucleusSettings dnSettings) {

        if(_NullSafe.isEmpty(jdoEntityDiscoveryListeners)) {
            return;
        }
        // assuming, as we instantiate a DN PMF, all entities discovered are JDO entities
        val jdoEntityTypes = beanTypeRegistry.getEntityTypes();
        if(_NullSafe.isEmpty(jdoEntityTypes)) {
            return;
        }
        val jdoEntityTypesView = Collections.unmodifiableSet(jdoEntityTypes.keySet());
        val dnProps = Collections.unmodifiableMap(dnSettings.getAsProperties());
        jdoEntityDiscoveryListeners
            .forEach(listener->
                    listener.onEntitiesDiscovered(pmf, jdoEntityTypesView, dnProps));
    }

    /**
     * integrates with settings from isis.persistence.schema.*
     */
    @SneakyThrows
    private static DataSource autoCreateSchemas(
            final DataSource dataSource,
            final IsisConfiguration isisConfiguration) {

        val persistenceSchemaConf = isisConfiguration.getPersistence().getSchema();

        if(!persistenceSchemaConf.getAutoCreateSchemas().isEmpty()) {

            log.info("About to create db schema(s) {} with template '{}'",
                    persistenceSchemaConf.getAutoCreateSchemas(),
                    persistenceSchemaConf.getCreateSchemaSqlTemplate());

            try(val con = dataSource.getConnection()){

                val s = con.createStatement();

                for(val schema : persistenceSchemaConf.getAutoCreateSchemas()) {
                    val sql = String.format(persistenceSchemaConf.getCreateSchemaSqlTemplate(), schema);
                    log.info("SQL '{}'", sql);
                    s.execute(sql);
                }

            }
        }

        return dataSource;
    }

    private static PersistenceUnitMetaData createDefaultPersistenceUnit (
            final IsisBeanTypeRegistry beanTypeRegistry) {
        val pumd = new PersistenceUnitMetaData(
                "dynamic-unit", "RESOURCE_LOCAL", null);
        pumd.setExcludeUnlistedClasses(false);
        beanTypeRegistry.getEntityTypes().keySet().stream()
        .map(Class::getName)
        .forEach(pumd::addClassName);
        return pumd;
    }

    private static void integrateWithApplicationLayer(
            final MetaModelContext metaModelContext,
            final EventBusService eventBusService,
            final Provider<EntityChangeTracker> entityChangeTrackerProvider,
            final PersistenceManagerFactory pmf) {

        // install JDO specific entity change listeners ...

        val jdoLifecycleListener =
                new JdoLifecycleListener(metaModelContext, eventBusService, entityChangeTrackerProvider);
        pmf.addInstanceLifecycleListener(jdoLifecycleListener, (Class[]) null);

    }

}
