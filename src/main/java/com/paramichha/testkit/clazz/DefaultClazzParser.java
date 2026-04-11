package com.paramichha.testkit.clazz;

/**
 * Resolves Java type name strings — primitives and common types — to their runtime {@link Class}.
 *
 * <pre>
 * parser.parse("int")        → int.class
 * parser.parse("Integer")    → Integer.class
 * parser.parse("String")     → String.class
 * parser.parse("LocalDate")  → LocalDate.class
 * </pre>
 *
 * Throws {@link ClazzParserException} for null, blank, or unrecognised type names.
 */
public final class DefaultClazzParser implements ClazzParser {

    @Override
    public Class<?> parse(String type) throws ClazzParserException {
        if (type == null || type.isBlank()) {
            throw new ClazzParserException("Type name must not be null or blank");
        }
        return switch (type.trim()) {
            // primitives
            case "int"     -> int.class;
            case "long"    -> long.class;
            case "double"  -> double.class;
            case "float"   -> float.class;
            case "boolean" -> boolean.class;
            case "char"    -> char.class;
            case "short"   -> short.class;
            case "byte"    -> byte.class;
            // wrappers
            case "Integer"   -> Integer.class;
            case "Long"      -> Long.class;
            case "Double"    -> Double.class;
            case "Float"     -> Float.class;
            case "Boolean"   -> Boolean.class;
            case "Character" -> Character.class;
            case "Short"     -> Short.class;
            case "Byte"      -> Byte.class;
            // common types
            case "String"     -> String.class;
            case "BigDecimal" -> java.math.BigDecimal.class;
            case "BigInteger" -> java.math.BigInteger.class;
            // temporal
            case "LocalDate"      -> java.time.LocalDate.class;
            case "LocalDateTime"  -> java.time.LocalDateTime.class;
            case "LocalTime"      -> java.time.LocalTime.class;
            case "ZonedDateTime"  -> java.time.ZonedDateTime.class;
            case "OffsetDateTime" -> java.time.OffsetDateTime.class;
            case "Instant"        -> java.time.Instant.class;
            case "Year"           -> java.time.Year.class;
            case "YearMonth"      -> java.time.YearMonth.class;
            // other
            case "UUID"       -> java.util.UUID.class;
            case "Date"       -> java.util.Date.class;
            case "List"       -> java.util.List.class;
            case "Collection" -> java.util.Collection.class;
            default -> throw new ClazzParserException("Unrecognised type: '" + type + "'");
        };
    }
}
