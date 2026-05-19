package com.paramichha.testkit.generator;

import com.paramichha.datafactory.DataFactory;
import com.paramichha.testkit.model.ReflectionClassInfo;
import com.paramichha.testkit.model.FieldInfo;

import com.paramichha.utils.Texts;
import java.lang.annotation.Annotation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes constructor/builder call expressions for a class.
 * Single responsibility — knows how to instantiate, nothing else.
 */
final class InstantiationWriter {

    private final ReflectionClassInfo meta;

    InstantiationWriter(ReflectionClassInfo meta) {
        this.meta = meta;
    }

    /** Full constructor call with all field values as literals. */
    String constructorCall(List<Object> values) {
        return switch (meta.constructorStyle()) {
            case RECORD, ALL_ARGS -> {
                String args = values.stream().map(LiteralConverter::toLiteral)
                        .collect(Collectors.joining(", "));
                yield "new " + meta.simpleName() + "(" + args + ")";
            }
            case LOMBOK_BUILDER -> {
                StringBuilder b = new StringBuilder(meta.simpleName() + ".builder()");
                List<FieldInfo> fields = meta.fields();
                for (int i = 0; i < fields.size() && i < values.size(); i++)
                    b.append("\n                    .").append(fields.get(i).name())
                     .append("(").append(LiteralConverter.toLiteral(values.get(i))).append(")");
                yield b.append("\n                    .build()").toString();
            }
            case SETTER -> {
                List<FieldInfo> fields = meta.fields();
                StringBuilder b = new StringBuilder("build" + meta.simpleName() + "(");
                for (int i = 0; i < fields.size() && i < values.size(); i++) {
                    if (i > 0) b.append(", ");
                    b.append(LiteralConverter.toLiteral(values.get(i)));
                }
                b.append(")");
                yield b.toString();
            }
        };
    }

    /** Constructor call with one field replaced by a variable reference. */
    String constructorCallWithVar(List<Object> baseParts, int fieldIndex, String varName) {
        List<String> args = new java.util.ArrayList<>();
        List<FieldInfo> fields = meta.fields();
        for (int i = 0; i < fields.size(); i++)
            args.add(i == fieldIndex ? varName : LiteralConverter.toLiteral(baseParts.get(i)));

        return switch (meta.constructorStyle()) {
            case RECORD, ALL_ARGS -> "new " + meta.simpleName() + "(" + String.join(", ", args) + ")";
            case LOMBOK_BUILDER -> {
                StringBuilder b = new StringBuilder(meta.simpleName() + ".builder()");
                for (int i = 0; i < fields.size(); i++)
                    b.append(".").append(fields.get(i).name()).append("(").append(args.get(i)).append(")");
                yield b.append(".build()").toString();
            }
            case SETTER -> {
                fields = meta.fields();
                StringBuilder b = new StringBuilder("build" + meta.simpleName() + "(");
                for (int i = 0; i < fields.size(); i++) {
                    if (i > 0) b.append(", ");
                    b.append(i == fieldIndex ? varName : LiteralConverter.toLiteral(baseParts.get(i)));
                }
                b.append(")");
                yield b.toString();
            }
        };
    }

    /**
     * Returns valid field values by calling DataFactory.field(f).validList() directly.
     * This bypasses the DefaultDataBuilder.validList() skip of fields with <= 1 value,
     * ensuring every field always has at least one valid value for test generation.
     */
    List<?> validValuesFor(Class<?> type, FieldInfo field) {
        try {
            // Use field name + type + FieldMeta annotations — NOT field.getAnnotations()
            // because for records, Jakarta annotations live on the record component/
            // constructor parameter, not the backing field, so field.getAnnotations()
            // returns empty and DataFactory generates unconstrained values.
            List<Annotation> annotations = field.validations();
            Class<?> rawType = field.rawType();

            // @Null constraint — the only valid value IS null
            boolean isMustBeNull = annotations.stream()
                    .anyMatch(a -> a.annotationType().getSimpleName().equals("Null"));
            if (isMustBeNull) {
                return java.util.Arrays.asList((Object) null);
            }

            // List<T> has no shaper in FieldBuilder — generate one element instead
            if (rawType == java.util.List.class || rawType == java.util.Collection.class) {
                Object oneElement = buildOneValidListElement(field);
                return oneElement != null ? List.of(oneElement) : List.of();
            }

            // Domain type (not a java/jakarta type): use DataFactory.valid() directly
            // since FieldBuilder has no shaper for custom types
            String pkg = rawType.getPackageName();
            boolean isDomainType = !rawType.isPrimitive() && !rawType.isEnum()
                    && !pkg.startsWith("java.") && !pkg.startsWith("jakarta.");
            if (isDomainType) {
                try {
                    Object domainValid = DataFactory.valid(rawType);
                    return domainValid != null ? List.of(domainValid) : List.of();
                } catch (Exception ignored2) {
                    return List.of();
                }
            }

            @SuppressWarnings("unchecked")
            List<?> values = DataFactory.field(
                    field.name(), (Class<Object>) rawType, annotations).validList();
            // validAll may return empty for truly unknown types — fall back to valid()
            if (values.isEmpty()) {
                Object single = DataFactory.field(
                        field.name(), (Class<Object>) rawType, annotations).valid();
                return single != null ? List.of(single) : List.of();
            }
            return values;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Builds a single-element list as the valid value for a List<T> field.
     * Uses DataFactory to generate one valid element of the list's element type.
     */
    @SuppressWarnings("unchecked")
    private Object buildOneValidListElement(FieldInfo field) {
        try {
            // Extract element type from FieldMeta.genericType (e.g. List<String> → String)
            Class<?> elemType = String.class;
            if (field.genericType() instanceof java.lang.reflect.ParameterizedType pt) {
                java.lang.reflect.Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class<?> c) elemType = c;
            }
            // Domain type — use DefaultDataBuilder to create a valid instance
            if (!elemType.getPackageName().startsWith("java.")) {
                return java.util.List.of(DataFactory.valid(elemType));
            }
            // Java type — use FieldBuilder
            Object elem = DataFactory.field(field.name(), (Class<Object>) elemType, java.util.List.of()).valid();
            return elem != null ? java.util.List.of(elem) : java.util.List.of("x");
        } catch (Exception e) {
            return java.util.List.of();
        }
    }

    /**
     * Returns invalid field values by calling DataFactory.field(f).invalidList().
     */
    List<?> invalidValuesFor(Class<?> type, FieldInfo field) {
        try {
            List<Annotation> annotations = field.validations();
            @SuppressWarnings("unchecked")
            List<?> values = DataFactory.field(
                    field.name(), (Class<Object>) field.rawType(), annotations).invalidList();
            return values;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String capitalize(String s) {
        return Texts.capitalize(s);
    }
}
