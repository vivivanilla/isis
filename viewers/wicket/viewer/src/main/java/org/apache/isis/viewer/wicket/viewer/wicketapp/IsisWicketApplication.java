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
package org.apache.isis.viewer.wicket.viewer.wicketapp;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authentication.strategy.DefaultAuthenticationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.markup.head.ResourceAggregator;
import org.apache.wicket.markup.head.filter.JavaScriptFilteredIntoFooterHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.settings.RequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import org.apache.isis.commons.internal.concurrent._ConcurrentContext;
import org.apache.isis.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;
import org.apache.isis.viewer.wicket.model.isis.WicketApplicationInitializer;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.accmngt.AccountConfirmationMap;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketLogoutPage;
import org.apache.isis.viewer.wicket.viewer.integration.AuthenticatedWebSessionForIsis;
import org.apache.isis.viewer.wicket.viewer.integration.ConverterForObjectAdapter;
import org.apache.isis.viewer.wicket.viewer.integration.ConverterForObjectAdapterMemento;
import org.apache.isis.viewer.wicket.viewer.integration.IsisResourceSettings;
import org.apache.isis.viewer.wicket.viewer.integration.WebRequestCycleForIsis;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Main application, subclassing the Wicket {@link Application} and
 * bootstrapping Isis.
 *
 * <p>
 * Its main responsibility is to allow the set of {@link ComponentFactory}s used
 * to render the domain objects to be registered. This type of customization is
 * common place. At a more fundamental level, also allows the {@link Page}
 * implementation for each {@link PageType page type} to be overridden. This is
 * probably less common, because CSS can also be used for this purpose.
 *
 * <p>
 * New {@link ComponentFactory}s can be specified in two ways. The preferred
 * approach is to have the IoC container discover the ComponentFactory.
 * See <tt>asciidoc</tt> extension for an example of this.
 *
 */
