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
package org.apache.isis.testing.fixtures.applib.fixturescripts;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.testing.fixtures.applib.events.FixturesInstalledEvent;
import org.apache.isis.testing.fixtures.applib.events.FixturesInstallingEvent;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.testing.fixtures.applib.personas.PersonaWithBuilderScript;

import lombok.Getter;
import lombok.Setter;
import lombok.val;


/**
 * Provides the mechanism to execute {@link FixtureScript}s from the UI of
 * a domain app; and can also be used within integration testing.
 *
 * @since 1.x {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW
)
@Named(FixtureScripts.LOGICAL_TYPE_NAME)
@DomainServiceLayout(
        named="Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class FixtureScripts {

    public static final String LOGICAL_TYPE_NAME = IsisModuleTestingFixturesApplib.NAMESPACE + ".FixtureScripts"; // secman seeding

    @Inject private TitleService titleService;
    @Inject private JaxbService jaxbService;
    @Inject private ServiceInjector serviceInjector;
    @Inject private RepositoryService repositoryService;
    @Inject private TransactionService transactionService;
    @Inject private ExecutionParametersService executionParametersService;
    @Inject private InteractionService interactionService;

    @Inject private EventBusService eventBusService;


    // -- Specification, nonPersistedObjectsStrategy, multipleExecutionStrategy enums

    /**
     * How to handle objects that are to be
     * {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added}
     * into a {@link FixtureResult} but which are not yet persisted.
     *
     * @since 1.x {@index}
     */
    public enum NonPersistedObjectsStrategy {
        PERSIST,
        IGNORE
    }

    /**
     * How to handle fixture scripts that are submitted to be executed more than once.
     *
     * <p>
     *     Note that this is a {@link FixtureScripts#getMultipleExecutionStrategy() global setting} of the
     *     {@link FixtureScripts} service; there isn't (currently) any way to mix-and-match fixture scripts that are
     *     written with differing semantics in mind.  Ideally it should be the responsibility of the fixture script
     *     itself to determine whether it should be run.  As a partial solution to this, the
     * </p>
     *
     * @since 1.x {@index}
     */
    public enum MultipleExecutionStrategy {
        /**
         * Any given fixture script (or more precisely, any fixture script instance for a particular fixture script
         * class) can only be run once.
         *
         * <p>
         *     This strategy represents the original design of fixture scripts service.  Specifically, it allows an
         *     arbitrary graph of fixture scripts (eg A -> B -> C, A -> B -> D, A -> C -> D) to be created each
         *     specifying its dependencies, and without having to worry or co-ordinate whether those prerequisite
         *     fixture scripts have already been run.
         * </p>
         * <p>
         *     The most obvious example is a global teardown script; every fixture script can require this to be
         *     called, but it will only be run once.
         * </p>
         * <p>
         *     Note that this strategy treats fixture scripts as combining both the 'how' (which business action(s) to
         *     call) and the also the 'what' (what the arguments are to those actions).
         * </p>
         */
        EXECUTE_ONCE_BY_CLASS,
        /**
         * Any given fixture script can only be run once, where the check to determine if a fixture script has already
         * been run is performed using value semantics.
         *
         * <p>
         *     This strategy is a half-way house between the {@link #EXECUTE_ONCE_BY_VALUE} and {@link #EXECUTE}
         *     strategies, where we want to prevent a fixture from running more than once, where by "fixture" we mean
         *     the 'what' - the data to be loaded up; the 'how' is unimportant.
         * </p>
         *
         * <p>
         *     This strategy was introduced in order to better support the <tt>ExcelFixture</tt> fixture script
         *     (provided by the (non-ASF) Isis Addons'
         *     <a href="https://github.com/isisaddons/isis-module-excel">Excel module</a>.  The <tt>ExcelFixture</tt>
         *     takes an Excel spreadsheet as the 'what' and loads up each row.  So the 'how' is re-usable (therefore
         *     the {@link #EXECUTE_ONCE_BY_CLASS} doesn't apply) on the other hand we don't want the 'what' to be
         *     loaded more than once (so the {@link #EXECUTE} strategy doesn't apply either).  The solution is for
         *     <tt>ExcelFixture</tt> to have value semantics (a digest of the spreadsheet argument).
         * </p>
         */
        EXECUTE_ONCE_BY_VALUE,
        /**
         * Allow fixture scripts to run as requested.
         *
         * <p>
         *     This strategy is conceptually the simplest; all fixtures are run as requested.  However, it is then
         *     the responsibility of the programmer to ensure that fixtures do not interfere with each other.  For
         *     example, if fixture A calls fixture B which calls teardown, and fixture A also calls fixture C that
         *     itself calls teardown, then fixture B's setup will get removed.
         * </p>
         * <p>
         *     The workaround to the teardown issue is of course to call the teardown fixture only once in the test
         *     itself; however even then this strategy cannot cope with arbitrary graphs of fixtures.  The solution
         *     is for the fixture list to be flat, one level high.
         * </p>
         */
        EXECUTE;

        public boolean isExecuteOnceByClass() {
            return this == EXECUTE_ONCE_BY_CLASS;
        }
        public boolean isExecuteOnceByValue() {
            return this == EXECUTE_ONCE_BY_VALUE;
        }
        public boolean isExecute() {
            return this == EXECUTE;
        }
    }

    // -- constructors

    // -- constructor, init

    /**
     * The package prefix to search for fixture scripts.  This default value will result in
     * no fixture scripts being found.  However, normally it will be overridden.
     */
    public static final String PACKAGE_PREFIX = FixtureScripts.class.getPackage().getName();


    /**
     * Used to configure the UI menu actions, namely {@link #runFixtureScript(String, String)} and
     * {@link #recreateObjectsAndReturnFirst()}.
     *
     * <p>
     *     May be <code>null</code> if no {@link FixtureScriptsSpecificationProvider} was provided either explicitly
     *     or implicitly by way of configuring the <code>isis.testing.fixtures.fixture-scripts-specification</code>
     *     configuration properties.
     * </p>
     *
     * @see #getFixtureScriptByFriendlyName()
     * @see #getMultipleExecutionStrategy()
     * @see #getNonPersistedObjectsStrategy()
     */
    @Getter
    private final FixtureScriptsSpecification specification;

    /**
     * Global setting as to how to handle returned entities from fixture scripts that
     * are still transient (not yet persisted).
     *
     * <p>
     *     Will be <code>null</code> if there is no {@link #getSpecification()}.
     * </p>
     *
     * @see #getSpecification()
     */
    @Getter(onMethod_ = {@Programmatic})
    private final NonPersistedObjectsStrategy nonPersistedObjectsStrategy;

    /**
     * Global setting as to how to handle fixture scripts that are executed more than once.  See
     * {@link MultipleExecutionStrategy} for more details.
     *
     * <p>
     *     Will be <code>null</code> if there is no {@link #getSpecification()}.
     * </p>
     *
     * @see #getSpecification()
     */
    @Getter(onMethod_ = {@Programmatic})
    private final MultipleExecutionStrategy multipleExecutionStrategy;

    /**
     * Maps all discovered {@link FixtureScript}s to a friendly name for display in the UI (that is, for the
     * {@link #runFixtureScript(String, String)} menu action parameters).
     *
     * <p>
     *     Will be <code>null</code> if there is no {@link #getSpecification()}.
     * </p>
     *
     * @see #getSpecification()
     */
    @Getter
    private final SortedMap<String,FixtureScript> fixtureScriptByFriendlyName;


    @Inject
    public FixtureScripts(
            final FixtureScriptsSpecificationProvider fixtureScriptsSpecificationProvider,
            final ServiceRegistry serviceRegistry) {

        this.specification = fixtureScriptsSpecificationProvider.getSpecification();
        this.nonPersistedObjectsStrategy = specification.getNonPersistedObjectsStrategy();
        this.multipleExecutionStrategy = specification.getMultipleExecutionStrategy();

        val packagePrefix = specification.getPackagePrefix();
        this.fixtureScriptByFriendlyName =
                packagePrefix != null
                    ? serviceRegistry.select(FixtureScript.class).stream()
                        .filter(Objects::nonNull)
                        .filter(fixtureScript -> fixtureScript.getClass().getPackage().getName().startsWith(packagePrefix))
                        .collect(Collectors.toMap(FixtureScript::getFriendlyName, Function.identity(),
                                (v1, v2) -> {
                                    throw new RuntimeException(String.format("Two FixtureScript's have the same friendly name '%s", v1));
                                },
                                TreeMap::new))
                    : _Maps.newTreeMap();
    }




    // -- runFixtureScript (using choices as the drop-down policy)

    /**
     * Main action - as exposed in the UI - to execute the specified fixture script.
     *
     * <p>
     *     Also allows arbitrary parameters to be specified for said fixture script.
     * </p>
     *
     * @param fixtureScriptName
     * @param parameters
     */
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa="fa fa-chevron-right",
            sequence="10")
    public List<FixtureResult> runFixtureScript(
            @ParameterLayout(named = "Fixture script")
            final String fixtureScriptName,
            @ParameterLayout(
                    named = "Parameters",
                    describedAs =
                            "Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)",
                    multiLine = 10)
            @Parameter(optionality = Optionality.OPTIONAL)
            final String parameters) {
        final FixtureScript fixtureScript = fixtureScriptByFriendlyName.get(fixtureScriptName);
        return runFixtureScript(fixtureScript, parameters);
    }

    @Programmatic
    public List<FixtureResult> runFixtureScript(
            final FixtureScript fixtureScript,
            final String parameters) {
        try {
            eventBusService.post(new FixturesInstallingEvent(this));

            // the caller may have simply new'd up the fixture script.  To allow this use case, we need to ensure that
            // domain services are injected into the fixture script.
            serviceInjector.injectServicesInto(fixtureScript);

            return fixtureScript.run(parameters, this);
        } finally {
            eventBusService.post(new FixturesInstalledEvent(this));
        }
    }
    @MemberSupport public boolean hideRunFixtureScript() {
        return specification == null;
    }
    @MemberSupport public String disableRunFixtureScript() {
        return getFixtureScriptByFriendlyName().isEmpty()
                ? String.format("No fixture scripts found under package '%s'", specification.getPackagePrefix())
                : null;
    }
    @MemberSupport public String default0RunFixtureScript() {
        val defaultFixtureScript = defaultFromFixtureScriptsSpecification();
        if(defaultFixtureScript != null) {
            return defaultFixtureScript;
        }
        val choices = choices0RunFixtureScript();
        return choices.size() == 1
                ? choices.iterator().next()
                : null;
    }
    @Domain.Exclude private String defaultFromFixtureScriptsSpecification() {
        Class<? extends FixtureScript> defaultScript = specification.getRunScriptDefaultScriptClass();
        return defaultScript != null
                ? findFixtureScriptNameFor(defaultScript)
                : null;
    }
    @MemberSupport public Set<String> choices0RunFixtureScript() {
        return fixtureScriptByFriendlyName.keySet();
    }
    @MemberSupport public String validateRunFixtureScript(final String fixtureScriptName, final String parameters) {
        return fixtureScriptByFriendlyName.get(fixtureScriptName).validateRun(parameters);
    }

    protected List<FixtureResult> runScript(final String fixtureScriptName, final String parameters) {
        final FixtureScript fixtureScript = fixtureScriptByFriendlyName.get(fixtureScriptName);
        return runScript(fixtureScript, parameters);
    }

    protected List<FixtureResult> runScript(final FixtureScript fixtureScript, final String parameters) {
        serviceInjector.injectServicesInto(fixtureScript);
        return fixtureScript.run(parameters, this);
    }





    // -- recreateObjectsAndReturnFirst

    /**
     * Convenience action - exposed through the UI - to execute the specified
     * &quot;recreate&quot; {@link FixtureScript fixture script} and
     * return/show the first object returned by that fixture script.
     */
    @Action(
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa="fa fa-sync",
            sequence="20")
    public Object recreateObjectsAndReturnFirst() {
        val recreateScriptClass =  getSpecification().getRecreateScriptClass();
        val recreateScript = findFixtureScriptNameFor(recreateScriptClass);
        if(recreateScript == null) {
            return null;
        }
        final List<FixtureResult> results = runScript(recreateScript, null);
        if(results.isEmpty()) {
            return null;
        }
        return results.get(0).getObject();
    }
    @MemberSupport public boolean hideRecreateObjectsAndReturnFirst() {
        return specification == null || specification.getRecreateScriptClass() == null;
    }




    // -- programmatic API

    @Programmatic
    public void run(final FixtureScript... fixtureScriptList) {

    	val singleScript = toSingleScript(fixtureScriptList);
    	String parameters = null;

    	interactionService.runAnonymous(()->{
    	    transactionService.runWithinCurrentTransactionElseCreateNew(()->{
                runScript(singleScript, parameters);
            });
    	});

    }

    @SafeVarargs
    @Programmatic
    public final void runPersonas(final PersonaWithBuilderScript<?,? extends BuilderScriptAbstract<?>> ... personaScripts) {
        for (val personaWithBuilderScript : personaScripts) {

            val script = _Casts.<PersonaWithBuilderScript<Object,BuilderScriptAbstract<Object>>>
                uncheckedCast(personaWithBuilderScript);

            runPersona(script);
        }
    }

    @Programmatic
    public <T> T runPersona(final PersonaWithBuilderScript<T,? extends BuilderScriptAbstract<? extends T>> persona) {
        val fixtureScript = persona.builder();
        return runBuilder(fixtureScript);
    }

    /**
     * Runs the builderScript within its own transactional boundary.
     * @param <T>
     * @param builderScript
     */
    @Programmatic
    public <T> T runBuilder(final BuilderScriptAbstract<T> builderScript) {

        return interactionService.callAnonymous(()->
            transactionService.callWithinCurrentTransactionElseCreateNew(()->
                runBuilderScriptNonTransactional(builderScript)
            )
        )
        .ifFailureFail()
        .getValue().orElse(null);
    }

    /**
     * Runs the builderScript without its own transactional boundary.<br>
     * The caller is responsible to provide a transactional context/boundary.
     * @param <T>
     * @param builderScript
     */
    @Programmatic
    public <T> T runBuilderScriptNonTransactional(final BuilderScriptAbstract<T> builderScript) {

        serviceInjector.injectServicesInto(builderScript);

        builderScript.run(null, this);
        final T object = builderScript.getObject();
        return object;
    }

    @Programmatic
    public String findFixtureScriptNameFor(final Class<? extends FixtureScript> fixtureScriptClass) {
        val fixtureScripts = getFixtureScriptByFriendlyName().entrySet();
        for (final Map.Entry<String,FixtureScript> fs : fixtureScripts) {
            if(fixtureScriptClass.isAssignableFrom(fs.getValue().getClass())) {
                return fs.getKey();
            }
        }
        return null;
    }

    @Programmatic
    public FixtureScript.ExecutionContext newExecutionContext(final String parameters) {
        val executionParameters = executionParametersService.newExecutionParameters(parameters);
        return FixtureScript.ExecutionContext.create(executionParameters, this);
    }



    // -- memento support for FixtureScript

    @XmlRootElement(name = "fixtureScriptMemento")
    public static class FixtureScriptMemento {
        @Getter @Setter
        private String path;
    }

    String mementoFor(final FixtureScript fs) {
        final FixtureScriptMemento memento = new FixtureScriptMemento();
        memento.setPath(fs.getParentPath());
        return jaxbService.toXml(memento);
    }

    void initOf(final String xml, final FixtureScript fs) {
        final FixtureScriptMemento memento = jaxbService.fromXml(FixtureScriptMemento.class, xml);
        fs.setParentPath(memento.getPath());
    }

    // -- helpers (package level)

    @Programmatic
    FixtureResult newFixtureResult(final FixtureScript script, final String subkey, final Object object, final boolean firstTime) {
        if(object == null) {
            return null;
        }

        if (object instanceof ViewModel
                || repositoryService.getEntityState(object).isAttachedOrRemoved()) {
            // continue
        } else {
            switch(getNonPersistedObjectsStrategy()) {
            case PERSIST:
                transactionService.flushTransaction();
                break;
            case IGNORE:
                return null;
            default:
                throw _Exceptions.unmatchedCase(getNonPersistedObjectsStrategy());
            }
        }
        final FixtureResult fixtureResult = serviceInjector.injectServicesInto(
                                                                new FixtureResult());
        fixtureResult.setFixtureScriptClassName(firstTime ? script.getClass().getName() : null);
        fixtureResult.setFixtureScriptQualifiedName(script.getQualifiedName());
        fixtureResult.setKey(script.pathWith(subkey));
        fixtureResult.setObject(object);
        return fixtureResult;
    }

    @Programmatic
    String titleOf(final FixtureResult fixtureResult) {
        final Object object = fixtureResult.getObject();
        return object != null? titleService.titleOf(object): "(null)";
    }

    // -- HELPERS - LOCAL

    private static FixtureScript toSingleScript(final FixtureScript[] fixtureScriptList) {

        if (fixtureScriptList.length == 1) {
            return fixtureScriptList[0];
        }

        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                for (FixtureScript fixtureScript : fixtureScriptList) {
                    executionContext.executeChild(this, fixtureScript);
                }
            }
        };

    }


}
