package com.paramichha.datafactory.utils;

import com.paramichha.utils.Texts;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pure Java language naming conventions.
 *
 * <p>No business logic, no framework assumptions, no testing conventions.
 * Covers JavaBean accessors, class/field/method/package naming,
 * identifier validation and safe identifier generation.
 */
public final class JavaNames {

    private static final Pattern JAVA_IDENTIFIER =
            Pattern.compile("[A-Za-z_$][A-Za-z\\d_$]*");

    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while",
            "record", "sealed", "permits", "non-sealed", "var"
    );

    private JavaNames() {}

    // =========================================================================
    // JavaBean accessor naming
    // =========================================================================

    /** Returns "isActive" for boolean/Boolean, "getName" for everything else. */
    public static String getterName(String fieldName, Class<?> type) {
        String suffix = Texts.capitalize(fieldName);
        if (type == boolean.class || type == Boolean.class) return "is" + suffix;
        return "get" + suffix;
    }

    public static String setterName(String fieldName) {
        return "set" + Texts.capitalize(fieldName);
    }

    public static String witherName(String fieldName) {
        return "with" + Texts.capitalize(fieldName);
    }

    public static String adderName(String fieldName) {
        return "add" + Texts.capitalize(fieldName);
    }

    public static String removerName(String fieldName) {
        return "remove" + Texts.capitalize(fieldName);
    }

    // =========================================================================
    // Class / variable / method / package naming
    // =========================================================================

    /** "user request" → "UserRequest" */
    public static String className(String value) {
        if (Texts.isBlank(value)) return value;
        return Texts.pascalCase(clean(value));
    }

    /** "UserRequest" → "userRequest" */
    public static String variableName(String value) {
        if (Texts.isBlank(value)) return value;
        return Texts.camelCase(clean(value));
    }

    public static String variableName(Class<?> type) {
        return variableName(type.getSimpleName());
    }

    public static String fieldName(String value) {
        return variableName(value);
    }

    public static String parameterName(String value) {
        return variableName(value);
    }

    public static String parameterName(Class<?> type) {
        return variableName(type);
    }

    public static String methodName(String value) {
        return variableName(value);
    }

    /** "maxRetryCount" → "MAX_RETRY_COUNT" */
    public static String constantName(String value) {
        if (Texts.isBlank(value)) return value;
        return Texts.snakeCase(clean(value)).toUpperCase();
    }

    /** "com.example My Package" → "com.example.my.package" */
    public static String packageName(String value) {
        if (Texts.isBlank(value)) return value;
        return value.trim()
                .replaceAll("[^a-zA-Z0-9. ]", "")
                .replaceAll("[\\s-]+", ".")
                .toLowerCase();
    }

    // =========================================================================
    // Identifier validation and safe naming
    // =========================================================================

    public static boolean isValidIdentifier(String value) {
        if (Texts.isBlank(value)) return false;
        return JAVA_IDENTIFIER.matcher(value).matches()
                && !JAVA_KEYWORDS.contains(value);
    }

    public static boolean isKeyword(String value) {
        return JAVA_KEYWORDS.contains(value);
    }

    /**
     * Converts any string into a valid Java identifier.
     * Appends "Value" if the result is a keyword.
     */
    public static String safeIdentifier(String value) {
        if (Texts.isBlank(value)) return "_";
        String cleaned = clean(value).replaceAll("[^A-Za-z\\d_$]", "");
        if (cleaned.isBlank()) cleaned = "_";
        if (Character.isDigit(cleaned.charAt(0))) cleaned = "_" + cleaned;
        if (JAVA_KEYWORDS.contains(cleaned)) cleaned = cleaned + "Value";
        return cleaned;
    }

    // =========================================================================
    // Internal
    // =========================================================================

    private static String clean(String value) {
        return value.replaceAll("[^a-zA-Z0-9_$\\- ]", " ").trim();
    }
}
