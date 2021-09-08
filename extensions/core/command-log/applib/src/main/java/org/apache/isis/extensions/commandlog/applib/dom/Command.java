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

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.jaxb.JavaSqlXMLGregorianCalendarMarshalling;
import org.apache.isis.applib.mixins.system.DomainChangeRecord;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandOutcomeHandler;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.services.tablecol.TableColumnOrderForCollectionTypeAbstract;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.TitleBuffer;
import org.apache.isis.commons.functional.Result;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.util.BigDecimalUtils;
import org.apache.isis.extensions.commandlog.applib.util.StringUtils;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.cmd.v2.MapDto;

import lombok.val;

@DomainObject(
        logicalTypeName = Command.LOGICAL_TYPE_NAME,
        editing = Editing.DISABLED
)
@DomainObjectLayout(
        titleUiEvent = Command.TitleUiEvent.class,
        iconUiEvent = Command.IconUiEvent.class,
        cssClassUiEvent = Command.CssClassUiEvent.class,
        layoutUiEvent = Command.LayoutUiEvent.class
)
public abstract class Command implements DomainChangeRecord {

    public final static String LOGICAL_TYPE_NAME = IsisModuleExtCommandLogApplib.NAMESPACE + ".Command";

    public static class TitleUiEvent extends IsisModuleExtCommandLogApplib.TitleUiEvent<CommandModel> { }
    public static class IconUiEvent extends IsisModuleExtCommandLogApplib.IconUiEvent<CommandModel> { }
    public static class CssClassUiEvent extends IsisModuleExtCommandLogApplib.CssClassUiEvent<CommandModel> { }
    public static class LayoutUiEvent extends IsisModuleExtCommandLogApplib.LayoutUiEvent<CommandModel> { }

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtCommandLogApplib.PropertyDomainEvent<CommandModel, T> { }
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtCommandLogApplib.CollectionDomainEvent<CommandModel, T> { }


