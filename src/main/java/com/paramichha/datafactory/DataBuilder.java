package com.paramichha.datafactory;

import java.util.Map;

/**
 * Fluent builder for generating valid and invalid instances of a Jakarta-annotated class.
 *
 * <p>Single results return {@link DataFactoryResult}.
 * Bulk results return {@link DataFactoryResults}.
 * Neither ever throws.
 *
 * <h2>Usage</h2>
 * <pre>
 * // Single valid
 * DataFactoryResult&lt;UserRequest&gt; result = DataFactory.of(UserRequest.class).valid();
 * UserRequest req = result.get();
 *
 * // Bulk valid — same processing every time
 * DataFactoryResults&lt;UserRequest&gt; bulk = DataFactory.of(UserRequest.class).validList(1000);
 * bulk.succeeded().forEach(r -> use(r.get()));
 * bulk.failed().forEach(r -> log(r.errorType() + ": " + r.reason()));
 *
 * // Invalid per field
 * DataFactoryResults&lt;UserRequest&gt; invalid = DataFactory.of(UserRequest.class).invalidList();
 * if (invalid.isFailure() && invalid.failed().get(0).errorType() == ErrorType.UNSUPPORTED_ANNOTATION) ...
 * </pre>
 *
 * @param <T> the type being built
 */
public interface DataBuilder<T> {

    // ── Configuration ─────────────────────────────────────────────────────────

    DataBuilder<T> with(String fieldName, Object value);
    DataBuilder<T> withNull(String fieldName);
    DataBuilder<T> mode(GenerationMode mode);
    DataBuilder<T> seed(long seed);

    // ── Single result ─────────────────────────────────────────────────────────

    /** Builds one valid instance. Returns {@link ResultStatus#FAILED} on any error. */
    DataFactoryResult<T> valid();

    /** Builds one invalid instance. Returns {@link ResultStatus#FAILED} with {@link ErrorType#INVALID_CONFIGURATION} if no constraints to violate. */
    DataFactoryResult<T> invalid();

    /** Builds one invalid instance for the named field only. */
    DataFactoryResult<T> invalidFor(String fieldName);

    // ── Bulk result ───────────────────────────────────────────────────────────

    /** Builds {@code count} valid instances. {@link ResultStatus} is SUCCESS, PARTIAL, or FAILED. */
    DataFactoryResults<T> validList(int count);

    /**
     * Builds one invalid instance per constraint per field.
     * Returns {@link ResultStatus#FAILED} with {@link ErrorType#INVALID_CONFIGURATION} if nothing to violate.
     */
    DataFactoryResults<T> invalidList();

    /**
     * Returns boundary-covering valid instances keyed by field name.
     * Each field maps to its own {@link DataFactoryResults}.
     */
    Map<String, DataFactoryResults<T>> validPerField();

    // ── Per-field ─────────────────────────────────────────────────────────────

    /**
     * Valid and invalid values for every field, each generated independently — no
     * instance of {@code T} is built. A field that cannot be generated (unsupported
     * type, conflicting annotations) yields a failed result for that field only;
     * every other field still succeeds.
     *
     * <p>Keys follow field declaration order. Seeded builders are deterministic per
     * field: a field's values do not change when other fields are added or removed.
     */
    Map<String, DataFactoryResult<FieldCases>> fields();
}
