package com.paramichha.testkit.scan;

import java.lang.annotation.*;

/**
 * Batch configuration — generate validation tests for multiple classes at once.
 * Place on any class (typically a dedicated config class).
 *
 * <pre>
 * {@literal @}Testable({
 *     UserRequest.class,
 *     PaymentRequest.class,
 *     RegisterRequest.class
 * })
 * class TestConfig {}
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Testables {

    /** Classes to generate tests for. */
    Class<?>[] value();

    /** Override the target package for all generated tests. Defaults to each class's own package. */
    String targetPackage() default "";
}
