package com.paramichha.testkit.scan;

import java.lang.annotation.*;

/**
 * Marks a Jakarta-annotated class for automatic JUnit 5 validation test generation.
 *
 * <pre>
 * {@literal @}Testable
 * public record UserRequest(
 *     {@literal @}NotBlank {@literal @}Email String email,
 *     {@literal @}NotNull {@literal @}Min(18) Integer age
 * ) {}
 * </pre>
 *
 * Run {@code TestKit.scanPackage("com.example").writeTo(Path.of("src/test/java"))}
 * to generate {@code UserRequestTest.java}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Testable {

    /** Override the target package for the generated test. Defaults to class package. */
    String targetPackage() default "";
}
