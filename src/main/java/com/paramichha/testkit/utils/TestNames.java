package com.paramichha.testkit.utils;

import com.paramichha.utils.Texts;

/**
 * TestKit and code-generator naming conventions.
 *
 * <p>All class names, display names, data source names and instance
 * field names used in generated test code are computed here.
 * No naming logic should exist inline in writers.
 *
 * <p>Also used by DevBoost/KickStart for generated class naming.
 */
public final class TestNames {

    private TestNames() {}

    // =========================================================================
    // String helpers — used by both TestKit and DevBoost
    // =========================================================================

    /** "Circle" → "circle" */
    public static String toVariable(String className) {
        return Texts.decapitalize(className);
    }

    /** "Circle" → "circle" */
    public static String toVariable(Class<?> type) {
        return Texts.decapitalize(type.getSimpleName());
    }

    /** "radius" → "Radius" */
    public static String toClassName(String value) {
        return JavaNames.className(value);
    }

    /** "roles" → "role", "addresses" → "address" */
    public static String singular(String value) {
        if (Texts.isBlank(value)) return value;
        if (value.endsWith("ies")) return value.substring(0, value.length() - 3) + "y";
        if (value.endsWith("ses")) return value.substring(0, value.length() - 2);
        if (value.endsWith("s") && value.length() > 1) return value.substring(0, value.length() - 1);
        return value;
    }

    /** "role" → "roles", "address" → "addresses" */
    public static String pluralize(String value) {
        if (Texts.isBlank(value)) return value;
        if (value.endsWith("y") && value.length() > 1 && "aeiouAEIOU".indexOf(value.charAt(value.length() - 2)) < 0) {
            return value.substring(0, value.length() - 1) + "ies";
        }
        if (value.endsWith("s") || value.endsWith("x") || value.endsWith("z") || value.endsWith("ch") || value.endsWith("sh")) {
            return value + "es";
        }
        return value + "s";
    }

    // =========================================================================
    // Generated test class names
    // =========================================================================

    /** "Circle" → "CircleTest" */
    public static String testClass(String className) {
        return className + "Test";
    }

    /** "Circle" → "CircleCreationTest" */
    public static String creationClass(String className) {
        return className + "CreationTest";
    }

    /** "Circle", "radius" → "CircleRadiusTest" */
    public static String memberClass(String className, String memberName) {
        return className + JavaNames.className(memberName) + "Test";
    }

    /** "Circle", "resize" → "CircleMethodResizeTest" */
    public static String methodClass(String className, String methodName) {
        return className + "Method" + JavaNames.className(methodName) + "Test";
    }

    /** "Circle" → "CircleScenarioTest" */
    public static String scenarioClass(String className) {
        return className + "ScenarioTest";
    }

    // =========================================================================
    // Data source method names
    // =========================================================================

    /** "radius" → "validRadii" / "validRoles" etc */
    public static String validSource(String memberName) {
        return "valid" + JavaNames.className(pluralize(memberName));
    }

    /** "radius" → "invalidRadii" / "invalidRoles" etc */
    public static String invalidSource(String memberName) {
        return "invalid" + JavaNames.className(pluralize(memberName));
    }

    /** "resize" → "validResizeInputs" */
    public static String validMethodSource(String methodName) {
        return "valid" + JavaNames.className(methodName) + "Inputs";
    }

    /** "resize" → "invalidResizeInputs" */
    public static String invalidMethodSource(String methodName) {
        return "invalid" + JavaNames.className(methodName) + "Inputs";
    }

    // =========================================================================
    // Display names — used in @DisplayName annotations
    // =========================================================================

    /** "Circle" → "Circle object creation" */
    public static String creationDisplay(String className) {
        return className + " object creation";
    }

    /** "radius", "Circle" → "radius in Circle" */
    public static String memberDisplay(String memberName, String className) {
        return memberName + " in " + className;
    }

    /** "resize", "Circle" → "resize in Circle" */
    public static String methodDisplay(String methodName, String className) {
        return methodName + " in " + className;
    }

    /** "Circle" → "Circle scenarios" */
    public static String scenarioDisplay(String className) {
        return className + " scenarios";
    }

    /** "circle" → "circle can be created" */
    public static String canBeCreatedDisplay(String fieldName) {
        return fieldName + " can be created";
    }

    /** "circle" → "circle holds expected values" */
    public static String holdsExpectedDisplay(String fieldName) {
        return fieldName + " holds expected values";
    }

    /** "circle" → "circle is valid" */
    public static String isValidDisplay(String fieldName) {
        return fieldName + " is valid";
    }

    /** "radius" → "radius accepts valid value" */
    public static String acceptsDisplay(String memberName) {
        return memberName + " accepts valid value";
    }

    /** "radius" → "radius rejects invalid value" */
    public static String rejectsDisplay(String memberName) {
        return memberName + " rejects invalid value";
    }

    /** "resize" → "resize accepts valid input" */
    public static String acceptsInputDisplay(String methodName) {
        return methodName + " accepts valid input";
    }

    /** "resize" → "resize rejects invalid input" */
    public static String rejectsInputDisplay(String methodName) {
        return methodName + " rejects invalid input";
    }

    /** "resize" → "resize returns a result" */
    public static String returnsResultDisplay(String methodName) {
        return methodName + " returns a result";
    }

    /** "resize" → "resize returns consistent result" */
    public static String returnsConsistentDisplay(String methodName) {
        return methodName + " returns consistent result";
    }
}