    public Command(final org.apache.isis.applib.services.command.Command command) {

        setInteractionIdStr(command.getInteractionId().toString());
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
    public Command(
            final CommandDto commandDto,
            final ReplayState replayState,
            final int targetIndex) {

        setInteractionIdStr(commandDto.getInteractionId());
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

    @Service
    public static class TitleProvider {

        @EventListener(TitleUiEvent.class)
        public void on(final TitleUiEvent ev) {
            if(!Objects.equals(ev.getTitle(), "Command Jdo") || ev.getTranslatableTitle() != null) {
                return;
            }
            ev.setTitle(title((Command)ev.getSource()));
        }

        private static String title(final Command source) {
            // nb: not thread-safe
            // formats defined in https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
            val format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            val buf = new TitleBuffer();
            buf.append(format.format(source.getTimestamp()));
            buf.append(" ").append(source.getLogicalMemberIdentifier());
            return buf.toString();
        }
    }


    @DomainChangeRecord.ChangeTypeMeta
    @Override
    public DomainChangeRecord.ChangeType getType() {
        return ChangeType.COMMAND;
    }


    @DomainChangeRecord.InteractionId
    @Override
    public abstract UUID getInteractionId();
    public abstract void setInteractionId(UUID interactionId);


    @DomainChangeRecord.Username
    @Override
    public abstract String getUsername();
    public abstract void setUsername(String username);


    @DomainChangeRecord.Timestamp
    @Override
    public abstract java.sql.Timestamp getTimestamp();
    public abstract void setTimestamp(java.sql.Timestamp timestamp);



    @Property(
            domainEvent = ReplayState.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = ReplayState.MAX_LENGTH
    )
    @PropertyLayout(
            // fieldSetId = "XXX",  // TODO: fix
            // sequence = "XXX",
            typicalLength = ReplayState.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = ReplayState.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Replay State",
            typicalLength = ReplayState.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReplayState {
        int MAX_LENGTH = 12;
        int TYPICAL_LENGTH = 12;

        class DomainEvent extends PropertyDomainEvent<org.apache.isis.extensions.commandlog.applib.dom.ReplayState> {}
    }


    /**
     * For a replayed command, what the outcome was.
     */
    @Command.ReplayState
    public abstract org.apache.isis.extensions.commandlog.applib.dom.ReplayState getReplayState();
    public abstract void setReplayState(org.apache.isis.extensions.commandlog.applib.dom.ReplayState replayState);



    @Property(
            domainEvent = ReplayStateFailureReason.DomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            multiLine = ReplayStateFailureReason.MULTILINE
    )
    @Parameter(
    )
    @ParameterLayout(
            multiLine = ReplayStateFailureReason.MULTILINE
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ReplayStateFailureReason {

        int MULTILINE = 5;
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



    public static class ParentDomainEvent extends PropertyDomainEvent<org.apache.isis.applib.services.command.Command> { }
    @Property(
            domainEvent = ParentDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    public abstract Command getParent();
    public abstract void setParent(Command parent);



    public static class TargetDomainEvent extends PropertyDomainEvent<Bookmark> { }
    @Property(
            domainEvent = TargetDomainEvent.class
    )
    @PropertyLayout(
            named = "Object"
    )
    public abstract Bookmark getTarget();
    public abstract void setTarget(Bookmark target);

    public String getTargetStr() {
        return Optional.ofNullable(getTarget()).map(Bookmark::toString).orElse(null);
    }

    @Override
    public String getTargetMember() {
        return getCommandDto().getMember().getLogicalMemberIdentifier();
    }


    public static class LocalMemberEvent extends PropertyDomainEvent<String> { }
    @Property(
            domainEvent = LocalMemberEvent.class
    )
    @PropertyLayout(
            named = "Member"
    )
    public String getLocalMember() {
        val targetMember = getTargetMember();
        return targetMember.substring(targetMember.indexOf("#") + 1);
    }


    public static class LogicalMemberIdentifierDomainEvent extends PropertyDomainEvent<String> { }
    @Property(
            domainEvent = LogicalMemberIdentifierDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES
    )
    public abstract String getLogicalMemberIdentifier();
    public abstract void setLogicalMemberIdentifier(String logicalMemberIdentifier);


    public static class CommandDtoDomainEvent extends PropertyDomainEvent<CommandDto> { }
    @Property(
            domainEvent = CommandDtoDomainEvent.class
    )
    @PropertyLayout(
            multiLine = 9
    )
    public abstract CommandDto getCommandDto();
    public abstract void setCommandDto(CommandDto commandDto);



    public static class StartedAtDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @Property(
            domainEvent = StartedAtDomainEvent.class
    )
    public abstract Timestamp getStartedAt();
    public abstract void setStartedAt(Timestamp startedAt);


    public static class CompletedAtDomainEvent extends PropertyDomainEvent<Timestamp> { }
    @Property(
            domainEvent = CompletedAtDomainEvent.class
    )
    public abstract Timestamp getCompletedAt();
    public abstract void setCompletedAt(Timestamp completedAt);


    public static class DurationDomainEvent extends PropertyDomainEvent<BigDecimal> { }
    /**
     * The number of seconds (to 3 decimal places) that this interaction lasted.
     *
     * <p>
     * Populated only if it has {@link #getCompletedAt() completed}.
     */
    @javax.validation.constraints.Digits(integer=5, fraction=3)
    @Property(
            domainEvent = DurationDomainEvent.class
    )
    public BigDecimal getDuration() {
        return BigDecimalUtils.durationBetween(getStartedAt(), getCompletedAt());
    }


    public static class IsCompleteDomainEvent extends PropertyDomainEvent<Boolean> { }
    @Property(
            domainEvent = IsCompleteDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS
    )
    public boolean isComplete() {
        return getCompletedAt() != null;
    }



    public static class ResultSummaryDomainEvent extends PropertyDomainEvent<String> { }
    @Property(domainEvent = ResultSummaryDomainEvent.class)
    @PropertyLayout(hidden = Where.OBJECT_FORMS, named = "Result")
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


    public static class ResultDomainEvent extends PropertyDomainEvent<String> { }
    @Property(
            domainEvent = ResultDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            named = "Result Bookmark"
    )
    public abstract Bookmark getResult();
    public abstract void setResult(Bookmark result);


    public static class ExceptionDomainEvent extends PropertyDomainEvent<String> { }
    /**
     * Stack trace of any exception that might have occurred if this interaction/transaction aborted.
     *
     * <p>
     * Not part of the applib API, because the default implementation is not persistent
     * and so there's no object that can be accessed to be annotated.
     */
    @Property(
            domainEvent = ExceptionDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.ALL_TABLES,
            multiLine = 5, named = "Exception (if any)"
    )
    public abstract String getException();
    public abstract void setException(final String exception);

    public void setException(final Throwable exception) {
        setException(_Exceptions.asStacktrace(exception));
    }


    public static class IsCausedExceptionDomainEvent extends PropertyDomainEvent<Boolean> { }
    @Property(
            domainEvent = IsCausedExceptionDomainEvent.class
    )
    @PropertyLayout(
            hidden = Where.OBJECT_FORMS
    )
    public boolean isCausedException() {
        return getException() != null;
    }


    @Override
    public String getPreValue() {
        return null;
    }

    @Override
    public String getPostValue() {
        return null;
    }


    public void saveAnalysis(final String analysis) {
        if (analysis == null) {
            setReplayState(ReplayState.OK);
        } else {
            setReplayState(ReplayState.FAILED);
            setReplayStateFailureReason(StringUtils.trimmed(analysis, 255));
        }
    }

    @Override
    public String toString() {
        return toFriendlyString();
    }

    String toFriendlyString() {
        return ObjectContracts
                .toString("interactionId", Command::getInteractionId)
                .thenToString("username", Command::getUsername)
                .thenToString("timestamp", Command::getTimestamp)
                .thenToString("target", Command::getTarget)
                .thenToString("logicalMemberIdentifier", Command::getLogicalMemberIdentifier)
                .thenToStringOmitIfAbsent("startedAt", Command::getStartedAt)
                .thenToStringOmitIfAbsent("completedAt", Command::getCompletedAt)
                .toString(this);
    }

    public CommandOutcomeHandler outcomeHandler() {
        return new CommandOutcomeHandler() {
            @Override
            public Timestamp getStartedAt() {
                return Command.this.getStartedAt();
            }

            @Override
            public void setStartedAt(final Timestamp startedAt) {
                Command.this.setStartedAt(startedAt);
            }

            @Override
            public void setCompletedAt(final Timestamp completedAt) {
                Command.this.setCompletedAt(completedAt);
            }

            @Override
            public void setResult(final Result<Bookmark> resultBookmark) {
                Command.this.setResult(resultBookmark.getValue().orElse(null));
                Command.this.setException(resultBookmark.getFailure().orElse(null));
            }

        };
    }

    @Service
    @javax.annotation.Priority(PriorityPrecedence.LATE - 10)
    public static class TableColumnOrderDefault extends TableColumnOrderForCollectionTypeAbstract<Command> {

        public TableColumnOrderDefault() { super(Command.class); }

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
                "timestamp", "target", "targetMember", "username", "complete", "resultSummary", "interactionIdStr"
            );
        }
    }
}

