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
package org.apache.isis.testdomain.model.valuetypes;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.graph.tree.TreeAdapter;
import org.apache.isis.applib.graph.tree.TreeNode;
import org.apache.isis.applib.graph.tree.TreeState;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.valuesemantics.ApplicationFeatureIdValueSemantics;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEvent;
import org.apache.isis.extensions.fullcalendar.applib.value.CalendarEventSemantics;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.cmd.v2.CommandDto;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public abstract class ValueTypeExample<T> {

    @Property
    public abstract T getValue();
    public abstract void setValue(T value);

    @Programmatic
    public abstract T getUpdateValue();

    @Action
    public final void updateValue(final T value) {
        setValue(value);
    }

    @Autowired(required = false) List<ValueSemanticsAbstract<T>> semanticsList;
    @Programmatic
    public Can<T> getExamples() {
        return Can.ofCollection(semanticsList)
        .getFirst()
        .map(semantics->semantics.getExamples())
        .orElseGet(()->Can.of(getValue(), getUpdateValue()));
    }

    @Collection
    public List<T> getValues() {
        return List.of(getValue(), getUpdateValue());
    }

    @SuppressWarnings("unchecked")
    @Programmatic
    public final Class<T> getValueType() {
        return (Class<T>) getValue().getClass();
    }

    // -- EXAMPLES - BASIC

    @Named("isis.testdomain.valuetypes.ValueTypeExampleBoolean")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBoolean
    extends ValueTypeExample<Boolean> {
        @Property @Getter @Setter
        private Boolean value = Boolean.TRUE;
        @Getter
        private Boolean updateValue = Boolean.FALSE;
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleCharacter")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleCharacter
    extends ValueTypeExample<Character> {
        @Property @Getter @Setter
        private Character value = 'a';
        @Getter
        private Character updateValue = 'b';
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleString")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleString
    extends ValueTypeExample<String> {
        @Property @Getter @Setter
        private String value = "aString";
        @Getter
        private String updateValue = "anotherString";
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExamplePassword")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExamplePassword
    extends ValueTypeExample<Password> {
        @Property @Getter @Setter
        private Password value = Password.of("aPassword");
        @Getter
        private Password updateValue = Password.of("anotherPassword");
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleBufferedImage")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBufferedImage
    extends ValueTypeExample<BufferedImage> {
        @Property @Getter @Setter
        private BufferedImage value = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        @Getter
        private BufferedImage updateValue = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleBlob")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBlob
    extends ValueTypeExample<Blob> {
        @Property @Getter @Setter
        private Blob value = Blob.of("aBlob", CommonMimeType.BIN, new byte[] {1, 2, 3});
        @Getter
        private Blob updateValue = Blob.of("anotherBlob", CommonMimeType.BIN, new byte[] {3, 4});
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleClob")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleClob
    extends ValueTypeExample<Clob> {
        @Property @Getter @Setter
        private Clob value = Clob.of("aClob", CommonMimeType.TXT, "abc");
        @Getter
        private Clob updateValue = Clob.of("anotherClob", CommonMimeType.TXT, "ef");
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleLocalResourcePath")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalResourcePath
    extends ValueTypeExample<LocalResourcePath> {
        @Property @Getter @Setter
        private LocalResourcePath value = new LocalResourcePath("img/a");
        @Getter
        private LocalResourcePath updateValue = new LocalResourcePath("img/b");
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleUrl")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleUrl
    extends ValueTypeExample<URL> {
        @Property @Getter @Setter
        private URL value = url("https://a.b.c");
        @Getter
        private URL updateValue = url("https://b.c.d");
        @SneakyThrows
        private static URL url(final String url) {
            return new URL(url);
        }
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleMarkup")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleMarkup
    extends ValueTypeExample<Markup> {
        @Property @Getter @Setter
        private Markup value = Markup.valueOf("aMarkup");
        @Getter
        private Markup updateValue = Markup.valueOf("anotherMarkup");
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleUuid")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleUuid
    extends ValueTypeExample<UUID> {
        @Property @Getter @Setter
        private UUID value = UUID.randomUUID();
        @Getter
        private UUID updateValue = UUID.randomUUID();
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleLocale")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocale
    extends ValueTypeExample<Locale> {
        @Property @Getter @Setter
        private Locale value = Locale.US;
        @Getter
        private Locale updateValue = Locale.GERMAN;
    }

    // -- EXAMPLES - NUMBERS

    @Named("isis.testdomain.valuetypes.ValueTypeExampleByte")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleByte
    extends ValueTypeExample<Byte> {
        @Property @Getter @Setter
        private Byte value = -63;
        @Getter
        private Byte updateValue = 0;
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleShort")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleShort
    extends ValueTypeExample<Short> {
        @Property @Getter @Setter
        private Short value = -63;
        @Getter
        private Short updateValue = 0;
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleInteger")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleInteger
    extends ValueTypeExample<Integer> {
        @Property @Getter @Setter
        private Integer value = -63;
        @Getter
        private Integer updateValue = 0;
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleLong")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLong
    extends ValueTypeExample<Long> {
        @Property @Getter @Setter
        private Long value = -63L;
        @Getter
        private Long updateValue = 0L;
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleFloat")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleFloat
    extends ValueTypeExample<Float> {
        @Property @Getter @Setter
        private Float value = -63.1f;
        @Getter
        private Float updateValue = 0.f;

        //FIXME does not handle example Float.MIN_VALUE well
        @Deprecated // remove override once fixed
        @Override public Can<Float> getExamples() {
            return Can.of(value, updateValue);
        }
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleDouble")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleDouble
    extends ValueTypeExample<Double> {
        @Property @Getter @Setter
        private Double value = -63.1;
        @Getter
        private Double updateValue = 0.;

        //FIXME does not handle example Double.MIN_VALUE well
        @Deprecated // remove override once fixed
        @Override public Can<Double> getExamples() {
            return Can.of(value, updateValue);
        }
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleBigInteger")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBigInteger
    extends ValueTypeExample<BigInteger> {
        @Property @Getter @Setter
        private BigInteger value = BigInteger.valueOf(-63L);
        @Getter
        private BigInteger updateValue = BigInteger.ZERO;
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleBigDecimal")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBigDecimal
    extends ValueTypeExample<BigDecimal> {
        @Property @Getter @Setter
        private BigDecimal value = new BigDecimal("-63.1");
        @Getter
        private BigDecimal updateValue = BigDecimal.ZERO;
    }

    // -- EXAMPLES - TEMPORAL - LEGACY

    @Named("isis.testdomain.valuetypes.ValueTypeExampleJavaUtilDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJavaUtilDate
    extends ValueTypeExample<java.util.Date> {
        @Property @Getter @Setter
        private java.util.Date value = new java.util.Date();
        @Getter
        private java.util.Date updateValue = new java.util.Date(0L);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleJavaSqlDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJavaSqlDate
    extends ValueTypeExample<java.sql.Date> {
        @Property @Getter @Setter
        private java.sql.Date value = new java.sql.Date(new java.util.Date().getTime());
        @Getter
        private java.sql.Date updateValue = new java.sql.Date(0L);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleJavaSqlTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJavaSqlTime
    extends ValueTypeExample<java.sql.Time> {
        @Property @Getter @Setter
        private java.sql.Time value = new java.sql.Time(new java.util.Date().getTime());
        @Getter
        private java.sql.Time updateValue = new java.sql.Time(0L);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleTimestamp")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleTimestamp
    extends ValueTypeExample<Timestamp> {
        @Property @Getter @Setter
        private Timestamp value = new Timestamp(new java.util.Date().getTime());
        @Getter
        private Timestamp updateValue = new Timestamp(0L);
    }

    // -- EXAMPLES - TEMPORAL - JAVA TIME

    @Named("isis.testdomain.valuetypes.ValueTypeExampleLocalDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalDate
    extends ValueTypeExample<LocalDate> {
        @Property @Getter @Setter
        private LocalDate value = LocalDate.now();
        @Getter
        private LocalDate updateValue = LocalDate.now().plusDays(2);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleLocalDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalDateTime
    extends ValueTypeExample<LocalDateTime> {
        @Property @Getter @Setter
        private LocalDateTime value = LocalDateTime.now();
        @Getter
        private LocalDateTime updateValue = LocalDateTime.now().plusDays(2).plusSeconds(15);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleLocalTime")
    @DomainObject(

            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalTime
    extends ValueTypeExample<LocalTime> {
        @Property @Getter @Setter
        private LocalTime value = LocalTime.now();
        @Getter
        private LocalTime updateValue = LocalTime.now().plusSeconds(15);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleOffsetDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleOffsetDateTime
    extends ValueTypeExample<OffsetDateTime> {
        @Property @Getter @Setter
        private OffsetDateTime value = OffsetDateTime.now();
        @Getter
        private OffsetDateTime updateValue = OffsetDateTime.now().plusDays(2).plusSeconds(15);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleOffsetTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleOffsetTime
    extends ValueTypeExample<OffsetTime> {
        @Property @Getter @Setter
        private OffsetTime value = OffsetTime.now();
        @Getter
        private OffsetTime updateValue = OffsetTime.now().plusSeconds(15);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleZonedDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleZonedDateTime
    extends ValueTypeExample<ZonedDateTime> {
        @Property @Getter @Setter
        private ZonedDateTime value = ZonedDateTime.now();
        @Getter
        private ZonedDateTime updateValue = ZonedDateTime.now().plusDays(2).plusSeconds(15);
    }

    // -- EXAMPLES - TEMPORAL - JODA TIME

    @Named("isis.testdomain.valuetypes.ValueTypeExampleJodaDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaDateTime
    extends ValueTypeExample<org.joda.time.DateTime> {
        @Property @Getter @Setter
        private org.joda.time.DateTime value = org.joda.time.DateTime.now();
        @Getter
        private org.joda.time.DateTime updateValue = org.joda.time.DateTime.now().plusDays(2).plusSeconds(15);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleJodaLocalDateTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaLocalDateTime
    extends ValueTypeExample<org.joda.time.LocalDateTime> {
        @Property @Getter @Setter
        private org.joda.time.LocalDateTime value = org.joda.time.LocalDateTime.now();
        @Getter
        private org.joda.time.LocalDateTime updateValue = org.joda.time.LocalDateTime.now().plusDays(2).plusSeconds(15);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleJodaLocalDate")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaLocalDate
    extends ValueTypeExample<org.joda.time.LocalDate> {
        @Property @Getter @Setter
        private org.joda.time.LocalDate value = org.joda.time.LocalDate.now();
        @Getter
        private org.joda.time.LocalDate updateValue = org.joda.time.LocalDate.now().plusDays(2);
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleJodaLocalTime")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleJodaLocalTime
    extends ValueTypeExample<org.joda.time.LocalTime> {
        @Property @Getter @Setter
        private org.joda.time.LocalTime value = org.joda.time.LocalTime.now();
        @Getter
        private org.joda.time.LocalTime updateValue = org.joda.time.LocalTime.now().plusSeconds(15);
    }

    // -- EXAMPLES - META MODEL

    @Named("isis.testdomain.valuetypes.ValueTypeExampleApplicationFeatureId")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleApplicationFeatureId
    extends ValueTypeExample<ApplicationFeatureId> {
        @Property @Getter @Setter
        private ApplicationFeatureId value = new ApplicationFeatureIdValueSemantics().getExamples().getElseFail(0);
        @Getter
        private ApplicationFeatureId updateValue = new ApplicationFeatureIdValueSemantics().getExamples().getElseFail(1);
    }

    // -- EXAMPLES - DATA STRUCTURE

    //TODO    TreeNode
//    @DomainObject(
//            @Named("isis.testdomain.valuetypes.ValueTypeExampleTreeNode",
//            nature = Nature.BEAN)
    public static class ValueTypeExampleTreeNode
    extends ValueTypeExample<TreeNode<String>> {
        @Property @Getter @Setter
        private TreeNode<String> value = TreeNode.of("root", TreeAdapterString.class, TreeState.rootCollapsed());
        @Getter
        private TreeNode<String> updateValue = TreeNode.of("anotherRoot", TreeAdapterString.class, TreeState.rootCollapsed());

        private static class TreeAdapterString implements TreeAdapter<String> {
            @Override public Optional<String> parentOf(final String value) {
                return null; }
            @Override public int childCountOf(final String value) {
                return 0; }
            @Override public Stream<String> childrenOf(final String value) {
                return Stream.empty(); }
        }

    }

    // -- EXAMPLES - ENUM

    public static enum ExampleEnum {
        HALLO, WORLD
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleEnum")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleEnum
    extends ValueTypeExample<ExampleEnum> {
        @Property @Getter @Setter
        private ExampleEnum value = ExampleEnum.HALLO;
        @Getter
        private ExampleEnum updateValue = ExampleEnum.WORLD;
    }

    // -- EXAMPLES - COMPOSITES

    @Named("isis.testdomain.valuetypes.ValueTypeExampleCalendarEvent")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleCalendarEvent
    extends ValueTypeExample<CalendarEvent> {
        @Property @Getter @Setter
        private CalendarEvent value = new CalendarEventSemantics().getExamples().getElseFail(0);
        @Getter
        private CalendarEvent updateValue = new CalendarEventSemantics().getExamples().getElseFail(1);
    }

    // -- EXAMPLES - DATA STRUCTURE

    // -- EXAMPLES - OTHER

    @Named("isis.testdomain.valuetypes.ValueTypeExampleBookmark")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleBookmark
    extends ValueTypeExample<Bookmark> {
        @Property @Getter @Setter
        private Bookmark value = Bookmark.parseElseFail("a:b");
        @Getter
        private Bookmark updateValue = Bookmark.parseElseFail("c:d");
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleOidDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleOidDto
    extends ValueTypeExample<OidDto> {
        @Property @Getter @Setter
        private OidDto value = Bookmark.parseElseFail("a:b").toOidDto();
        @Getter
        private OidDto updateValue = Bookmark.parseElseFail("c:d").toOidDto();
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleChangesDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleChangesDto
    extends ValueTypeExample<ChangesDto> {
        @Property @Getter @Setter
        private ChangesDto value = new ChangesDto();
        @Getter
        private ChangesDto updateValue = new ChangesDto();
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleCommandDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleCommandDto
    extends ValueTypeExample<CommandDto> {
        @Property @Getter @Setter
        private CommandDto value = new CommandDto();
        @Getter
        private CommandDto updateValue = new CommandDto();
    }

    @Named("isis.testdomain.valuetypes.ValueTypeExampleInteractionDto")
    @DomainObject(
            nature = Nature.BEAN)
    public static class ValueTypeExampleInteractionDto
    extends ValueTypeExample<InteractionDto> {
        @Property @Getter @Setter
        private InteractionDto value = new InteractionDto();
        @Getter
        private InteractionDto updateValue = new InteractionDto();
    }

}
