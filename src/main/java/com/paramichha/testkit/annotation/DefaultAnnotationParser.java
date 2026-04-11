package com.paramichha.testkit.annotation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public final class DefaultAnnotationParser implements AnnotationParser, AnnotationSupport {

    private final Map<String, Class<? extends Annotation>> byName = new HashMap<>();
    private final Map<String, AnnotationType> byType = new HashMap<>();
    private final AnnotationTypeHandlerRegistry handlerRegistry;

    public DefaultAnnotationParser() {
        this(new AnnotationTypeHandlerRegistry());
    }

    public DefaultAnnotationParser(AnnotationTypeHandlerRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;

        register(AnnotationType.NO_ARG,
                NotNull.class,
                NotBlank.class,
                NotEmpty.class,
                Null.class,
                Valid.class,
                Email.class,
                URL.class,
                CreditCardNumber.class,
                ISBN.class,
                EAN.class,
                org.hibernate.validator.constraints.UUID.class,
                Positive.class,
                PositiveOrZero.class,
                Negative.class,
                NegativeOrZero.class,
                Past.class,
                PastOrPresent.class,
                Future.class,
                FutureOrPresent.class,
                AssertTrue.class,
                AssertFalse.class);

        register(AnnotationType.ONE_ARG,
                Min.class,
                Max.class,
                DecimalMin.class,
                DecimalMax.class,
                Pattern.class);

        register(AnnotationType.MULTI_ARG,
                Size.class,
                Length.class,
                Digits.class);
    }

    @Override
    public Annotation parse(String annotationString) throws AnnotationParseException {
        try {
            String trimmed = annotationString.trim();

            if (trimmed.startsWith("@")) {
                trimmed = trimmed.substring(1);

                int open = trimmed.indexOf('(');
                int close = trimmed.lastIndexOf(')');

                String name = null;
                String rawAttrs = null;

                if (open < 0 && close < 0) {
                    name = trimmed.trim();
                } else if (open >= 0 && close > open) {
                    name = trimmed.substring(0, open).trim();
                    rawAttrs = trimmed.substring(open + 1, close).trim();
                }

                Class<? extends Annotation> clazz = getSupportedAnnotation(name);

                AnnotationType type = getAnnotationType(clazz);
                AnnotationTypeHandler handler = handlerRegistry.get(type);

                return handler.create(clazz, rawAttrs == null ? "" : rawAttrs.trim());
            }

        } catch (Exception e) {
            throw new AnnotationParseException("Could not parse " + annotationString, e);
        }
        throw new AnnotationParseException("Annotation must start with '@': " + annotationString);
    }

    @Override
    public Class<? extends Annotation> getSupportedAnnotation(String name) throws AnnotationParseException {
        return byName.get(name);
    }

    @Override
    public AnnotationType getAnnotationType(Class<? extends Annotation> annotationClazz) throws AnnotationParseException {
        return byType.get(annotationClazz.getSimpleName());
    }

    @SafeVarargs
    private void register(AnnotationType type, Class<? extends Annotation>... classes) {
        for (Class<? extends Annotation> c : classes) {
            byName.put(c.getSimpleName(), c);
            byType.put(c.getSimpleName(), type);
        }
    }
}
