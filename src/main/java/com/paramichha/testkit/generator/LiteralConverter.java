package com.paramichha.testkit.generator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.*;

/**
 * Converts a runtime value to a Java source literal string.
 *
 * <pre>
 * "alice@example.com"  → "\"alice@example.com\""
 * 34                   → "34"
 * LocalDate.of(...)    → "LocalDate.of(1990, 1, 15)"
 * null                 → "null"
 * </pre>
 *
 * Complex objects that cannot be expressed as a literal (nested records,
 * domain objects, etc.) return null from {@link #toLiteralOrNull} and
 * are filtered out by callers before writing.
 */
final class LiteralConverter {

    private LiteralConverter() {}

    /**
     * Returns true if this value can be expressed as a Java source literal.
     * Used by writers to skip fields/objects they cannot inline.
     */
    static boolean canSerialize(Object value) {
        if (value == null) return true;
        return switch (value) {
            case String ignored      -> true;
            case Character ignored   -> true;
            case Boolean ignored     -> true;
            case Integer ignored     -> true;
            case Long ignored        -> true;
            case Short ignored       -> true;
            case Byte ignored        -> true;
            case Float ignored       -> true;
            case Double ignored      -> true;
            case BigDecimal ignored  -> true;
            case BigInteger ignored  -> true;
            case UUID ignored        -> true;
            case LocalDate ignored   -> true;
            case LocalDateTime ignored -> true;
            case LocalTime ignored   -> true;
            case Instant ignored     -> true;
            case ZonedDateTime ignored -> true;
            case OffsetDateTime ignored -> true;
            case Year ignored        -> true;
            case YearMonth ignored   -> true;
            case Date ignored        -> true;
            case Enum<?> ignored     -> true;
            case List<?> list        -> list.stream().allMatch(LiteralConverter::canSerialize);
            default                  -> value.getClass().isRecord() && canSerializeRecord(value);
        };
    }

    /**
     * Converts a value to a literal, or returns null if the value cannot
     * be expressed as a Java source literal.
     * Callers should use {@link #canSerialize} to filter before calling this.
     */
    static String toLiteralOrNull(Object value) {
        if (!canSerialize(value)) return null;
        return toLiteral(value);
    }

    public static String toLiteral(Object value) {
        return switch (value) {
            case null                -> "null";
            case String s            -> "\"" + escape(s) + "\"";
            case Character c         -> "'" + (c == '\'' ? "\\'": c == '\\' ? "\\\\" : String.valueOf(c)) + "'";
            case Boolean b           -> b.toString();
            case Integer i           -> i.toString();
            case Long l              -> l + "L";
            case Short s             -> "(short) " + s;
            case Byte b              -> "(byte) " + b;
            case Float f             -> f + "f";
            case Double d            -> d + "d";
            case BigDecimal bd       -> "new BigDecimal(\"" + bd.toPlainString() + "\")";
            case BigInteger bi       -> "BigInteger.valueOf(" + bi + "L)";
            case UUID u              -> "UUID.fromString(\"" + u + "\")";
            case LocalDate d         -> "LocalDate.of(" + d.getYear() + ", " + d.getMonthValue() + ", " + d.getDayOfMonth() + ")";
            case LocalDateTime d     -> "LocalDateTime.of(" + d.getYear() + ", " + d.getMonthValue() + ", " + d.getDayOfMonth() + ", " + d.getHour() + ", " + d.getMinute() + ")";
            case LocalTime t         -> "LocalTime.of(" + t.getHour() + ", " + t.getMinute() + ")";
            case Instant i           -> "Instant.parse(\"" + i + "\")";
            case ZonedDateTime z     -> "ZonedDateTime.parse(\"" + z + "\")";
            case OffsetDateTime o    -> "OffsetDateTime.parse(\"" + o + "\")";
            case Year y              -> "Year.of(" + y.getValue() + ")";
            case YearMonth ym        -> "YearMonth.of(" + ym.getYear() + ", " + ym.getMonthValue() + ")";
            case Date d              -> "new Date(" + d.getTime() + "L)";
            case Enum<?> e           -> e.getClass().getSimpleName() + "." + e.name();
            case List<?> list        -> {
                if (list.isEmpty()) yield "List.of()";
                // if any element can't be serialized, fall back to empty list
                if (!list.stream().allMatch(LiteralConverter::canSerialize)) yield "List.of()";
                yield "List.of(" + list.stream().map(LiteralConverter::toLiteral)
                        .reduce((a, b) -> a + ", " + b).orElse("") + ")";
            }
            default                  -> {
                if (value.getClass().isRecord()) yield toLiteralRecord(value);
                yield "/* TODO: " + value.getClass().getSimpleName() + " */ null";
            }
        };
    }

