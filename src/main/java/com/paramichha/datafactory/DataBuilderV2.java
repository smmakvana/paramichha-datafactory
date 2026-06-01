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
 * DataFactoryResult&lt;UserRequest&gt; result = DataFactoryV2.of(UserRequest.class).valid();
 * UserRequest req = result.get();
 *
 * // Bulk valid — same processing every time
 * DataFactoryResults&lt;UserRequest&gt; bulk = DataFactoryV2.of(UserRequest.class).validList(1000);
 * bulk.succeeded().forEach(r -> use(r.get()));
 * bulk.failed().forEach(r -> log(r.errorType() + ": " + r.reason()));
 *
 * // Invalid per field
 * DataFactoryResults&lt;UserRequest&gt; invalid = DataFactoryV2.of(UserRequest.class).invalidList();
 * if (invalid.isFailure() && invalid.failed().get(0).errorType() == ErrorType.UNSUPPORTED_ANNOTATION) ...
 * </pre>
 *
 * @param <T> the type being built
 */
public interface DataBuilderV2<T> {

    // ── Configuration ─────────────────────────────────────────────────────────

    DataBuilderV2<T> with(String fieldName, Object value);
    DataBuilderV2<T> withNull(String fieldName);
    DataBuilderV2<T> mode(GenerationMode mode);
    DataBuilderV2<T> seed(long seed);

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
}
