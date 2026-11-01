package com.paramichha.datafactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The result of a bulk DataFactory generation operation.
 *
 * <p>Contains one {@link DataFactoryResult} per requested item.
 * {@link ResultStatus} is derived automatically:
 * <ul>
 *   <li>{@link ResultStatus#SUCCESS} — all items succeeded</li>
 *   <li>{@link ResultStatus#PARTIAL} — some succeeded, some failed</li>
 *   <li>{@link ResultStatus#FAILED}  — all failed</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>
 * DataFactoryResults&lt;UserRequest&gt; bulk = DataFactory.of(UserRequest.class).validList(1000);
 *
 * bulk.status();                        // SUCCESS, PARTIAL, FAILED
 * bulk.succeeded();                     // all successful results
 * bulk.failed();                        // all failed results
 * bulk.results();                       // every result — success and failure
 *
 * // Process same way every time
 * bulk.succeeded().forEach(r -> use(r.get()));
 * bulk.failed().forEach(r -> log(r.errorType() + ": " + r.reason()));
 * </pre>
 *
 * @param <T> the payload type
 */
public final class DataFactoryResults<T> {

    private final List<DataFactoryResult<T>> results;
    private final ResultStatus               status;

    private DataFactoryResults(List<DataFactoryResult<T>> results) {
        this.results = Collections.unmodifiableList(results);
        this.status  = deriveStatus(results);
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static <T> DataFactoryResults<T> of(List<DataFactoryResult<T>> results) {
        Objects.requireNonNull(results, "results must not be null");
        return new DataFactoryResults<>(results);
    }

    // ── Status — derived, never set manually ──────────────────────────────────

    /**
     * Overall status derived from individual results:
     * all success → SUCCESS, all failed → FAILED, mixed → PARTIAL.
     */
    public ResultStatus status() { return status; }

    public boolean isSuccess() { return status == ResultStatus.SUCCESS; }
    public boolean isPartial() { return status == ResultStatus.PARTIAL; }
    public boolean isFailure() { return status == ResultStatus.FAILED;  }

    // ── Access ────────────────────────────────────────────────────────────────

    /** Every result — both successful and failed. */
    public List<DataFactoryResult<T>> results() { return results; }

    /** Only successful results. */
    public List<DataFactoryResult<T>> succeeded() {
        return results.stream().filter(DataFactoryResult::isSuccess).toList();
    }

    /** Only failed results. */
    public List<DataFactoryResult<T>> failed() {
        return results.stream().filter(DataFactoryResult::isFailure).toList();
    }

    /** Count of successful results. */
    /**
     * All payloads, in order. Throws {@link DataFactoryException} naming the first
     * failure if any result failed — the bulk counterpart of {@link DataFactoryResult#get()}.
     */
    public List<T> getAll() {
        for (DataFactoryResult<T> r : results) {
            if (r.isFailure()) throw new DataFactoryException(r.errorType(),
                    "DataFactory [" + r.errorType() + "]: " + r.reason()
                    + " (" + failureCount() + " of " + total() + " results failed)");
        }
        return results.stream().map(DataFactoryResult::get).toList();
    }

    public int successCount() { return (int) results.stream().filter(DataFactoryResult::isSuccess).count(); }

    /** Count of failed results. */
    public int failureCount() { return (int) results.stream().filter(DataFactoryResult::isFailure).count(); }

    /** Total results — succeeded + failed. */
    public int total() { return results.size(); }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static <T> ResultStatus deriveStatus(List<DataFactoryResult<T>> results) {
        if (results.isEmpty()) return ResultStatus.FAILED;
        long successCount = results.stream().filter(DataFactoryResult::isSuccess).count();
        if (successCount == results.size()) return ResultStatus.SUCCESS;
        if (successCount == 0)             return ResultStatus.FAILED;
        return ResultStatus.PARTIAL;
    }

    // ── Standard overrides ────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "DataFactoryResults[status=" + status
                + ", total=" + total()
                + ", succeeded=" + successCount()
                + ", failed=" + failureCount() + "]";
    }
}
