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
package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug._Probe.EntryPoint;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.common.model.decorators.FormLabelDecorator.FormLabelDecorationModel;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FrameFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.RegularFrame;
import org.apache.isis.viewer.wicket.ui.components.scalars.blobclob.IsisBlobOrClobPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.bool.BooleanPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.reference.ReferencePanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices.ValueChoicesSelect2Panel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.Wkt.EventTopic;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;
import org.apache.isis.viewer.wicket.ui.util.WktDecorators;
import org.apache.isis.viewer.wicket.ui.util.WktTooltips;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;


public abstract class ScalarPanelAbstract
extends PanelAbstract<ManagedObject, ScalarModel>
implements ScalarModelSubscriber {

    private static final long serialVersionUID = 1L;

    protected static final String ID_SCALAR_TYPE_CONTAINER = "scalarTypeContainer";

    protected static final String ID_SCALAR_NAME = "scalarName";
    protected static final String ID_SCALAR_VALUE = "scalarValue";

    public enum FormatModifier {
        MARKUP,
        MULITLINE,
        COMPOSITE,
        TRISTATE,
        BLOB,
        BADGE,
        /** render output un-escaped; careful not to allow XSS vulnerabilities*/
        NO_OUTPUT_ESCAPE
    }

    public enum Repaint {
        ENTIRE_FORM,
        PARAM_ONLY,
        NOTHING
    }

    public enum RenderScenario {
        COMPACT,
        /**
         * Is viewing and cannot edit.
         * But there might be associated actions with dialog feature inline-as-if-edit.
         */
        READONLY,
        /**
         * Is viewing and can edit.
         */
        CAN_EDIT,
        CAN_EDIT_INLINE,
        CAN_EDIT_INLINE_VIA_ACTION,
        /**
         * Is editing (either prompt form or other dialog).
         */
        EDITING,
        EDITING_WITH_LINK_TO_NESTED,
        ;

        public boolean isCompact() { return this==COMPACT;}
        public boolean isReadonly() { return this==READONLY;}
        public boolean isCanEdit() { return this==CAN_EDIT;}
        public boolean isEditing() { return this==EDITING;}
        public boolean isEditingAny() {
            return this==EDITING
                    || this==CAN_EDIT_INLINE_VIA_ACTION;}
        public boolean isCanEditAny() {
            return this==CAN_EDIT
                || this==CAN_EDIT_INLINE
                || this==CAN_EDIT_INLINE_VIA_ACTION; }

        static RenderScenario inferFrom(final ScalarPanelAbstract scalarPanel) {
            val scalarModel = scalarPanel.scalarModel();
            if(!scalarModel.getRenderingHint().isRegular()) {
                return COMPACT;
            }
            if(scalarModel.isEditMode()) {
                return
                        _Util.canParameterEnterNestedEdit(scalarModel)
                        ? EDITING_WITH_LINK_TO_NESTED // nested/embedded dialog
                        : EDITING;
            }
            if(_Util.canPropertyEnterInlineEditDirectly(scalarModel)) {
                return CAN_EDIT_INLINE;
            }
            if(_Util.lookupPropertyActionForInlineEdit(scalarModel).isPresent()) {
                return CAN_EDIT_INLINE_VIA_ACTION;
            }
            return scalarModel.isEnabled()
                    ? CAN_EDIT
                    : READONLY;
        }

    }

    // -- CONSTRUCTION

    /**
     * Identical to super.getModel()
     */
    public final ScalarModel scalarModel() {
        return super.getModel();
    }

    @Getter
    private final ImmutableEnumSet<FormatModifier> formatModifiers;
    protected void setupFormatModifiers(final EnumSet<FormatModifier> modifiers) {}

    // -- COMPACT FRAME

    @Getter(AccessLevel.PROTECTED)
    private Component compactFrame;

    /**
     * Builds the component to render the model when in COMPACT form.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected abstract Component createCompactFrame();

    // -- REGULAR FRAME

    @Getter(AccessLevel.PROTECTED)
    private MarkupContainer regularFrame;

    /**
     * Builds the component to render the model when in REGULAR format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected abstract MarkupContainer createRegularFrame();

    // -- INLINE EDIT FORM FRAME

    /**
     * Used by most subclasses
     * ({@link ScalarPanelAbstract}, {@link ReferencePanel}, {@link ValueChoicesSelect2Panel})
     * but not all ({@link IsisBlobOrClobPanelAbstract}, {@link BooleanPanel})
     */
    @Getter(AccessLevel.PROTECTED)
    private WebMarkupContainer formFrame;

    // -- FRAME CONTAINER

    private WebMarkupContainer scalarFrameContainer;
    protected final WebMarkupContainer getScalarFrameContainer() { return scalarFrameContainer; }

    // -- RENDER SCENARIO

    @Getter(AccessLevel.PROTECTED)
    private final RenderScenario renderScenario;

    // -- CONSTRUCTION

    protected ScalarPanelAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);

        val formatModifiers = EnumSet.noneOf(FormatModifier.class);
        setupFormatModifiers(formatModifiers);

        this.formatModifiers = ImmutableEnumSet.from(formatModifiers);
        this.renderScenario = RenderScenario.inferFrom(this);
    }

    // -- INIT

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
        callHooks();
        setOutputMarkupId(true);
    }

    /**
     * determines the CSS that is added to the outermost 'scalarTypeContainer' div.
     */
    public final String getCssClassName() {
        return _Strings.decapitalize(getClass().getSimpleName());
    }

    /**
     * Builds GUI lazily prior to first render.
     *
     * <p>
     * This design allows the panel to be configured first.
     *
     * @see #onBeforeRender()
     */
    private void buildGui() {

        val scalarModel = scalarModel();

        scalarFrameContainer = Wkt.containerAdd(this, ID_SCALAR_TYPE_CONTAINER);
        Wkt.cssAppend(scalarFrameContainer, getCssClassName());

        switch(scalarModel.getRenderingHint()) {
        case REGULAR:
            regularFrame = Wkt.ajaxEnable(createRegularFrame());
            compactFrame = createShallowCompactFrame();
            regularFrame.setVisible(true);
            compactFrame.setVisible(false);

            scalarFrameContainer.addOrReplace(compactFrame, regularFrame,
                    formFrame = createFormFrame());

            val associatedLinksAndLabels = _Util.associatedLinksAndLabels(scalarModel);
            addPositioningCssTo(regularFrame, associatedLinksAndLabels);
            addActionLinksBelowAndRight(regularFrame, associatedLinksAndLabels);

            addFeedbackOnlyTo(regularFrame, getValidationFeedbackReceiver());

            setupInlinePrompt();

            break;
        default:
            regularFrame = createShallowRegularFrame();
            compactFrame = createCompactFrame();
            regularFrame.setVisible(false);
            compactFrame.setVisible(true);

            scalarFrameContainer.addOrReplace(compactFrame, regularFrame,
                    formFrame = createFormFrame());

            break;
        }

        // prevent from tabbing into non-editable widgets.
        if(scalarModel.isProperty()
                && scalarModel.getMode() == ScalarRepresentation.VIEWING
                && (scalarModel.getPromptStyle().isDialogAny()
                        || !scalarModel.canEnterEditMode())) {

            Wkt.noTabbing(getValidationFeedbackReceiver());
        }

        addCssFromMetaModel();

        notifyOnChange(this);
        addFormComponentBehaviourToUpdateSubscribers();
    }

    protected abstract void setupInlinePrompt();

    /**
     * Builds the hidden REGULAR component when in COMPACT format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected MarkupContainer createShallowRegularFrame() {
        val shallowRegularFrame = FrameFragment.REGULAR
                .createComponent(Wkt::container);
        WktComponents.permanentlyHide(shallowRegularFrame,
                ID_SCALAR_NAME, ID_SCALAR_VALUE,
                RegularFrame.FIELD.getContainerId(),
                RegularFrame.FEEDBACK.getContainerId(),
                RegularFrame.ASSOCIATED_ACTION_LINKS_BELOW.getContainerId(),
                RegularFrame.ASSOCIATED_ACTION_LINKS_RIGHT.getContainerId());
        return shallowRegularFrame;
    }

    /**
     * Builds the hidden COMPACT component when in REGULAR format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected Component createShallowCompactFrame() {
        return FrameFragment.COMPACT
                .createComponent(Wkt::container); // empty component;
    }

    /**
     * Builds the component to render the model when in INLINE EDITING FORM format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected WebMarkupContainer createFormFrame() {
        val isRegular = scalarModel().getRenderingHint().isRegular();
        return (WebMarkupContainer)FrameFragment.INLINE_PROMPT_FORM
                .createComponent(WebMarkupContainer::new)
                .setVisible(false)
                .setOutputMarkupId(isRegular);
    }

    // -- FRAME SWITCHING

    protected final void switchRegularFrameToFormFrame() {
        getComponentFactoryRegistry()
                .addOrReplaceComponent(
                    getScalarFrameContainer(),
                    FrameFragment.INLINE_PROMPT_FORM.getContainerId(),
                    ComponentType.PROPERTY_EDIT_FORM,
                    scalarModel());

        getRegularFrame().setVisible(false);
        getFormFrame().setVisible(true);
    }

    // -- HOOKS

    private void callHooks() {

        val scalarModel = scalarModel();

        final String disableReasonIfAny = scalarModel.disableReasonIfAny();
        final boolean mustBeEditable = scalarModel.mustBeEditable();
        if (disableReasonIfAny != null) {
            if(mustBeEditable) {
                onInitializeNotEditable();
            } else {
                onInitializeReadonly(disableReasonIfAny);
            }
        } else {
            if (scalarModel.isViewMode()) {
                onInitializeNotEditable();
            } else {
                onInitializeEditable();
            }
        }
    }

    /**
     * The widget starts off in read-only, but should be possible to activate into edit mode.
     */
    protected void onInitializeNotEditable() {
    }

    /**
     * The widget starts off read-only, and CANNOT be activated into edit mode.
     */
    protected void onInitializeReadonly(final String disableReason) {
    }

    /**
     * The widget starts off immediately editable.
     */
    protected void onInitializeEditable() {
    }

    /**
     * The widget is no longer editable, but should be possible to activate into edit mode.
     */
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
    }

    /**
     * The widget should be made editable.
     *
     */
    protected void onEditable(final @NonNull Optional<AjaxRequestTarget> target) {
    }

    private void addCssFromMetaModel() {
        val scalarModel = scalarModel();

        Wkt.cssAppend(this, scalarModel.getCssClass());

        Facets.cssClass(scalarModel.getMetaModel(), scalarModel.getParentUiModel().getManagedObject())
        .ifPresent(cssClass->
            Wkt.cssAppend(this, cssClass));
    }


    // //////////////////////////////////////

    /**
     * Each component is now responsible for determining if it should be visible or not.
     *
     * <p>
     * Unlike the constructor and <tt>onInitialize</tt>, which are called only once, the <tt>onConfigure</tt> callback
     * is called multiple times, just prior to <tt>onBeforeRendering</tt>.  It is therefore the correct place for
     * components to set up their visibility/usability.
     * </p>
     *
     */
    @Override
    protected void onConfigure() {
        final boolean hidden = scalarModel().whetherHidden();
        setVisibilityAllowed(!hidden);
        super.onConfigure();
    }


    // //////////////////////////////////////


    static class ScalarUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
        private static final long serialVersionUID = 1L;

        private final ScalarPanelAbstract scalarPanel;

        private ScalarUpdatingBehavior(final ScalarPanelAbstract scalarPanel) {
            super("change");
            this.scalarPanel = scalarPanel;
        }

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {

            _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                    + "originating from User either having changed a Property value during inline editing "
                    + "or having changed a Parameter value within an open ActionPrompt.");

            for (ScalarModelSubscriber subscriber : scalarPanel.subscribers) {
                subscriber.onUpdate(target, scalarPanel);
            }
        }

        @Override
        protected void onError(final AjaxRequestTarget target, final RuntimeException e) {
            super.onError(target, e);
            for (ScalarModelSubscriber subscriber : scalarPanel.subscribers) {
                subscriber.onError(target, scalarPanel);
            }
        }
    }

    private final List<ScalarModelSubscriber> subscribers = _Lists.newArrayList();

    public void notifyOnChange(final ScalarModelSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    private void addFormComponentBehaviourToUpdateSubscribers() {
        val validationFeedbackReceiver = getValidationFeedbackReceiver();
        if(validationFeedbackReceiver == null) {
            return;
        }
        for (Behavior b : validationFeedbackReceiver.getBehaviors(ScalarUpdatingBehavior.class)) {
            validationFeedbackReceiver.remove(b);
        }
        validationFeedbackReceiver.add(new ScalarUpdatingBehavior(this));
    }

    // //////////////////////////////////////

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
        if(getModel().isParameter()) {
            Wkt.javaScriptAdd(target, EventTopic.FOCUS_FIRST_PARAMETER, getMarkupId());
        }
    }


    @Override
    public void onError(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
    }

    // ///////////////////////////////////////////////////////////////////

    protected Label createScalarNameLabel(final String id, final IModel<String> labelCaption) {

        final Label scalarNameLabel = Wkt.label(id, labelCaption);
        if(_Strings.isNullOrEmpty(labelCaption.getObject())) {
            return scalarNameLabel;
        }

        val scalarModel = scalarModel();

        WktDecorators.getFormLabel()
            .decorate(scalarNameLabel, FormLabelDecorationModel
                    .mandatory(scalarModel.isRequired()
                            && scalarModel.isEnabled()));

        scalarModel.getDescribedAs()
        .ifPresent(describedAs->WktTooltips.addTooltip(scalarNameLabel, describedAs));
        return scalarNameLabel;
    }

    // ///////////////////////////////////////////////////////////////////

    /**
     * Component to attach feedback to.
     */
    @Nullable
    protected abstract Component getValidationFeedbackReceiver();


    private void addFeedbackOnlyTo(final MarkupContainer markupContainer, final Component component) {
        if(component==null) {
            return;
        }
        markupContainer.addOrReplace(
                RegularFrame.FEEDBACK.createComponent(id->
                    new NotificationPanel(id, component, new ComponentFeedbackMessageFilter(component))));
    }

    private void addActionLinksBelowAndRight(
            final MarkupContainer labelIfRegular,
            final Can<LinkAndLabel> linkAndLabels) {

        val linksBelow = linkAndLabels
                .filter(LinkAndLabel.isPositionedAt(ActionLayout.Position.BELOW));
        AdditionalLinksPanel.addAdditionalLinks(
                labelIfRegular, RegularFrame.ASSOCIATED_ACTION_LINKS_BELOW.getContainerId(),
                linksBelow, AdditionalLinksPanel.Style.INLINE_LIST);

        val linksRight = linkAndLabels
                .filter(LinkAndLabel.isPositionedAt(ActionLayout.Position.RIGHT));
        AdditionalLinksPanel.addAdditionalLinks(
                labelIfRegular, RegularFrame.ASSOCIATED_ACTION_LINKS_RIGHT.getContainerId(),
                linksRight, AdditionalLinksPanel.Style.DROPDOWN);
    }

    /**
     * Applies the {@literal @}{@link LabelAtFacet} and also CSS based on
     * whether any of the associated actions have {@literal @}{@link ActionLayout layout} positioned to
     * the {@link ActionLayout.Position#RIGHT right}.
     *
     * @param markupContainer The form group element
     * @param actionLinks
     */
    private void addPositioningCssTo(
            final MarkupContainer markupContainer,
            final Can<LinkAndLabel> actionLinks) {
        Wkt.cssAppend(markupContainer, determinePropParamLayoutCss(getModel()));
        Wkt.cssAppend(markupContainer, determineActionLayoutPositioningCss(actionLinks));
    }

    private static String determinePropParamLayoutCss(final ScalarModel scalarModel) {
        return Facets.labelAtCss(scalarModel.getMetaModel());
    }

    private static String determineActionLayoutPositioningCss(final Can<LinkAndLabel> entityActionLinks) {
        return entityActionLinks.stream()
                .anyMatch(LinkAndLabel.isPositionedAt(ActionLayout.Position.RIGHT))
                    ? "actions-right"
                    : null;
    }

    // ///////////////////////////////////////////////////////////////////

    /**
     * Repaints this panel of just some of its children
     *
     * @param target The Ajax request handler
     */
    public void repaint(final AjaxRequestTarget target) {
        target.add(this);
    }

    /**
    *
    * @param paramModel - the action being invoked
    * @param target - in case there's more to be repainted...
    *
    * @return - true if changed as a result of these pending arguments.
    */
   public Repaint updateIfNecessary(
           final @NonNull ParameterUiModel paramModel,
           final @NonNull Optional<AjaxRequestTarget> target) {

       val scalarModel = scalarModel();

       // visibility
       val visibilityConsent = paramModel.getParameterNegotiationModel().getVisibilityConsent(paramModel.getParameterIndex());
       val visibilityBefore = isVisible();
       val visibilityAfter = visibilityConsent.isAllowed();
       setVisible(visibilityAfter);

       // usability
       val usabilityConsent = paramModel.getParameterNegotiationModel().getUsabilityConsent(paramModel.getParameterIndex());
       val usabilityBefore = isEnabled();
       val usabilityAfter = usabilityConsent.isAllowed();
       if(usabilityAfter) {
           onEditable(target);
       } else {
           onNotEditable(usabilityConsent.getReason(), target);
       }

       val paramValue = paramModel.getValue();
       val valueChanged = !Objects.equals(scalarModel.getObject(), paramValue);

       if(valueChanged) {
           if(ManagedObjects.isNullOrUnspecifiedOrEmpty(paramValue)) {
               scalarModel.setObject(null);
           } else {
               scalarModel.setObject(paramValue);
           }
       }

       // repaint the entire form if visibility has changed
       if (!visibilityBefore || !visibilityAfter) {
           return Repaint.ENTIRE_FORM;
       }

       // repaint the param if usability has changed
       if (!usabilityAfter || !usabilityBefore) {
           return Repaint.PARAM_ONLY;
       }

       // also repaint the param if its pending arg has changed.
       return valueChanged
               ? Repaint.PARAM_ONLY
               : Repaint.NOTHING;
   }

}
