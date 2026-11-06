package com.paramichha.datafactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The result of a single DataFactory generation attempt.
 *
 * <p>Never throws — check {@link #isSuccess()} or {@link #status()} before accessing payload.
 *
 * <h2>Usage</h2>
 * <pre>
 * DataFactoryResult&lt;UserRequest&gt; result = DataFactory.of(UserRequest.class).valid();
 *
 * // Safe checks
 * if (result.isSuccess())  UserRequest req = result.get();
 * if (result.isFailure())  log(result.errorType() + ": " + result.reason());
 *
 * // Convenience
 * UserRequest req = result.getOrElse(fallback);
 * Optional&lt;UserRequest&gt; opt = result.toOptional();
 * </pre>
 *
 * <p>For bulk results see {@link DataFactoryResults}.
 *
 * @param <T> the payload type
 */
public final class DataFactoryResult<T> {

    private final T           payload;
    private final ResultStatus status;
    private final ErrorType   errorType;
    private final String      reason;

    private DataFactoryResult(T payload, ResultStatus status, ErrorType errorType, String reason) {
        this.payload   = payload;
        this.status    = status;
        this.errorType = errorType;
        this.reason    = reason;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /** Creates a successful result. Payload must not be null. */
    public static <T> DataFactoryResult<T> success(T payload) {
        Objects.requireNonNull(payload, "success payload must not be null");
        return new DataFactoryResult<>(payload, ResultStatus.SUCCESS, null, null);
    }

    /** Creates a failed result. Payload will be null. */
    public static <T> DataFactoryResult<T> failure(ErrorType errorType, String reason) {
        Objects.requireNonNull(errorType, "errorType must not be null");
        Objects.requireNonNull(reason,    "reason must not be null");
        return new DataFactoryResult<>(null, ResultStatus.FAILED, errorType, reason);
    }

    // ── State checks ──────────────────────────────────────────────────────────

    public boolean isSuccess() { return status == ResultStatus.SUCCESS; }
    public boolean isFailure() { return !isSuccess(); }

    // ── Payload access ────────────────────────────────────────────────────────

    /**
     * Returns the payload.
     * @throws DataFactoryException if this result is a failure
     */
    public T get() {
        if (isFailure()) throw new DataFactoryException(errorType,
                "DataFactory [" + errorType + "]: " + reason);
        return payload;
    }

    /** Returns the payload, or {@code null} if failed. */
    public T getOrNull() { return payload; }

    /** Returns the payload if successful, or {@code defaultValue} if failed. */
    public T getOrElse(T defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue must not be null");
        return isSuccess() ? payload : defaultValue;
    }

    /** Maps the payload if successful, propagates failure otherwise. */
    public <U> DataFactoryResult<U> map(Function<T, U> mapper) {
        Objects.requireNonNull(mapper, "mapper must not be null");
        if (isFailure()) return DataFactoryResult.failure(errorType, reason);
        U mapped = mapper.apply(payload);
        return mapped != null
                ? DataFactoryResult.success(mapped)
                : DataFactoryResult.failure(ErrorType.INTERNAL_ERROR,
                        "mapper returned null for payload: " + payload);
    }

    /** Bridges to {@link Optional}. */
    public Optional<T> toOptional() {
        return isSuccess() ? Optional.of(payload) : Optional.empty();
    }

    // ── Status ────────────────────────────────────────────────────────────────

    /** Always present. {@link ResultStatus#SUCCESS} or {@link ResultStatus#FAILED}. */
    public ResultStatus status()    { return status; }

    /** Null on success. Classification of why generation failed. */
    public ErrorType errorType()    { return errorType; }

    /** Null on success. Human readable explanation of the failure. */
    public String reason()          { return reason; }

    // ── Standard overrides ────────────────────────────────────────────────────

    @Override
    public String toString() {
        return isSuccess()
                ? "DataFactoryResult[SUCCESS, payload=" + payload + "]"
                : "DataFactoryResult[FAILED, errorType=" + errorType + ", reason=" + reason + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataFactoryResult<?> other)) return false;
        return status    == other.status
            && errorType == other.errorType
            && Objects.equals(payload, other.payload)
            && Objects.equals(reason,  other.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload, status, errorType, reason);
    }
}
