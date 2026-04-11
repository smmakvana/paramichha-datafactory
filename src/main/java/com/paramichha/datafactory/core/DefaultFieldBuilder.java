package com.paramichha.datafactory.core;

import com.paramichha.datafactory.analyzer.FieldConstraints;
import com.paramichha.datafactory.planner.BoundaryPlanner;
import com.paramichha.datafactory.shaper.ValueGenerator;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class DefaultFieldBuilder implements FieldBuilder {

    private final FieldConstraints constraints;

    DefaultFieldBuilder(FieldConstraints constraints) {
        this.constraints = constraints;
    }

    private static List<ConstraintCase> deduplicate(List<ConstraintCase> cases) {
        List<ConstraintCase> result = new ArrayList<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        for (ConstraintCase c : cases) {
            if (seen.add(c.testNameSuffix())) result.add(c);
        }
        return result;
    }

    private static int toStringLength(long value) {
        return (int) Math.min(Math.max(value, 0), Integer.MAX_VALUE);
    }

    private static boolean isNumeric(Class<?> t) {
        return t == Integer.class || t == int.class
                || t == Long.class || t == long.class
                || t == Short.class || t == short.class
                || t == Byte.class || t == byte.class
                || t == Double.class || t == double.class
                || t == Float.class || t == float.class
                || t == BigDecimal.class
                || t == java.math.BigInteger.class;
    }

    private static boolean isBoolean(Class<?> t) {
        return t == Boolean.class || t == boolean.class;
    }

    private static boolean isList(Class<?> t) {
        return t == java.util.List.class || t == java.util.Collection.class;
    }

    private static Object castNumeric(Class<?> type, long value) {
        if (type == Integer.class || type == int.class) return (int) value;
        if (type == Long.class || type == long.class) return value;
        if (type == Short.class || type == short.class) return (short) value;
        if (type == Byte.class || type == byte.class) return (byte) value;
        if (type == Double.class || type == double.class) return (double) value;
        if (type == Float.class || type == float.class) return (float) value;
        if (type == BigDecimal.class) return BigDecimal.valueOf(value);
        if (type == java.math.BigInteger.class) return java.math.BigInteger.valueOf(value);
        return value;
    }

    private static Object futureTemporal(Class<?> type) {
        if (type == LocalDate.class) return LocalDate.now().plusDays(30);
        if (type == LocalDateTime.class) return LocalDateTime.now().plusDays(30);
        if (type == ZonedDateTime.class) return ZonedDateTime.now().plusDays(30);
        if (type == OffsetDateTime.class) return OffsetDateTime.now().plusDays(30);
        if (type == java.util.Date.class) return java.util.Date.from(Instant.now().plusSeconds(86400 * 30L));
        if (type == Year.class) return Year.now().plusYears(1);
        if (type == YearMonth.class) return YearMonth.now().plusMonths(1);
        return Instant.now().plusSeconds(86400 * 30L);
    }

    private static Object pastTemporal(Class<?> type) {
        if (type == LocalDate.class) return LocalDate.now().minusDays(30);
        if (type == LocalDateTime.class) return LocalDateTime.now().minusDays(30);
        if (type == ZonedDateTime.class) return ZonedDateTime.now().minusDays(30);
        if (type == OffsetDateTime.class) return OffsetDateTime.now().minusDays(30);
        if (type == java.util.Date.class) return java.util.Date.from(Instant.now().minusSeconds(86400 * 30L));
        if (type == Year.class) return Year.now().minusYears(1);
        if (type == YearMonth.class) return YearMonth.now().minusMonths(1);
        return Instant.now().minusSeconds(86400 * 30L);
    }

    @Override
    public List<Object> validValues() {
        return ValueGenerator.shapeAll(constraints, BoundaryPlanner.plan(constraints));
    }

    @Override
    public List<String> validSourceCode() {
        return validValues().stream().map(this::toSourceCode).toList();
    }

    @Override
    public List<ConstraintCase> constraintCases() {
        return computeConstraintCases();
    }

    @Override
    public List<ConstraintCase> annotationCases() {
        return constraintCases().stream().filter(ConstraintCase::isAnnotationDriven).toList();
    }

    @Override
    public FieldConstraints constraints() {
        return constraints;
    }

    private List<ConstraintCase> computeConstraintCases() {
        List<ConstraintCase> cases = new ArrayList<>();
        String fn = constraints.fieldName();
        Class<?> type = constraints.fieldType();

        // Layer 1: type-level defaults

        if (!type.isPrimitive() && !constraints.mustBeNull() && constraints.nullable()) {
            cases.add(typeDefault(fn, "Null", fn + "_null", null));
        }
        if (type == String.class && constraints.nullable()) {
            cases.add(typeDefault(fn, "Blank", fn + "_blank", ""));
        }
        if (isNumeric(type) && constraints.nullable() && !constraints.bounds().hasMin() && !isNegativeAllowed()) {
            cases.add(typeDefault(fn, "Negative", fn + "_negative", castNumeric(type, -1)));
        }
        if (isBoolean(type) && constraints.nullable() && !constraints.assertTrueRequired() && !constraints.assertFalseRequired()) {
            cases.add(typeDefault(fn, "False", fn + "_false", false));
        }
        if (isList(type) && constraints.nullable()) {
            cases.add(typeDefault(fn, "Null", fn + "_null", null));
            cases.add(typeDefault(fn, "Empty", fn + "_empty", List.of()));
        }

        // Layer 2: annotation-driven cases

        if (!constraints.nullable()) {
            cases.add(annotationCase(fn, "NotNull", fn + "_null", null));
        }
        if (!constraints.nullable() && type == String.class) {
            cases.add(annotationCase(fn, "NotBlank", fn + "_blank", ""));
        }
        switch (constraints.format()) {
            case EMAIL -> cases.add(annotationCase(fn, "Email", fn + "_invalidFormat", "notanemail"));
            case URL -> cases.add(annotationCase(fn, "URL", fn + "_invalidFormat", "not-a-url"));
            case CREDIT_CARD -> cases.add(annotationCase(fn, "CreditCardNumber", fn + "_invalidFormat", "1234-bad"));
            default -> {
            }
        }
        if (constraints.bounds().hasMin() && type == String.class) {
            long min = constraints.bounds().min();
            if (min > 0) {
                cases.add(annotationCase(fn, "Size", fn + "_tooShort", "x".repeat(toStringLength(min - 1))));
            }
        }
        if (constraints.bounds().hasMax() && type == String.class) {
            long max = constraints.bounds().max();
            cases.add(annotationCase(fn, "Size", fn + "_tooLong", "x".repeat(toStringLength(max + 1))));
        }
        if (constraints.bounds().hasMin() && isNumeric(type)) {
            cases.add(annotationCase(fn, "Min", fn + "_belowMin", castNumeric(type, constraints.bounds().min() - 1)));
        }
        if (constraints.bounds().hasMax() && isNumeric(type)) {
            cases.add(annotationCase(fn, "Max", fn + "_aboveMax", castNumeric(type, constraints.bounds().max() + 1)));
        }
        if (isNegativeAllowed() && isNumeric(type)) {
            cases.add(annotationCase(fn, "Negative", fn + "_positive", castNumeric(type, 1)));
        }
        if (constraints.isPast()) {
            cases.add(annotationCase(fn, "Past", fn + "_notPast", futureTemporal(type)));
        }
        if (constraints.isFuture()) {
            cases.add(annotationCase(fn, "Future", fn + "_notFuture", pastTemporal(type)));
        }
        if (constraints.assertTrueRequired()) {
            cases.add(annotationCase(fn, "AssertTrue", fn + "_false", false));
        }
        if (constraints.assertFalseRequired()) {
            cases.add(annotationCase(fn, "AssertFalse", fn + "_true", true));
        }

        return List.copyOf(deduplicate(cases));
    }

    private boolean isNegativeAllowed() {
        return constraints.bounds().hasMax()
                && constraints.bounds().max() != null
                && constraints.bounds().max() <= 0;
    }

    private ConstraintCase annotationCase(String fieldName, String constraint, String suffix, Object value) {
        return ConstraintCase.of(fieldName, constraint, suffix, value, toSourceCode(value));
    }

    private ConstraintCase typeDefault(String fieldName, String constraint, String suffix, Object value) {
        return ConstraintCase.typeDefault(fieldName, constraint, suffix, value, toSourceCode(value));
    }

    private String toSourceCode(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + s + "\"";
        if (value instanceof Long l) return l + "L";
        if (value instanceof Double d) return d + "d";
        if (value instanceof Float f) return f + "f";
        if (value instanceof BigDecimal bd) return "new BigDecimal(\"" + bd + "\")";
        if (value instanceof Instant) return "Instant.now()";
        if (value instanceof LocalDate) return "LocalDate.now()";
        if (value instanceof LocalDateTime) return "LocalDateTime.now()";
        if (value instanceof ZonedDateTime) return "ZonedDateTime.now()";
        if (value instanceof OffsetDateTime) return "OffsetDateTime.now()";
        if (value instanceof Enum<?> e) return e.getClass().getSimpleName() + "." + e.name();
        if (value instanceof UUID) return "UUID.randomUUID()";
        if (value instanceof List<?> l && l.isEmpty()) return "List.of()";
        return value.toString();
    }
}
