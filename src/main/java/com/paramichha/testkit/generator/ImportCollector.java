package com.paramichha.testkit.generator;

import com.paramichha.testkit.model.FieldInfo;

import java.util.*;

/**
 * Collects Java import statements needed by generated test source.
 *
 * Two sources of imports:
 * 1. Runtime value types — BigDecimal, LocalDate etc from LiteralConverter
 * 2. Field types from the @Testable class — enum types, domain types, nested types
 *    that appear in constructor calls in the generated test
 */
final class ImportCollector {

    private static final List<String> FIXED = List.of(
            "jakarta.validation.Validation",
            "jakarta.validation.Validator",
            "org.junit.jupiter.api.BeforeAll",
            "org.junit.jupiter.api.DisplayName",
            "org.junit.jupiter.api.Nested",
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.params.provider.Arguments",
            "org.junit.jupiter.params.provider.MethodSource",
            "java.util.List",
            "java.util.Collection",
            "java.util.stream.Stream",
            "static org.assertj.core.api.Assertions.assertThat",
            "static org.assertj.core.api.Assertions.assertThatCode",
            "static org.assertj.core.api.Assertions.assertThatThrownBy"
    );

    private final TreeSet<String> imports = new TreeSet<>(FIXED);

    /** Add import for the @Testable class itself. */
    void addTypeImport(Class<?> type) {
        addIfNeeded(type);
    }

    /**
     * Add imports for all field types declared on the @Testable class.
     * This covers: enum types, domain types, nested types — anything that
     * appears in the generated constructor call.
     */
    void addFieldTypeImports(List<FieldInfo> fields) {
        for (FieldInfo field : fields) {
            addIfNeeded(field.rawType());

            // Handle generic type arguments — e.g. List<LocalDate>, List<FixtureStatus>
            if (field.genericType() instanceof java.lang.reflect.ParameterizedType pt) {
                for (java.lang.reflect.Type arg : pt.getActualTypeArguments()) {
                    if (arg instanceof Class<?> argClass) {
                        addIfNeeded(argClass);
                    }
                }
            }
        }
    }

    /** Add imports derived from runtime values (BigDecimal, LocalDate etc). */
    void addValueImports(Object value) {
        imports.addAll(LiteralConverter.importsFor(value));
    }

    void addValueImports(List<?> values) {
        values.forEach(this::addValueImports);
    }

    /** Add imports for Jakarta annotations used in wrapper records. */
    void addAnnotationImports(List<java.lang.annotation.Annotation> annotations) {
        for (java.lang.annotation.Annotation a : annotations) {
            String pkg = a.annotationType().getPackageName();
            if (!pkg.startsWith("java.lang")) {
                imports.add(a.annotationType().getName()
                        .replace(a.annotationType().getSimpleName(), "")
                        .replaceAll("\\.$", "") + "." + a.annotationType().getSimpleName());
            }
        }
    }

    void addRawImport(String fullyQualified) {
        imports.add(fullyQualified);
    }

    Set<String> collect() {
        return Collections.unmodifiableSet(imports);
    }

    // ── private ───────────────────────────────────────────────────────────

    private void addIfNeeded(Class<?> type) {
        // Delegate to LiteralConverter.importsForClass which handles
        // inner classes, arrays, primitives, and java.lang exclusions consistently
        imports.addAll(LiteralConverter.importsForClass(type));
    }
}