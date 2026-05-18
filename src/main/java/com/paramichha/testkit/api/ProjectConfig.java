package com.paramichha.testkit.api;

/**
 * Configuration that controls how tests are generated.
 * Passed through the pipeline — read by CodeWriter for assertion style decisions.
 *
 * Replaces the scattered ProjectType enum.
 * Auto-detected from classpath or constructed manually.
 */
public final class ProjectConfig {

    public enum Environment {
        SPRING,      // Spring on classpath — validator at boundaries
        JAKARTA,     // Hibernate Validator present, no Spring
        PLAIN_JAVA   // no validator — DataValidation only
    }

    private final Environment environment;
    private final String      targetPackageSuffix;  // appended to source package, default ""

    private ProjectConfig(Environment environment, String targetPackageSuffix) {
        this.environment         = environment;
        this.targetPackageSuffix = targetPackageSuffix;
    }

    // ── Factory ────────────────────────────────────────────────────────────

    public static ProjectConfig detect() {
        Environment env = isOnClasspath("org.springframework.context.ApplicationContext") ? Environment.SPRING
                        : isOnClasspath("org.hibernate.validator.HibernateValidator")    ? Environment.JAKARTA
                        : Environment.PLAIN_JAVA;
        return new ProjectConfig(env, "");
    }

    public static ProjectConfig spring()     { return new ProjectConfig(Environment.SPRING,     ""); }
    public static ProjectConfig jakarta()    { return new ProjectConfig(Environment.JAKARTA,    ""); }
    public static ProjectConfig plainJava()  { return new ProjectConfig(Environment.PLAIN_JAVA, ""); }

    // ── Fluent customisation ──────────────────────────────────────────────

    public ProjectConfig withTargetPackageSuffix(String suffix) {
        return new ProjectConfig(this.environment, suffix);
    }

    // ── Queries ────────────────────────────────────────────────────────────

    public Environment environment()          { return environment; }
    public boolean     hasValidator()         { return environment != Environment.PLAIN_JAVA; }
    public boolean     isSpring()             { return environment == Environment.SPRING; }
    public boolean     isPlainJava()          { return environment == Environment.PLAIN_JAVA; }
    public String      targetPackageSuffix()  { return targetPackageSuffix; }

    // ── Internal ──────────────────────────────────────────────────────────

    private static boolean isOnClasspath(String className) {
        try { Class.forName(className); return true; }
        catch (ClassNotFoundException e) { return false; }
    }
}