@Log4j2
public class IsisWicketApplication
extends AuthenticatedWebApplication
implements
    ComponentFactoryRegistryAccessor,
    PageClassRegistryAccessor,
    WicketViewerSettingsAccessor,
    HasCommonContext {

    private static final long serialVersionUID = 1L;

    /**
     * Convenience locator, down-casts inherited functionality.
     */
    public static IsisWicketApplication get() {
        return (IsisWicketApplication) AuthenticatedWebApplication.get();
    }

    @Getter(onMethod = @__(@Override))
    private IsisAppCommonContext commonContext;

    @Inject private MetaModelContext metaModelContext;
    @Inject private List<WicketApplicationInitializer> applicationInitializers;
    @Inject private IsisSystemEnvironment systemEnvironment;
    @Inject private IsisConfiguration configuration;

    @Getter(onMethod = @__(@Override))
    @Inject private ComponentFactoryRegistry componentFactoryRegistry;

    @Getter(onMethod = @__(@Override))
    @Inject private PageClassRegistry pageClassRegistry;

    @Getter(onMethod = @__(@Override))
    @Inject private WicketViewerSettings settings;

    // -- CONSTRUCTION

    public IsisWicketApplication() {
    }

    /**
     * Although there are warnings about not overriding this method, it doesn't seem possible
     * to call {@link #setResourceSettings(org.apache.wicket.settings.ResourceSettings)} in the
     * {@link #init()} method.
     */
    @Override
    protected void internalInit() {
        // replace with custom implementation of ResourceSettings that changes the order
        // in which search for i18n properties, to search for the application-specific
        // settings before any other.
        setResourceSettings(new IsisResourceSettings(this));

        super.internalInit();

        // intercept AJAX requests and reload view-models so any detached entities are re-fetched
        IsisWicketAjaxRequestListenerUtil.setRootRequestMapper(this, commonContext);
    }

    private AjaxRequestTarget decorate(final AjaxRequestTarget ajaxRequestTarget) {
        ajaxRequestTarget.registerRespondListener(
                commonContext.injectServicesInto(
                        new TargetRespondListenerToResetQueryResultCache() ));
        return ajaxRequestTarget;
    }

    @Override
    public Application setAjaxRequestTargetProvider(final Function<Page, AjaxRequestTarget> ajaxRequestTargetProvider) {
        final Application application = super.setAjaxRequestTargetProvider(
                (final Page context) -> decorate(ajaxRequestTargetProvider.apply(context)) );
        return application;
    }

    /**
     * Initializes the application; in particular, bootstrapping the Isis
     * backend, and initializing the {@link ComponentFactoryRegistry} to be used
     * for rendering.
     */
    @Override
    protected void init() {
        super.init();

        // initialize Spring Dependency Injection for wicket
        val springInjector = new SpringComponentInjector(this);
        getComponentInstantiationListeners().add(springInjector);
        // resolve injection-points on self
        springInjector.inject(this);

        this.commonContext = IsisAppCommonContext.of(metaModelContext);

        // gather configuration plugins into a list of named tasks
        val initializationTasks =
                _ConcurrentTaskList.named("Isis Application Initialization Tasks");
        applicationInitializers
            .forEach(initializer->initializationTasks
                    .addRunnable(String
                            .format("Configure %s",
                                    initializer.getClass().getSimpleName()),
                            ()->initializer.init(this)));

        try {

            initializationTasks
                // unfortunately must run on same thread that provides Application.get()
                .submit(_ConcurrentContext.sequential())
                .await();

            getRequestCycleSettings().setRenderStrategy(RequestCycleSettings.RenderStrategy.REDIRECT_TO_RENDER);
            getResourceSettings().setParentFolderPlaceholder("$up$");

            getRequestCycleListeners().add(createWebRequestCycleListenerForIsis());
            getRequestCycleListeners().add(new PageRequestHandlerTracker());

            //XXX ISIS-2530, don't recreate expired pages
            getPageSettings().setRecreateBookmarkablePagesAfterExpiry(false);
            getMarkupSettings().setStripWicketTags(configuration.getViewer().getWicket().isStripWicketTags());

            configureSecurity(configuration);

            filterJavascriptContributions();

            mountPages();

            log.debug("storeSettings.asynchronousQueueCapacity: {}", getStoreSettings().getAsynchronousQueueCapacity());
            log.debug("storeSettings.maxSizePerSession        : {}", getStoreSettings().getMaxSizePerSession());
            log.debug("storeSettings.fileStoreFolder          : {}", getStoreSettings().getFileStoreFolder());

        } catch(RuntimeException ex) {
            // because Wicket's handling in its WicketFilter (that calls this method) does not log the exception.
            log.error("Failed to initialize", ex);
            throw ex;
        }

    }

    /*
     * @since 2.0 ... overrides the default, to handle special cases when recreating bookmarked pages
     */
    @Override
    protected IPageFactory newPageFactory() {
        return new _PageFactory(this, super.newPageFactory());
    }

    /*
     * @since 2.0 ... overrides the default, to 'inject' the commonContext into new sessions
     */
    @Override
    public Session newSession(final Request request, final Response response) {
        val newSession = (AuthenticatedWebSessionForIsis) super.newSession(request, response);
        newSession.init(getCommonContext());
        return newSession;
    }

    /**
     * protected visibility to allow ad-hoc overriding of some other authentication strategy.
     */
    protected void configureSecurity(final IsisConfiguration configuration) {
        // since Wicket 9, CSP is enabled by default [https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP]
        getCspSettings().blocking().disabled();
        getSecuritySettings().setAuthenticationStrategy(newAuthenticationStrategy(configuration));

        // TODO ISIS-987 Either make the API better (no direct access to the map) or use DB records
        final int maxEntries = 1000;
        setMetaData(AccountConfirmationMap.KEY, new AccountConfirmationMap(maxEntries, Duration.ofDays(1)));
    }

    /**
     * protected visibility to allow ad-hoc overriding of some other authentication strategy.
     */
    protected IAuthenticationStrategy newAuthenticationStrategy(final IsisConfiguration configuration) {
        val rememberMe = configuration.getViewer().getWicket().getRememberMe();
        val cookieKey = rememberMe.getCookieKey();
        val encryptionKey = rememberMe.getEncryptionKey().orElse(defaultEncryptionKey());
        return new DefaultAuthenticationStrategy(cookieKey, _CryptFactory.sunJceCrypt(encryptionKey));
    }

    /**
     * As called by {@link #newAuthenticationStrategy(IsisConfiguration)}.
     * If an encryption key for the 'rememberMe' cookie hasn't been configured,
     * then use a different encryption key for the 'rememberMe'
     * cookie each time the app is restarted.
     */
    protected String defaultEncryptionKey() {
        return systemEnvironment.isPrototyping()
                ? _CryptFactory.FIXED_SALT_FOR_PROTOTYPING
                : UUID.randomUUID().toString();
    }

    // //////////////////////////////////////

    /**
     * Factored out for easy (informal) pluggability.
     */
    protected IRequestCycleListener createWebRequestCycleListenerForIsis() {
        val webRequestCycleForIsis = new WebRequestCycleForIsis();
        webRequestCycleForIsis.setPageClassRegistry(getPageClassRegistry());
        return webRequestCycleForIsis;
    }


    protected static final Function<ComponentFactory, Iterable<CssResourceReference>> getCssResourceReferences =
            (final ComponentFactory input) -> {
                final CssResourceReference cssResourceReference = input.getCssResourceReference();
                return cssResourceReference != null
                        ? Collections.singletonList(cssResourceReference)
                        : Collections.<CssResourceReference>emptyList();
            };

    // //////////////////////////////////////

    /**
     * Collects JavaScript header contributions for rendering to bottom of page.
     * <p>
     * Factored out for easy (informal) pluggability.
     */
    protected void filterJavascriptContributions() {
        getHeaderResponseDecorators().replaceAll(response ->
            new ResourceAggregator(new JavaScriptFilteredIntoFooterHeaderResponse(response, "footerJS")));
    }

    // //////////////////////////////////////

    /**
     * Map entity and action to provide prettier URLs.
     * <p>
     * Factored out for easy (informal) pluggability.
     */
    protected void mountPages() {
        mountPage("/signin", PageType.SIGN_IN);
        mountPage("/signup", PageType.SIGN_UP);
        mountPage("/signup/verify", PageType.SIGN_UP_VERIFY);
        mountPage("/password/reset", PageType.PASSWORD_RESET);
        mountPage("/entity/#{objectOid}", PageType.ENTITY);
        mountPage("/logout", WicketLogoutPage.class);
    }

    protected void mountPage(final String mountPath, final PageType pageType) {
        final Class<? extends Page> pageClass = this.pageClassRegistry.getPageClass(pageType);
        mount(new MountedMapper(mountPath, pageClass));
    }

    @Override
    public final RuntimeConfigurationType getConfigurationType() {

        if(systemEnvironment==null) {
            return RuntimeConfigurationType.DEPLOYMENT;
        }

        return systemEnvironment.isPrototyping()
                ? RuntimeConfigurationType.DEVELOPMENT
                : RuntimeConfigurationType.DEPLOYMENT;
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
        } catch(final RuntimeException ex) {
            // symmetry with #init()
            log.error("Failed to destroy", ex);
            throw ex;
        }
    }

    // -- WICKET HOOKS

    /**
     * Installs a {@link AuthenticatedWebSessionForIsis custom implementation}
     * of Wicket's own {@link AuthenticatedWebSession}, effectively associating
     * the Wicket session with the Isis's equivalent session object.
     *
     * <p>
     * In general, it shouldn't be necessary to override this method.
     */
    @Override
    protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
        return AuthenticatedWebSessionForIsis.class;
    }

    /**
     * Installs a {@link ConverterLocator} preconfigured with a number of
     * implementations to support Isis specific objects.
     */
    @Override
    protected IConverterLocator newConverterLocator() {
        final ConverterLocator converterLocator = new ConverterLocator();
        converterLocator.set(ManagedObject.class, new ConverterForObjectAdapter());
        converterLocator.set(ObjectMemento.class, new ConverterForObjectAdapterMemento(commonContext));
        return converterLocator;
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @Override
    public Class<? extends Page> getHomePage() {
        return getPageClassRegistry().getPageClass(PageType.HOME);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends WebPage> getSignInPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.SIGN_IN);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends WebPage> getSignUpPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.SIGN_UP);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends WebPage> getSignUpVerifyPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.SIGN_UP_VERIFY);
    }

    /**
     * Delegates to the {@link #getPageClassRegistry() PageClassRegistry}.
     */
    @SuppressWarnings("unchecked")
    public Class<? extends WebPage> getForgotPasswordPageClass() {
        return (Class<? extends WebPage>) getPageClassRegistry().getPageClass(PageType.PASSWORD_RESET);
    }

}
