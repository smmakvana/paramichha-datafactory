package com.paramichha.datafactory.builder;

import com.paramichha.datafactory.FieldValue;
import com.paramichha.datafactory.constraint.AnnotationResolver;
import com.paramichha.datafactory.constraint.FieldDescriptor;
import com.paramichha.datafactory.generation.DataMachine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class ValueBuilderFactory {

    private ValueBuilderFactory() {}

    // ── without machine — uses DataMachines.random() internally ─────────────

    public static <T> FieldValue<T> create(Class<T> type) {
        return new FieldValueBuilder<>(
                AnnotationResolver.analyze("field", type, List.of()));
    }

    public static <T> FieldValue<T> create(Class<T> type, String hint) {
        return new FieldValueBuilder<>(
                AnnotationResolver.analyze(hint, type, List.of()));
    }

    public static <T> FieldValue<T> create(FieldDescriptor descriptor) {
        return new FieldValueBuilder<>(
                AnnotationResolver.analyze(
                        descriptor.name(), descriptor.rawType(), descriptor.validations()));
    }

    @SuppressWarnings("unchecked")
    public static <T> FieldValue<T> create(Field field) {
        List<Annotation> annotations = Arrays.asList(field.getAnnotations());
        return new FieldValueBuilder<>(
                AnnotationResolver.analyze(
                        field.getName(), (Class<T>) field.getType(), annotations));
    }

    public static <T> FieldValue<T> create(String name, Class<T> type, List<Annotation> annotations) {
        return new FieldValueBuilder<>(
                AnnotationResolver.analyze(name, type, annotations));
    }

    // ── with machine — deterministic when seeded ─────────────────────────────

    public static <T> FieldValue<T> create(Class<T> type, DataMachine machine) {
        return new FieldValueBuilder<>(
                AnnotationResolver.analyze("field", type, List.of()), machine);
    }

    public static <T> FieldValue<T> create(FieldDescriptor descriptor, DataMachine machine) {
        return new FieldValueBuilder<>(
                AnnotationResolver.analyze(
                        descriptor.name(), descriptor.rawType(), descriptor.validations()), machine);
    }
}
