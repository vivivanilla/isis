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
package org.apache.isis.applib.value.semantics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.TimePrecision;
import org.apache.isis.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.util.schema.CommonDtoUtils;
import org.apache.isis.applib.value.semantics.TemporalValueSemantics.EditingFormatDirection;
import org.apache.isis.applib.value.semantics.TemporalValueSemantics.TemporalEditingPattern;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.schema.common.v2.ValueWithTypeDto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @since 2.x {@index}
 */
public abstract class ValueSemanticsAbstract<T>
implements
    ValueSemanticsProvider<T> {

    @Getter
    @RequiredArgsConstructor
    public static enum PlaceholderLiteral {
        NULL_REPRESENTATION("(none)",       "badge bg-light placeholder-literal-null"),
        SUPPRESSED(         "(suppressed)", "badge bg-light placeholder-literal-suppressed");
        private final String literal;
        private final String cssClass;
        public String asText(final UnaryOperator<String> translator) {
            return translator.apply(literal);
        }
        public String asHtml(final UnaryOperator<String> translator) {
            return String.format("<span class=\"%s\">%s</span>",
                    getCssClass(),
                    asText(translator));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public OrderRelation<T, ?> getOrderRelation() {
        return this instanceof OrderRelation ? (OrderRelation<T, ?>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Converter<T, ?> getConverter() {
        return this instanceof Converter ? (Converter<T, ?>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Renderer<T> getRenderer() {
        return this instanceof Renderer ? (Renderer<T>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parser<T> getParser() {
        return this instanceof Parser ? (Parser<T>)this : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultsProvider<T> getDefaultsProvider() {
        return this instanceof DefaultsProvider ? (DefaultsProvider<T>)this : null;
    }

    /**
     * JUnit support.
     */
    public Can<T> getExamples() { return Can.empty(); }

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link Locale} from given context or else system's default
     */
    protected UserLocale getUserLocale(final @Nullable ValueSemanticsProvider.Context context) {
        return Optional.ofNullable(context)
        .map(ValueSemanticsProvider.Context::getInteractionContext)
        .map(InteractionContext::getLocale)
        .orElseGet(UserLocale::getDefault);
    }

    protected String renderTitle(final T value, final Function<T, String> toString) {
        return Optional.ofNullable(value)
                .map(toString)
                .orElse(PlaceholderLiteral.NULL_REPRESENTATION.asText(this::translate));
    }

    protected String renderHtml(final T value, final Function<T, String> toString) {
        return Optional.ofNullable(value)
                .map(toString)
                .orElse(PlaceholderLiteral.NULL_REPRESENTATION.asHtml(this::translate));
    }


    // -- COMPOSITION UTILS

    protected ValueDecomposition decomposeAsString(
            final @Nullable T value,
            final @NonNull Function<T, String> toString,
            final @NonNull Supplier<String> onNull) {
        _Assert.assertEquals(getSchemaValueType(), ValueType.STRING);
        return CommonDtoUtils.fundamentalTypeAsDecomposition(ValueType.STRING,
                value!=null
                    ? toString.apply(value)
                    : onNull.get());
    }

    protected T composeFromString(
            final @Nullable ValueDecomposition decomposition,
            final @NonNull Function<String, T> fromString,
            final @NonNull Supplier<T> onNullOrEmpty) {
        val string = decomposition!=null
                ? decomposition.left().map(ValueWithTypeDto::getString).orElse(null)
                : null;
        return _Strings.isNotEmpty(string)
                ? fromString.apply(string)
                : onNullOrEmpty.get();
    }


    /**
     * @param <F> - the underlying fundamental value-type
     */
    protected <F> ValueDecomposition decomposeAsNullable(
            final @Nullable T value,
            final @NonNull Function<T, F> onNonNull,
            final @NonNull Supplier<F> onNull) {
        return CommonDtoUtils.fundamentalTypeAsDecomposition(getSchemaValueType(),
                value!=null
                    ? onNonNull.apply(value)
                    : onNull.get());
    }

    /**
     * @param <F> - the underlying fundamental value-type
     */
    protected <F> T composeFromNullable(
            final @Nullable ValueDecomposition decomposition,
            final @NonNull Function<ValueWithTypeDto, F> fundamentalValueExtractor,
            final @NonNull Function<F, T> onNonNull,
            final @NonNull Supplier<T> onNull) {

        val valuePojo = decomposition!=null
                ? decomposition.left().map(fundamentalValueExtractor).orElse(null)
                : null;

        return valuePojo!=null
                ? onNonNull.apply(valuePojo)
                : onNull.get();
    }

    // -- NUMBER FORMATTING/PARSING

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link NumberFormat} the default from from given context's locale
     * or else system's default locale
     *
     * @implNote the format's MaximumFractionDigits are initialized to 16, as
     * 64 bit IEEE 754 double has 15 decimal digits of precision;
     * this is typically overruled later by implementations of
     * {@link #configureDecimalFormat(org.apache.isis.applib.adapters.ValueSemanticsProvider.Context, DecimalFormat) configureDecimalFormat}
     */
   @SuppressWarnings("javadoc")
   protected DecimalFormat getNumberFormat(final @Nullable ValueSemanticsProvider.Context context) {
        val format = (DecimalFormat)NumberFormat.getNumberInstance(getUserLocale(context).getNumberFormatLocale());
        // prime w/ 16 (64 bit IEEE 754 double has 15 decimal digits of precision)
        format.setMaximumFractionDigits(16);
        return format;
    }

    protected Optional<BigInteger> parseInteger(
            final @Nullable ValueSemanticsProvider.Context context,
            final @Nullable String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return Optional.empty();
        }
        try {
            return parseDecimal(context, input)
            .map(BigDecimal::toBigIntegerExact);
        } catch (final NumberFormatException | ArithmeticException e) {
            throw new TextEntryParseException("Not an integer value " + text, e);
        }
    }

    protected Optional<BigDecimal> parseDecimal(
            final @Nullable ValueSemanticsProvider.Context context,
            final @Nullable String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return Optional.empty();
        }
        val format = getNumberFormat(context);
        format.setParseBigDecimal(true);
        configureDecimalFormat(context, format);

        val position = new ParsePosition(0);
        try {
            val number = (BigDecimal)format.parse(input, position);
            if (position.getErrorIndex() != -1) {
                throw new ParseException("could not parse input='" + input + "'", position.getErrorIndex());
            } else if (position.getIndex() < input.length()) {
                throw new ParseException("input='" + input + "' was not processed completely", position.getIndex());
            }
            // check for maxFractionDigits if required ...
            final int maxFractionDigits = format.getMaximumFractionDigits();
            if(maxFractionDigits>-1
                    && number.scale()>format.getMaximumFractionDigits()) {
                throw new TextEntryParseException(String.format(
                        "No more than %d digits can be entered after the decimal separator, "
                        + "got %d in '%s'.", maxFractionDigits, number.scale(), input));
            }
            return Optional.of(number);
        } catch (final NumberFormatException | ParseException e) {
            throw new TextEntryParseException(String.format(
                    "Not a decimal value '%s': %s", input, e.getMessage()),
                    e);
        }
    }

    /**
     * Typically overridden by BigDecimalValueSemantics to set MaximumFractionDigits.
     */
    protected void configureDecimalFormat(final Context context, final DecimalFormat format) {}

    // -- TEMPORAL RENDERING

    protected DateTimeFormatter getTemporalNoZoneRenderingFormat(
            final @Nullable ValueSemanticsProvider.Context context,
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic,
            final @NonNull FormatStyle dateFormatStyle,
            final @NonNull FormatStyle timeFormatStyle) {

        final DateTimeFormatter noZoneOutputFormat;

        switch (temporalCharacteristic) {
        case DATE_TIME:
            noZoneOutputFormat = DateTimeFormatter.ofLocalizedDateTime(dateFormatStyle, timeFormatStyle);
            break;
        case DATE_ONLY:
            noZoneOutputFormat = DateTimeFormatter.ofLocalizedDate(dateFormatStyle);
            break;
        case TIME_ONLY:
            noZoneOutputFormat = DateTimeFormatter.ofLocalizedTime(timeFormatStyle);
            break;
        default:
            throw _Exceptions.unmatchedCase(temporalCharacteristic);
        }
        return noZoneOutputFormat
                .withLocale(getUserLocale(context).getTimeFormatLocale());
    }

    protected Optional<DateTimeFormatter> getTemporalZoneOnlyRenderingFormat(
            final @Nullable ValueSemanticsProvider.Context context,
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic) {

             switch (offsetCharacteristic) {
        case LOCAL:
            return Optional.empty();
        case OFFSET:
            return Optional.of(getIsoTimeZoneOffsetFormat());
        case ZONED:
            return Optional.of(getLocalizedTimeZoneIdFormat(context));
        default:
            throw _Exceptions.unmatchedCase(offsetCharacteristic);
        }
    }

    @Getter(lazy = true, value = AccessLevel.PROTECTED)
    private final DateTimeFormatter isoTimeZoneOffsetFormat = new DateTimeFormatterBuilder()
            .appendOffsetId()
            .toFormatter(Locale.US); // arbitrarily picking a locale, just in case; (this is an ISO format)

    private DateTimeFormatter getLocalizedTimeZoneIdFormat(final ValueSemanticsProvider.Context context) {
        return new DateTimeFormatterBuilder()
            .appendPattern("VV")
            .toFormatter(getUserLocale(context).getTimeFormatLocale());
    }

    // -- TEMPORAL FORMATTING/PARSING

    protected DateTimeFormatter getTemporalEditingFormat(
            final @Nullable ValueSemanticsProvider.Context context,
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic,
            final @NonNull TimePrecision timePrecision,
            final @NonNull EditingFormatDirection direction,
            final @NonNull TemporalEditingPattern editingPattern) {

        return new DateTimeFormatterBuilder()
                .appendPattern(editingPattern
                        .getEditingFormatAsPattern(
                                temporalCharacteristic, offsetCharacteristic, timePrecision, direction))
                .toFormatter(getUserLocale(context).getTimeFormatLocale());
    }

    protected DateTimeFormatter getTemporalIsoFormat(
            final @NonNull TemporalValueSemantics.TemporalCharacteristic temporalCharacteristic,
            final @NonNull TemporalValueSemantics.OffsetCharacteristic offsetCharacteristic) {

        switch (temporalCharacteristic) {
        case DATE_TIME:
            return offsetCharacteristic.isLocal()
                    ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    : DateTimeFormatter.ISO_DATE_TIME;
        case DATE_ONLY:
            return offsetCharacteristic.isLocal()
                    ? DateTimeFormatter.ISO_LOCAL_DATE
                    : DateTimeFormatter.ISO_DATE;
        case TIME_ONLY:
            return offsetCharacteristic.isLocal()
                    ? DateTimeFormatter.ISO_LOCAL_TIME
                    : DateTimeFormatter.ISO_TIME;
        default:
            throw _Exceptions.unmatchedCase(temporalCharacteristic);
        }
    }

    // TRANSLATION SUPPORT

    @Autowired(required = false) // nullable (JUnit support)
    protected TranslationService translationService;
    protected String translate(final String text) {
        return translationService!=null
                ? translationService.translate(TranslationContext.empty(), text)
                : text;
    }


}