    /** Returns true if all record components can be serialized. */
    private static boolean canSerializeRecord(Object record) {
        try {
            for (java.lang.reflect.RecordComponent rc : record.getClass().getRecordComponents()) {
                Object val = rc.getAccessor().invoke(record);
                if (!canSerialize(val)) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Serializes a record as a constructor call: new Foo("bar", 42). */
    private static String toLiteralRecord(Object record) {
        try {
            java.lang.reflect.RecordComponent[] rcs = record.getClass().getRecordComponents();
            // Use enclosing-aware name: InnerRecord → OuterClass.InnerRecord
            String typeName = JavaTypeNames.of(record.getClass());
            StringBuilder sb = new StringBuilder("new ")
                    .append(typeName).append("(");
            for (int i = 0; i < rcs.length; i++) {
                Object val = rcs[i].getAccessor().invoke(record);
                sb.append(toLiteral(val));
                if (i < rcs.length - 1) sb.append(", ");
            }
            return sb.append(")").toString();
        } catch (Exception e) {
            return "/* TODO: " + record.getClass().getSimpleName() + " */ null";
        }
    }

    public static List<String> importsFor(Object value) {
        if (value == null) return List.of();
        List<String> base = switch (value) {
            case BigDecimal ignored    -> List.of("java.math.BigDecimal");
            case BigInteger ignored    -> List.of("java.math.BigInteger");
            case UUID ignored          -> List.of("java.util.UUID");
            case LocalDate ignored     -> List.of("java.time.LocalDate");
            case LocalDateTime ignored -> List.of("java.time.LocalDateTime");
            case LocalTime ignored     -> List.of("java.time.LocalTime");
            case Instant ignored       -> List.of("java.time.Instant");
            case ZonedDateTime ignored -> List.of("java.time.ZonedDateTime");
            case OffsetDateTime ignored-> List.of("java.time.OffsetDateTime");
            case Year ignored          -> List.of("java.time.Year");
            case YearMonth ignored     -> List.of("java.time.YearMonth");
            case Date ignored          -> List.of("java.util.Date");
            default                    -> List.of();
        };
        // For records: recurse into each component to collect transitive imports
        if (value.getClass().isRecord()) {
            java.util.Set<String> all = new java.util.LinkedHashSet<>(base);
            // Import the record's own class (handles nested types via enclosing class)
            all.addAll(importsForClass(value.getClass()));
            try {
                for (java.lang.reflect.RecordComponent rc : value.getClass().getRecordComponents()) {
                    Object v = rc.getAccessor().invoke(value);
                    all.addAll(importsFor(v));
                    all.addAll(importsForClass(rc.getType()));
                }
            } catch (Exception ignored2) {}
            return List.copyOf(all);
        }
        // For lists: recurse into each element
        if (value instanceof List<?> list) {
            java.util.Set<String> all = new java.util.LinkedHashSet<>(base);
            for (Object elem : list) all.addAll(importsFor(elem));
            return List.copyOf(all);
        }
        // For enums: import the enum class
        if (value instanceof Enum<?>) {
            java.util.Set<String> all = new java.util.LinkedHashSet<>(base);
            all.addAll(importsForClass(value.getClass()));
            return List.copyOf(all);
        }
        return base;
    }

    /**
     * Returns the import string for a class, walking up to the top-level enclosing class.
     * Returns empty list for java.lang, primitives, and java.util.List.
     */
    static List<String> importsForClass(Class<?> type) {
        if (type == null || type.isPrimitive()) return List.of();
        if (type.isArray()) return importsForClass(type.getComponentType());
        // Walk to top-level enclosing class
        Class<?> top = type;
        while (top.getEnclosingClass() != null) top = top.getEnclosingClass();
        String pkg = top.getPackageName();
        if (pkg.isEmpty() || pkg.equals("java.lang")) return List.of();
        if (top == java.util.List.class || top == java.util.Collection.class) return List.of(); // in FIXED imports
        return List.of(pkg + "." + top.getSimpleName());
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}