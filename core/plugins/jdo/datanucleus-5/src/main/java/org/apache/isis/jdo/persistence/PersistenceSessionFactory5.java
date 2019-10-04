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

package org.apache.isis.jdo.persistence;

import java.util.Map;
import java.util.Objects;

import javax.enterprise.inject.Vetoed;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.StoreLifecycleListener;

import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.components.ApplicationScopedComponent;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.jdo.datanucleus.DataNucleusSettings;
import org.apache.isis.jdo.entities.JdoEntityTypeRegistry;
import org.apache.isis.jdo.lifecycles.JdoStoreLifecycleListenerForIsis;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.persistence.FixturesInstalledState;
import org.apache.isis.runtime.persistence.FixturesInstalledStateHolder;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtime.system.persistence.PersistenceSessionFactory;
import org.apache.isis.security.authentication.AuthenticationSession;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 *
 * Factory for {@link PersistenceSession}.
 *
 */
@Vetoed @Log4j2
public class PersistenceSessionFactory5
implements PersistenceSessionFactory, ApplicationScopedComponent, FixturesInstalledStateHolder {

    private final _Lazy<DataNucleusApplicationComponents5> applicationComponents = 
            _Lazy.threadSafe(this::createDataNucleusApplicationComponents);
    
    private StoreLifecycleListener storeLifecycleListener;
    private IsisConfiguration configuration;

    @Getter(onMethod=@__({@Override})) 
    @Setter(onMethod=@__({@Override})) 
    FixturesInstalledState fixturesInstalledState;

    @Override
    public void init() {
        this.configuration = IsisContext.getConfiguration();

        // need to eagerly build, ... must be completed before catalogNamedQueries().
        // Why? because that method causes entity classes to be loaded which register with DN's EnhancementHelper,
        // which are then cached in DN.  It results in our CreateSchema listener not firing.
        _Blackhole.consume(applicationComponents.get());
        
        this.storeLifecycleListener = new JdoStoreLifecycleListenerForIsis();
    }


    @Override
    public boolean isInitialized() {
        return this.configuration != null;
    }

    private DataNucleusApplicationComponents5 createDataNucleusApplicationComponents() {

        val dnSettings = IsisContext.getServiceRegistry().lookupServiceElseFail(DataNucleusSettings.class);
        val datanucleusProps = dnSettings.getAsMap();

        addDataNucleusPropertiesIfRequired(datanucleusProps, configuration);

        val classesToBePersisted = JdoEntityTypeRegistry.current().getEntityTypes();

        return new DataNucleusApplicationComponents5(
                configuration,
                datanucleusProps, 
                classesToBePersisted);
    }

    @Override
    public void catalogNamedQueries(final SpecificationLoader specificationLoader) {
        val classesToBePersisted = JdoEntityTypeRegistry.current().getEntityTypes();
        DataNucleusApplicationComponents5.catalogNamedQueries(classesToBePersisted, specificationLoader);
    }

    private static void addDataNucleusPropertiesIfRequired(Map<String, String> props, IsisConfiguration configuration) {

        String connectionFactoryName = configuration.getPersistor().getDatanucleus().getImpl().getDatanucleus().getConnectionFactoryName();
        if(connectionFactoryName != null) {
            String connectionFactory2Name = configuration.getPersistor().getDatanucleus().getImpl().getDatanucleus().getConnectionFactory2Name();
            IsisConfiguration.Persistor.Datanucleus.Impl.DataNucleus.TransactionTypeEnum transactionType = configuration.getPersistor().getDatanucleus().getImpl().getDatanucleus().getTransactionType();
            // extended logging
            if(transactionType == null) {
                log.info("found config properties to use non-JTA JNDI datasource ({})", connectionFactoryName);
                if(connectionFactory2Name != null) {
                    log.warn("found config properties to use non-JTA JNDI datasource ({}); second '-nontx' JNDI datasource also configured but will not be used ({})", connectionFactoryName, connectionFactory2Name);
                }
            } else {
                log.info("found config properties to use JTA JNDI datasource ({})", connectionFactoryName);
            }
            if(connectionFactory2Name == null) {
                // JDO/DN itself will (probably) throw an exception
                log.error("found config properties to use JTA JNDI datasource ({}) but config properties for second '-nontx' JNDI datasource were *not* found", connectionFactoryName);
            } else {
                log.info("... and config properties for second '-nontx' JNDI datasource also found; {}", connectionFactory2Name);
            }
            // nothing further to do
            return;
        } else {
            // use JDBC connection properties; put if not present

            putIfNotPresent(props, "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            putIfNotPresent(props, "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            putIfNotPresent(props, "javax.jdo.option.ConnectionUserName", "sa");
            putIfNotPresent(props, "javax.jdo.option.ConnectionPassword", "");

            if(log.isInfoEnabled()) {
                log.info("using JDBC connection '{}'", 
                        configuration.getPersistor().getDatanucleus().getImpl().getJavax().getJdo().getOption().getConnectionUrl());
            }
        }
    }

    private static void putIfNotPresent(
            final Map<String, String> props,
            String key,
            String value) {
        if(!props.containsKey(key)) {
            props.put(key, value);
        }
    }


    @Override
    public final void shutdown() {
        if(!isInitialized()) {
            return;
        }
        if(applicationComponents.isMemoized()) {
            applicationComponents.get().shutdown();
            applicationComponents.clear();
        }
        this.configuration = null;
        this.storeLifecycleListener = null;
    }

    /**
     * Called by {@link org.apache.isis.runtime.system.session.IsisSessionFactory#openSession(AuthenticationSession)}.
     */

    @Override
    public PersistenceSession5 createPersistenceSession(
            final AuthenticationSession authenticationSession) {

        Objects.requireNonNull(applicationComponents.get(),
                () -> "PersistenceSession5 requires initialization. "+this.hashCode());

        //[ahuber] if stale force recreate
        guardAgainstStaleState();

        final PersistenceManagerFactory persistenceManagerFactory =
                applicationComponents.get().getPersistenceManagerFactory();

        return new PersistenceSession5(
                authenticationSession, 
                persistenceManagerFactory,
                storeLifecycleListener,
                this);
    }
    
    // [ahuber] JRebel support, not tested at all
    private void guardAgainstStaleState() {
        if(!applicationComponents.isMemoized()) {
            return;
        }
        if(applicationComponents.get().isStale()) {
            try {
                applicationComponents.get().shutdown();
            } catch (Exception e) {
                // ignore
            }
            applicationComponents.clear();
        }
    }


}
