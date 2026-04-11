package com.paramichha.datafactory.analyzer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.*;

import java.lang.annotation.Annotation;

/**
 * Classifies real {@code java.lang.annotation.Annotation} instances obtained via reflection.
 */
final class RuntimeAnnotationClassifier implements AnnotationClassifier {

    public static final RuntimeAnnotationClassifier INSTANCE = new RuntimeAnnotationClassifier();

    private RuntimeAnnotationClassifier() {
    }

    @Override
    public boolean isNotNull(Object a) {
        return a instanceof NotNull;
    }

    @Override
    public boolean isNotBlank(Object a) {
        return a instanceof NotBlank;
    }

    @Override
    public boolean isNotEmpty(Object a) {
        return a instanceof NotEmpty;
    }

    @Override
    public boolean isNull(Object a) {
        return a instanceof Null;
    }

    @Override
    public boolean isValid(Object a) {
        return a instanceof Valid;
    }

    @Override
    public FormatType classifyFormat(Object raw) {
        Annotation a = (Annotation) raw;
        if (a instanceof Email) return FormatType.EMAIL;
        if (a instanceof URL) return FormatType.URL;
        if (a instanceof Pattern) return FormatType.PATTERN;
        if (a instanceof CreditCardNumber) return FormatType.CREDIT_CARD;
        if (a instanceof ISBN) return FormatType.ISBN;
        if (a instanceof EAN) return FormatType.EAN;
        if (a instanceof org.hibernate.validator.constraints.UUID) return FormatType.UUID_STRING;
        return FormatType.NONE;
    }

    @Override
    public String extractPatternRegexp(Object raw) {
        return raw instanceof Pattern p ? p.regexp() : null;
    }

    @Override
    public QuantityBounds classifyQuantity(Object raw) {
        Annotation a = (Annotation) raw;
        if (a instanceof Min m) return QuantityBounds.atLeast(m.value());
        if (a instanceof Max m) return QuantityBounds.atMost(m.value());
        if (a instanceof Positive) return QuantityBounds.atLeast(1L);
        if (a instanceof PositiveOrZero) return QuantityBounds.atLeast(0L);
        if (a instanceof Negative) return QuantityBounds.atMost(-1L);
        if (a instanceof NegativeOrZero) return QuantityBounds.atMost(0L);
        if (a instanceof Size s) {
            Long min = s.min() > 0 ? (long) s.min() : null;
            Long max = s.max() < Integer.MAX_VALUE ? (long) s.max() : null;
            return (min != null || max != null) ? new QuantityBounds(min, max) : null;
        }
        if (a instanceof Length l) {
            Long min = l.min() > 0 ? (long) l.min() : null;
            Long max = l.max() < Integer.MAX_VALUE ? (long) l.max() : null;
            return (min != null || max != null) ? new QuantityBounds(min, max) : null;
        }
        if (a instanceof DecimalMin dm) {
            try {
                return QuantityBounds.atLeast((long) Double.parseDouble(dm.value()));
            } catch (NumberFormatException ignored) {
            }
        }
        if (a instanceof DecimalMax dm) {
            try {
                return QuantityBounds.atMost((long) Double.parseDouble(dm.value()));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    @Override
    public int extractIntegerDigits(Object raw) {
        return raw instanceof Digits d ? d.integer() : -1;
    }

    @Override
    public int extractFractionDigits(Object raw) {
        return raw instanceof Digits d ? d.fraction() : -1;
    }

    @Override
    public TemporalDirection classifyTemporal(Object raw) {
        Annotation a = (Annotation) raw;
        if (a instanceof Past) return TemporalDirection.PAST;
        if (a instanceof PastOrPresent) return TemporalDirection.PAST_OR_NOW;
        if (a instanceof Future) return TemporalDirection.FUTURE;
        if (a instanceof FutureOrPresent) return TemporalDirection.FUTURE_OR_NOW;
        return TemporalDirection.NONE;
    }

    @Override
    public boolean isAssertTrue(Object a) {
        return a instanceof AssertTrue;
    }

    @Override
    public boolean isAssertFalse(Object a) {
        return a instanceof AssertFalse;
    }
}
