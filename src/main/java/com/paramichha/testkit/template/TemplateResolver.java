package com.paramichha.testkit.template;

/**
 * Helper for token substitutions used across writers.
 * Converts class/member names to the instance field name convention.
 */
public final class TemplateResolver {

    private TemplateResolver() {}

    /** "Circle" → "circle" — used as the class-level field name */
    public static String instanceFieldName(String className) {
        if (className == null || className.isEmpty()) return className;
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /** "name" → "Name" */
    public static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
