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
package org.apache.isis.extensions.commandlog.applib.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Domain;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandOutcomeHandler;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.util.BigDecimalUtils;
import org.apache.isis.extensions.commandlog.applib.util.StringUtils;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.MapDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

/**
 * A persistent representation of a {@link Command}.
 *
 * <p>
 *     Use cases requiring persistence including auditing, and for replay of
 *     commands for regression testing purposes.
 * </p>
 *
 * @since 2.0 {@index}
 */
@DomainObject(
        editing = Editing.DISABLED,
        logicalTypeName = PublishedCommand.LOGICAL_TYPE_NAME
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT,
        cssClassUiEvent = PublishedCommand.CssClassUiEvent.class,
        iconUiEvent = PublishedCommand.IconUiEvent.class,
        layoutUiEvent = PublishedCommand.LayoutUiEvent.class,
        titleUiEvent = PublishedCommand.TitleUiEvent.class
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class PublishedCommand implements DomainChangeRecord {

    public final static String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogApplib.NAMESPACE + ".PublishedCommand";

    public static final String NAMED_QUERY_FIND_BY_INTERACTION_ID = "PublishedCommand.findByInteractionId";
    public static final String NAMED_QUERY_FIND_BY_PARENT = "PublishedCommand.findByParent";
    public static final String NAMED_QUERY_FIND_CURRENT = "PublishedCommand.findCurrent";
    public static final String NAMED_QUERY_FIND_COMPLETED = "PublishedCommand.findCompleted";
    public static final String NAMED_QUERY_FIND_RECENT_BY_TARGET = "PublishedCommand.findRecentByTarget";
    public static final String NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BETWEEEN = "PublishedCommand.findByTargetAndTimestampBetween";
    public static final String NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_AFTER = "PublishedCommand.findByTargetAndTimestampAfter";
    public static final String NAMED_QUERY_FIND_BY_TARGET_AND_TIMESTAMP_BEFORE = "PublishedCommand.findByTargetAndTimestampBefore";
    public static final String NAMED_QUERY_FIND_BY_TARGET = "PublishedCommand.findByTarget";
    public static final String NAMED_QUERY_FIND_BY_TIMESTAMP_BETWEEN = "PublishedCommand.findByTimestampBetween";
    public static final String NAMED_QUERY_FIND_BY_TIMESTAMP_AFTER = "PublishedCommand.findByTimestampAfter";
    public static final String NAMED_QUERY_FIND_BY_TIMESTAMP_BEFORE = "PublishedCommand.findByTimestampBefore";
    public static final String NAMED_QUERY_FIND = "PublishedCommand.find";
    public static final String NAMED_QUERY_FIND_RECENT_BY_USERNAME = "PublishedCommand.findRecentByUsername";
    public static final String NAMED_QUERY_FIND_FIRST = "PublishedCommand.findFirst";
    public static final String NAMED_QUERY_FIND_SINCE = "PublishedCommand.findSince";
    /**
     * most recent (replayed) command previously replicated from primary to secondary.
     *
     * This should always exist except for the very first times (after restored the prod DB to secondary).
     */
    public static final String NAMED_QUERY_FIND_MOST_RECENT_REPLAYED = "PublishedCommand.findMostRecentReplayed";
    /**
     *  the most recent completed command, as queried on the secondary, corresponding to the
     *  last command run on primary before the production database was restored to the secondary
     */
    public static final String NAMED_QUERY_FIND_MOST_RECENT_COMPLETED = "PublishedCommand.findMostRecentCompleted";
    public static final String NAMED_QUERY_FIND_NOT_YET_REPLAYED = "PublishedCommand.findNotYetReplayed";
    // public static final String NAMED_QUERY_FIND_REPLAYABLE_MOST_RECENT_STARTED = "PublishedCommand.findReplayableMostRecentStarted";


    // EVENTS

    public static class TitleUiEvent extends IsisModuleExtCommandLogApplib.TitleUiEvent<PublishedCommand> { }
    public static class IconUiEvent extends IsisModuleExtCommandLogApplib.IconUiEvent<PublishedCommand> { }
    public static class CssClassUiEvent extends IsisModuleExtCommandLogApplib.CssClassUiEvent<PublishedCommand> { }
    public static class LayoutUiEvent extends IsisModuleExtCommandLogApplib.LayoutUiEvent<PublishedCommand> { }

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtCommandLogApplib.PropertyDomainEvent<PublishedCommand, T> { }
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtCommandLogApplib.CollectionDomainEvent<PublishedCommand, T> { }



    // CONSTRUCTORS

    /**
     * Intended for use on primary system.
     *
     * @param command
     */
    public PublishedCommand(final Command command) {

        setInteractionId(command.getInteractionId());
        setUsername(command.getUsername());
        setTimestamp(command.getTimestamp());

        setCommandDto(command.getCommandDto());
        setTarget(command.getTarget());
        setLogicalMemberIdentifier(command.getLogicalMemberIdentifier());

        setStartedAt(command.getStartedAt());
        setCompletedAt(command.getCompletedAt());

        setResult(command.getResult());
        setException(command.getException());

        setReplayState(ReplayState.UNDEFINED);
    }


    /**
     * Intended for use on secondary (replay) system.
     *
     * @param commandDto - obtained from the primary system as a representation of a command invocation
     * @param replayState - controls whether this is to be replayed
     * @param targetIndex - if the command represents a bulk action, then it is flattened out when replayed; this indicates which target to execute against.
     */
    public PublishedCommand(
            final CommandDto commandDto,
            final ReplayState replayState,
            final int targetIndex) {

        setInteractionId(UUID.fromString(commandDto.getInteractionId()));
        setUsername(commandDto.getUser());
        setTimestamp(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimestamp()));

        setCommandDto(commandDto);
        setTarget(Bookmark.forOidDto(commandDto.getTargets().getOid().get(targetIndex)));
        setLogicalMemberIdentifier(commandDto.getMember().getLogicalMemberIdentifier());

        // the hierarchy of commands calling other commands is only available on the primary system, and is
        setParent(null);

        setStartedAt(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimings().getStartedAt()));
        setCompletedAt(JavaSqlXMLGregorianCalendarMarshalling.toTimestamp(commandDto.getTimings().getCompletedAt()));

        copyOver(commandDto, UserDataKeys.RESULT, value -> this.setResult(Bookmark.parse(value).orElse(null)));
        copyOver(commandDto, UserDataKeys.EXCEPTION, this::setException);

        setReplayState(replayState);
    }

    static void copyOver(
            final CommandDto commandDto,
            final String key, final Consumer<String> consume) {
        commandDto.getUserData().getEntry()
                .stream()
                .filter(x -> Objects.equals(x.getKey(), key))
                .map(MapDto.Entry::getValue)
                .filter(Objects::nonNull)
                .filter(x -> x.length() > 0)
                .findFirst()
                .ifPresent(consume);
    }


    // TITLE

    @Component
    @javax.annotation.Priority(PriorityPrecedence.LATE)
    public static class TitleProvider {

        @EventListener(TitleUiEvent.class)
        public void on(final @NonNull TitleUiEvent ev) {
            if(!Objects.equals(ev.getTitle(), "Command Jdo") || ev.getTranslatableTitle() != null) {
                return;
            }
            ev.setTitle(title(ev.getSource()));
        }

        private static String title(final @Nullable PublishedCommand source) {
            if(source == null) {
                return "???"; // shouldn't happen.
            }
            // nb: not thread-safe, so need to instantiate each time.
            // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
            val format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            val buf = new TitleBuffer();
            buf.append(format.format(source.getTimestamp()));
            buf.append(" ").append(source.getLogicalMemberIdentifier());
            return buf.toString();
        }
    }


    // (CHANGE) TYPE

    @DomainChangeRecord.ChangeTypeMeta
    @Override
    public DomainChangeRecord.ChangeType getType() {
        return ChangeType.COMMAND;
    }


    // INTERACTION ID

    @DomainChangeRecord.InteractionId
    @Override
    public abstract UUID getInteractionId();
    public abstract void setInteractionId(UUID interactionId);


    // USER NAME

    @DomainChangeRecord.Username
    @Override
    public abstract String getUsername();
    public abstract void setUsername(String username);


    // TIMESTAMP

    @DomainChangeRecord.TimestampMeta
    @Override
    public abstract java.sql.Timestamp getTimestamp();
    public abstract void setTimestamp(java.sql.Timestamp timestamp);


    // REPLAY STATE

    @Property(
            domainEvent = ReplayStateMeta.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = ReplayStateMeta.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            // fieldSetId = "XXX",  // TODO: fix
            // sequence = "XXX",
            named = "Replay State",
            typicalLength = ReplayStateMeta.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = ReplayStateMeta.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "Replay State",
            typicalLength = ReplayStateMeta.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReplayStateMeta {
        int MAX_LENGTH = 12;
        int TYPICAL_LENGTH = 12;

        class DomainEvent extends PropertyDomainEvent<org.apache.isis.extensions.commandlog.applib.dom.ReplayState> {}
    }


    /**
     * For a replayed command, what the outcome was.
     */
    @ReplayStateMeta
    public abstract org.apache.isis.extensions.commandlog.applib.dom.ReplayState getReplayState();
    public abstract void setReplayState(org.apache.isis.extensions.commandlog.applib.dom.ReplayState replayState);



    // REPLAY STATE FAILURE REASON

    @Property(
            domainEvent = ReplayStateFailureReason.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = ReplayStateFailureReason.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            multiLine = ReplayStateFailureReason.MULTI_LINE,
            typicalLength = ReplayStateFailureReason.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = ReplayStateFailureReason.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            multiLine = ReplayStateFailureReason.MULTI_LINE,
            typicalLength = ReplayStateFailureReason.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReplayStateFailureReason {
        int MAX_LENGTH = Integer.MAX_VALUE;
        int TYPICAL_LENGTH = 4000;
        int MULTI_LINE = 5;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    /**
     * For a {@link org.apache.isis.extensions.commandlog.applib.dom.ReplayState#FAILED failed} replayed command,
     * what the reason was for the failure.
     */
    @ReplayStateFailureReason
    public abstract String getReplayStateFailureReason();
    public abstract void setReplayStateFailureReason(String replayStateFailureReason);

    @MemberSupport public boolean hideReplayStateFailureReason() {
        return getReplayState() == null || !getReplayState().isFailed();
    }


    // PARENT

    @Property(
            domainEvent = Parent.DomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parent {
        class DomainEvent extends PropertyDomainEvent<Command> { }
    }

    @Parent
    public abstract PublishedCommand getParent();
    public abstract void setParent(PublishedCommand parent);



    // TARGET

    @DomainChangeRecord.TargetMeta
    public abstract Bookmark getTarget();
    public abstract void setTarget(Bookmark target);


    @Domain.Exclude
    public String getTargetStr() {
        return Optional.ofNullable(getTarget()).map(Bookmark::toString).orElse(null);
    }


    // TARGET MEMBER

    @DomainChangeRecord.TargetMember
    @Override
    public String getTargetMember() {
        return getCommandDto().getMember().getLogicalMemberIdentifier();
    }



    // LOCAL MEMBER

    @Property(
            domainEvent = LocalMember.DomainEvent.class,
            maxLength = LocalMember.MAX_LENGTH
    )
    @PropertyLayout(
            named = "Member",
            typicalLength = LocalMember.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = LocalMember.MAX_LENGTH
    )
    @ParameterLayout(
            typicalLength = LocalMember.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LocalMember {
        int MAX_LENGTH = 255;
        int TYPICAL_LENGTH = 30;

        class DomainEvent extends PropertyDomainEvent<String> { }
    }

    @LocalMember
    public String getLocalMember() {
        val targetMember = getTargetMember();
        return targetMember.substring(targetMember.indexOf("#") + 1);
    }



    // LOGICAL MEMBER IDENTIFIER

    @Property(
            domainEvent = LogicalMemberIdentifier.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = LogicalMemberIdentifier.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            typicalLength = LogicalMemberIdentifier.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = LogicalMemberIdentifier.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @ParameterLayout(
            typicalLength = LogicalMemberIdentifier.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LogicalMemberIdentifier {
        int MAX_LENGTH = 1024;
        int TYPICAL_LENGTH = 128;

        class DomainEvent extends PropertyDomainEvent<String> { }
    }

    @LogicalMemberIdentifier
    public abstract String getLogicalMemberIdentifier();
    public abstract void setLogicalMemberIdentifier(String logicalMemberIdentifier);



    // COMMAND DTO

    @Property(
            domainEvent = CommandDtoMeta.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = CommandDtoMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            multiLine = CommandDtoMeta.MULTI_LINE,
            typicalLength = CommandDtoMeta.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = CommandDtoMeta.MAX_LENGTH,
            optionality = Optionality.MANDATORY
    )
    @ParameterLayout(
            multiLine = CommandDtoMeta.MULTI_LINE,
            typicalLength = CommandDtoMeta.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CommandDtoMeta {
        int MAX_LENGTH = Integer.MAX_VALUE;
        int TYPICAL_LENGTH = 4000;

        int MULTI_LINE = 9;
        class DomainEvent extends PropertyDomainEvent<CommandDto> { }
    }

    @CommandDtoMeta
    public abstract CommandDto getCommandDto();
    public abstract void setCommandDto(CommandDto commandDto);



    // STARTED AT

    @Property(
            domainEvent = StartedAt.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = StartedAt.MAX_LENGTH
    )
    @PropertyLayout(
            typicalLength = StartedAt.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = StartedAt.MAX_LENGTH
    )
    @ParameterLayout(
            typicalLength = StartedAt.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StartedAt {
        int MAX_LENGTH = 32;
        int TYPICAL_LENGTH = 32;

        class DomainEvent extends PropertyDomainEvent<Timestamp> { }
    }

    @StartedAt
    public abstract Timestamp getStartedAt();
    public abstract void setStartedAt(Timestamp startedAt);



    // COMPLETED AT

    @Property(
            domainEvent = CompletedAt.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = CompletedAt.MAX_LENGTH
    )
    @PropertyLayout(
            typicalLength = CompletedAt.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = CompletedAt.MAX_LENGTH
    )
    @ParameterLayout(
            typicalLength = CompletedAt.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CompletedAt {
        int MAX_LENGTH = 32;
        int TYPICAL_LENGTH = 32;

        class DomainEvent extends PropertyDomainEvent<Timestamp> { }
    }

    @CompletedAt
    public abstract Timestamp getCompletedAt();
    public abstract void setCompletedAt(Timestamp completedAt);



    // DURATION

    @javax.validation.constraints.Digits(
            integer = Duration.DIGITS_INTEGER,
            fraction = Duration.DIGITS_FRACTION
    )
    @Property(
            domainEvent = Duration.DomainEvent.class,
            maxLength = Duration.MAX_LENGTH
    )
    @PropertyLayout(
            typicalLength = Duration.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = Duration.MAX_LENGTH
    )
    @ParameterLayout(
            typicalLength = Duration.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Duration {
        int DIGITS_INTEGER = 5;
        int DIGITS_FRACTION = 3;
        int MAX_LENGTH = DIGITS_INTEGER + DIGITS_FRACTION + 1;
        int TYPICAL_LENGTH = 5;

        class DomainEvent extends PropertyDomainEvent<BigDecimal> { }
    }

    /**
     * The number of seconds (to 3 decimal places) that this interaction lasted.
     *
     * <p>
     * Populated only if it has {@link #getCompletedAt() completed}.
     */
    @Duration
    public BigDecimal getDuration() {
        return BigDecimalUtils.durationBetween(getStartedAt(), getCompletedAt());
    }



    // COMPLETE

    @Property(
            domainEvent = Complete.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Complete {
        class DomainEvent extends PropertyDomainEvent<Boolean> { }
    }

    @Complete
    public boolean isComplete() {
        return getCompletedAt() != null;
    }



    // RESULT SUMMARY

    @Property(
            domainEvent = ResultSummary.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = ResultSummary.MAX_LENGTH
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS,
            named = "Result",
            typicalLength = ResultSummary.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = ResultSummary.MAX_LENGTH
    )
    @ParameterLayout(
            typicalLength = ResultSummary.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ResultSummary {
        int MAX_LENGTH = 20;
        int TYPICAL_LENGTH = 12;
        class DomainEvent extends PropertyDomainEvent<String> { }
    }

    @ResultSummary
    public String getResultSummary() {
        if(getCompletedAt() == null) {
            return "";
        }
        if(!_Strings.isNullOrEmpty(getException())) {
            return "EXCEPTION";
        }
        if(getResult() != null) {
            return "OK";
        } else {
            return "OK (VOID)";
        }
    }



    // RESULT

    @Property(
            domainEvent = Result.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Result.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            named = "Result"
    )
    @Parameter(
            maxLength = Result.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            named = "Result"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Result {
        int MAX_LENGTH = 2000;

        class DomainEvent extends PropertyDomainEvent<Bookmark> { }
    }

    @Result
    public abstract Bookmark getResult();
    public abstract void setResult(Bookmark result);



    // EXCEPTION

    @Property(
            domainEvent = ExceptionMeta.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = ExceptionMeta.MAX_LENGTH
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            multiLine = ExceptionMeta.MULTI_LINE,
            named = "Exception (if any)",
            typicalLength = ExceptionMeta.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = ExceptionMeta.MAX_LENGTH
    )
    @ParameterLayout(
            multiLine = ExceptionMeta.MULTI_LINE,
            typicalLength = ExceptionMeta.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ExceptionMeta {
        int MAX_LENGTH = Integer.MAX_VALUE;
        int TYPICAL_LENGTH = 4000;
        int MULTI_LINE = 5;

        class DomainEvent extends PropertyDomainEvent<String> { }
    }

    /**
     * Stack trace of any exception that might have occurred if this interaction/transaction aborted.
     *
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so there's no object that can be accessed to be annotated.
     */
    public abstract String getException();
    public abstract void setException(final String exception);

    public void setException(final Throwable exception) {
        setException(_Exceptions.asStacktrace(exception));
    }



    // CAUSED EXCEPTION

    @Property(
            domainEvent = CausedException.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CausedException {
        class DomainEvent extends PropertyDomainEvent<Boolean> { }
    }

    @CausedException
    public boolean isCausedException() {
        return getException() != null;
    }



    // PRE VALUE

    @DomainChangeRecord.PreValue
    @Override
    public String getPreValue() {
        return null;
    }



    // POST VALUE

    @DomainChangeRecord.PostValue
    @Override
    public String getPostValue() {
        return null;
    }



    // PROGRAMMATIC

    @Programmatic
    public void saveAnalysis(final String analysis) {
        if (analysis == null) {
            setReplayState(ReplayState.OK);
        } else {
            setReplayState(ReplayState.FAILED);
            setReplayStateFailureReason(StringUtils.trimmed(analysis, 255));
        }
    }

    @Programmatic
    public CommandOutcomeHandler outcomeHandler() {
        return new CommandOutcomeHandler() {
            @Override
            public Timestamp getStartedAt() {
                return PublishedCommand.this.getStartedAt();
            }

            @Override
            public void setStartedAt(final Timestamp startedAt) {
                PublishedCommand.this.setStartedAt(startedAt);
            }

            @Override
            public void setCompletedAt(final Timestamp completedAt) {
                PublishedCommand.this.setCompletedAt(completedAt);
            }

            @Override
            public void setResult(final org.apache.isis.commons.functional.Result<Bookmark> resultBookmark) {
                PublishedCommand.this.setResult(resultBookmark.getValue().orElse(null));
                PublishedCommand.this.setException(resultBookmark.getFailure().orElse(null));
            }
        };
    }



    // TO STRING etc

    @Override
    public String toString() {
        return toFriendlyString();
    }

    String toFriendlyString() {
        return ObjectContracts
                .toString("interactionId", PublishedCommand::getInteractionId)
                .thenToString("username", PublishedCommand::getUsername)
                .thenToString("timestamp", PublishedCommand::getTimestamp)
                .thenToString("target", PublishedCommand::getTarget)
                .thenToString("logicalMemberIdentifier", PublishedCommand::getLogicalMemberIdentifier)
                .thenToStringOmitIfAbsent("startedAt", PublishedCommand::getStartedAt)
                .thenToStringOmitIfAbsent("completedAt", PublishedCommand::getCompletedAt)
                .toString(this);
    }


    @Component
    @javax.annotation.Priority(PriorityPrecedence.LATE - 10)
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<PublishedCommand> {

        public TableColumnOrderDefault() { super(PublishedCommand.class); }

        @Override
        protected List<String> orderParented(final Object parent, final String collectionId, final List<String> propertyIds) {
            return ordered(propertyIds);
        }

        @Override
        protected List<String> orderStandalone(final List<String> propertyIds) {
            return ordered(propertyIds);
        }

        private List<String> ordered(final List<String> propertyIds) {
            return Arrays.asList(
                "timestamp",
                    "target",
                    "targetMember",
                    "username",
                    "complete",
                    "resultSummary",
                    "interactionId"
            );
        }
    }
}

