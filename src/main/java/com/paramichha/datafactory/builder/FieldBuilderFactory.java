package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.FieldBuilder;
import com.paramichha.datafactory.constraint.AnnotationAnalyzer;
import com.paramichha.datafactory.constraint.FieldConstraints;
import com.paramichha.datafactory.constraint.FieldDescriptor;
import net.datafaker.Faker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class FieldBuilderFactory {

    private FieldBuilderFactory() {}

    // ── without faker — uses FakerProvider.random() internally ───────────

    public static <T> FieldBuilder<T> create(Class<T> type) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze("field", type, List.of()));
    }

    public static <T> FieldBuilder<T> create(Class<T> type, String hint) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze(hint, type, List.of()));
    }

    public static <T> FieldBuilder<T> create(FieldDescriptor descriptor) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze(
                        descriptor.name(), descriptor.rawType(), descriptor.validations()));
    }

    @SuppressWarnings("unchecked")
    public static <T> FieldBuilder<T> create(Field field) {
        List<Annotation> annotations = Arrays.asList(field.getAnnotations());
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze(
                        field.getName(), (Class<T>) field.getType(), annotations));
    }

    public static <T> FieldBuilder<T> create(String name, Class<T> type, List<Annotation> annotations) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze(name, type, annotations));
    }

    // ── with faker — deterministic when seed is set ───────────────────────

    public static <T> FieldBuilder<T> create(Class<T> type, Faker faker) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze("field", type, List.of()), faker);
    }

    public static <T> FieldBuilder<T> create(Class<T> type, Faker faker, long seed) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze("field", type, List.of()), faker, seed);
    }

    public static <T> FieldBuilder<T> create(FieldDescriptor descriptor, Faker faker) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze(
                        descriptor.name(), descriptor.rawType(), descriptor.validations()), faker);
    }

    public static <T> FieldBuilder<T> create(FieldDescriptor descriptor, Faker faker, long seed) {
        return new DefaultFieldBuilder<>(
                AnnotationAnalyzer.analyze(
                        descriptor.name(), descriptor.rawType(), descriptor.validations()), faker, seed);
    }
}