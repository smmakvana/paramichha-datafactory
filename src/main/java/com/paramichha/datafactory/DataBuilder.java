package com.paramichha.datafactory;

import java.util.List;
import java.util.Map;

/**
 * Fluent API for producing valid and invalid instances of an annotated class.
 *
 * <pre>
 * // One fully-valid object
 * RegisterRequest req = DataFactory.of(RegisterRequest.class).valid();
 *
 * // Boundary-covering valid objects per field
 * Map&lt;String, List&lt;RegisterRequest&gt;&gt; variants =
 *         DataFactory.of(RegisterRequest.class).validAll();
 *
 * // All valid except one field
 * RegisterRequest req = DataFactory.of(RegisterRequest.class).invalidFor("email");
 *
 * // One ViolationScenario per constraint per field — drives @ParameterizedTest
 * List&lt;ViolationScenario&lt;RegisterRequest&gt;&gt; scenarios =
 *         DataFactory.of(RegisterRequest.class).violations();
 *
 * // Override a field before building
 * RegisterRequest req = DataFactory.of(RegisterRequest.class).with("age", 25).valid();
 * </pre>
 */
public interface DataBuilder<T> {

    /**
     * Overrides a field value before building.
     */
    DataBuilder<T> with(String fieldName, Object value);

    /**
     * Returns one fully-valid object using semantic defaults for every field.
     */
    T valid();

    /**
     * Returns boundary-covering valid objects keyed by field name.
     * Fields with only one valid value and boolean fields are omitted.
     */
    Map<String, List<T>> validAll();

    /**
     * Returns one object where the named field holds its first invalid value.
     */
    T invalidFor(String fieldName);

    /**
     * Returns one {@link ViolationScenario} per annotation-driven constraint per field.
     * Every scenario is guaranteed to produce a Jakarta constraint violation.
     */
    List<ViolationScenario<T>> violations();
}
