package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.AnnotationParseException;
import com.paramichha.datafactory.annotation.AnnotationCreator;
import com.paramichha.datafactory.annotation.AnnotationCreatorRegistry;
import com.paramichha.datafactory.annotation.AnnotationLookup;
import com.paramichha.datafactory.annotation.AnnotationParser;
import com.paramichha.datafactory.annotation.ArgumentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

public final class StringAnnotationParser implements AnnotationParser, AnnotationLookup {

    public static final StringAnnotationParser INSTANCE = new StringAnnotationParser();

    private final Map<String, Class<? extends Annotation>> byName = new HashMap<>();
    private final Map<String, ArgumentType> byType = new HashMap<>();
    private final AnnotationCreatorRegistry handlerRegistry;

    public StringAnnotationParser() {
        this(new AnnotationCreatorRegistry());
    }

    public StringAnnotationParser(AnnotationCreatorRegistry handlerRegistry) {
        this.handlerRegistry = handlerRegistry;

        register(
                ArgumentType.NO_ARG,
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

        register(ArgumentType.ONE_ARG, Min.class, Max.class, DecimalMin.class, DecimalMax.class, Pattern.class);

        register(ArgumentType.MULTI_ARG, Size.class, Length.class, Digits.class);
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

                Class<? extends Annotation> clazz = getAnnotation(name);

                ArgumentType type = getArgumentType(clazz);
                AnnotationCreator handler = handlerRegistry.get(type);

                return handler.create(clazz, rawAttrs == null ? "" : rawAttrs.trim());
            }

        } catch (Exception e) {
            throw new AnnotationParseException("Could not parse " + annotationString, e);
        }
        throw new AnnotationParseException("Annotation must start with '@': " + annotationString);
    }

    @Override
    public Class<? extends Annotation> getAnnotation(String name) throws AnnotationParseException {
        return byName.get(name);
    }

    @Override
    public ArgumentType getArgumentType(Class<? extends Annotation> annotationClazz) throws AnnotationParseException {
        return byType.get(annotationClazz.getSimpleName());
    }

    @SafeVarargs
    private void register(ArgumentType type, Class<? extends Annotation>... classes) {
        for (Class<? extends Annotation> c : classes) {
            byName.put(c.getSimpleName(), c);
            byType.put(c.getSimpleName(), type);
        }
    }
}
