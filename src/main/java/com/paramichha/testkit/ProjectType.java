package com.paramichha.testkit;

/**
 * Describes the target project environment.
 *
 * <p>Detected automatically at generation time via classpath inspection.
 * Override via {@code TestKit.of(MyClass.class).withProjectType(ProjectType.PLAIN_JAVA)}.
 *
 * <p>Controls what gets generated:
 * <pre>
 * SPRING      — Spring on classpath; validator used for object state; DataValidation for return assertions
 * JAKARTA     — Hibernate Validator present, no Spring; validator for object state; DataValidation for guards
 * PLAIN_JAVA  — No validator; DataValidation everywhere; no validator field generated
 * </pre>
 */
public enum ProjectType {

    /**
     * Spring / Spring Boot project.
     * Validator available. Container enforces @Valid at boundaries.
     * Method guards still need DataValidation — Spring doesn't validate inside methods.
     */
    SPRING,

    /**
     * Plain Jakarta project — Hibernate Validator on classpath but no Spring.
     * Validator available for object state assertions.
     * Method guards need DataValidation.
     */
    JAKARTA,

    /**
     * Pure plain Java — no Hibernate Validator on classpath.
     * DataValidation is the only assertion tool.
     * No validator field generated. No validator.validate() calls.
     */
    PLAIN_JAVA;

    // ── Auto-detection ────────────────────────────────────────────────────

    /**
     * Detects project type from the runtime classpath.
     * Called once at generation time.
     */
    public static ProjectType detect() {
        if (isOnClasspath("org.springframework.context.ApplicationContext")) return SPRING;
        if (isOnClasspath("org.hibernate.validator.HibernateValidator"))     return JAKARTA;
        return PLAIN_JAVA;
    }

    public boolean hasValidator() {
        return this == SPRING || this == JAKARTA;
    }

    public boolean isSpring() {
        return this == SPRING;
    }

    public boolean isPlainJava() {
        return this == PLAIN_JAVA;
    }

    private static boolean isOnClasspath(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}