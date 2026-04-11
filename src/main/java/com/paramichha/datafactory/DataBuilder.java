package com.paramichha.datafactory;

import java.util.List;
import java.util.Map;

/**
 * Fluent builder for generating valid and invalid instances of a Jakarta-annotated class.
 *
 * <p>Obtained via {@link DataFactory#of(Class)}. Configuration methods return
 * {@code this} so calls can be chained before calling a terminal method.
 *
 * <h2>Common patterns</h2>
 * <pre>
 * // One valid object — most common use case
 * UserRequest req = DataFactory.of(UserRequest.class).valid();
 *
 * // Override one field, keep everything else valid
 * UserRequest req = DataFactory.of(UserRequest.class)
 *                              .with("age", 17)
 *                              .valid();
 *
 * // Null one field
 * UserRequest req = DataFactory.of(UserRequest.class)
 *                              .withNull("address")
 *                              .valid();
 *
 * // All objects that violate constraints — one per constraint per field
 * List&lt;UserRequest&gt; bad = DataFactory.of(UserRequest.class).invalidList();
 *
 * // 1000 valid objects for load testing
 * List&lt;UserRequest&gt; bulk = DataFactory.of(UserRequest.class).stream(1000);
 *
 * // Boundary-covering valid objects per field — used by TestKit
 * Map&lt;String, List&lt;UserRequest&gt;&gt; edges = DataFactory.of(UserRequest.class).validMap();
 * </pre>
 *
 * @param <T> the type being built
 * @see DataFactory#of(Class)
 * @see GenerationMode
 */
public interface DataBuilder<T> {

    /**
     * Overrides one field with the given value before building.
     * The value bypasses all constraint checks — use it to set
     * intentionally invalid values, or to pin a field to a specific value.
     * <pre>
     * // Force age to an invalid value
     * UserRequest underage = DataFactory.of(UserRequest.class).with("age", 15).valid();
     *
     * // Pin email to a known value, generate everything else
     * UserRequest req = DataFactory.of(UserRequest.class)
     *                              .with("email", "test@mycompany.com")
     *                              .valid();
     * </pre>
     */
    DataBuilder<T> with(String fieldName, Object value);

    /**
     * Overrides one field with {@code null}.
     * Shorthand for {@code .with(fieldName, null)}.
     * <pre>
     * // Build with no address — useful for testing optional fields
     * UserRequest req = DataFactory.of(UserRequest.class).withNull("address").valid();
     * </pre>
     */
    DataBuilder<T> withNull(String fieldName);

    /**
     * Sets the generation mode.
     * Defaults to {@link GenerationMode#PRODUCTION} when not called.
     *
     * <p>In most cases you do not need to call this — PRODUCTION mode is
     * the right default for tests. Switch to DEV when your class is still
     * being annotated and you want to explore all technically-valid values.
     *
     * @see GenerationMode
     */
    DataBuilder<T> mode(GenerationMode mode);

    /**
     * Seeds the random generator for deterministic output.
     * Same seed on the same class always produces identical field values.
     *
     * <p>Useful for snapshot tests where you want the generated values
     * to remain stable across runs, and for TestKit which seeds from
     * the class's fully-qualified name so generated test files stay
     * consistent in version control.
     */
    DataBuilder<T> seed(long seed);

    /**
     * Builds one valid instance satisfying all constraints.
     *
     * @throws DataFactoryException if the class cannot be instantiated,
     *         no suitable constructor or builder was found, or a field
     *         type has no registered value generator
     */
    T valid();

    /**
     * Builds {@code n} valid instances.
     * Each call to {@code valid()} is independent — values vary between instances.
     *
     * <p>The primary tool for load test data and bulk fixtures:
     * <pre>
     * List&lt;UserRequest&gt; users = DataFactory.of(UserRequest.class).stream(1000);
     * </pre>
     *
     * @throws DataFactoryException if any instance cannot be built
     */
    List<T> stream(int n);

    /**
     * Builds one invalid instance for the named field.
     * All other fields hold valid values — only the named field is intentionally wrong.
     *
     * <p>Useful when you want to test how your system handles a specific invalid input:
     * <pre>
     * // Submit a request with an invalid email, everything else fine
     * UserRequest badEmail = DataFactory.of(UserRequest.class).invalidFor("email");
     * assertThat(validator.validate(badEmail))
     *     .anyMatch(v -&gt; v.getPropertyPath().toString().equals("email"));
     * </pre>
     *
     * @throws DataFactoryException if the field name does not exist on the class
     */
    T invalidFor(String fieldName);

    /**
     * Builds one invalid instance per constraint per field.
     * Each object in the returned list violates exactly one constraint and passes all others.
     *
     * <p>This is the basis of constraint-by-constraint validation testing:
     * <pre>
     * {@literal @}ParameterizedTest
     * {@literal @}MethodSource("invalidRequests")
     * void each_invalid_request_fails_validation(UserRequest req) {
     *     assertThat(validator.validate(req)).isNotEmpty();
     * }
     *
     * static List&lt;UserRequest&gt; invalidRequests() {
     *     return DataFactory.of(UserRequest.class).invalidList();
     * }
     * </pre>
     *
     * @throws DataFactoryException if the class cannot be instantiated
     */
    List<T> invalidList();

    /**
     * Returns boundary-covering valid instances, keyed by field name.
     *
     * <p>For each field, the map contains a list of valid objects where that field
     * covers its boundary values — minimum, midpoint, maximum, and a semantically
     * realistic value. All other fields hold their canonical valid value.
     *
     * <p>This is primarily used by TestKit to generate parameterized boundary tests.
     * In application code, {@link #valid()} and {@link #stream(int)} cover most needs.
     *
     * @throws DataFactoryException if the class cannot be instantiated
     */
    Map<String, List<T>> validMap();
}
