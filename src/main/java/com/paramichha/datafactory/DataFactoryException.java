package com.paramichha.datafactory;

/**
 * Thrown when DataFactory cannot build a valid or invalid instance.
 *
 * <p>This is always a runtime failure — it means DataFactory encountered
 * something it cannot handle. Common causes:
 * <ul>
 *   <li>The class has no supported instantiation strategy — no Lombok builder,
 *       no all-args constructor, no record constructor, no setters.</li>
 *   <li>A field references a type that has no registered value generator.</li>
 *   <li>A circular reference was detected between domain objects.</li>
 *   <li>A field name passed to {@code with()} or {@code invalidFor()} does
 *       not exist on the class.</li>
 * </ul>
 *
 * <p>Always inspect {@link #getCause()} — the wrapped exception explains
 * the exact reason for the failure.
 */
public class DataFactoryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorType errorType;

    public DataFactoryException(String message) {
        this(null, message, null);
    }

    public DataFactoryException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public DataFactoryException(ErrorType errorType, String message) {
        this(errorType, message, null);
    }

    public DataFactoryException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    /**
     * The failure category, or {@code null} when the thrower did not classify it.
     * Use {@link #errorTypeInChain(Throwable)} to find the first classified cause.
     */
    public ErrorType errorType() {
        return errorType;
    }

    /**
     * Returns the error type of the first {@code DataFactoryException} in the
     * cause chain that carries one, or {@code null} if none does.
     */
    public static ErrorType errorTypeInChain(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof DataFactoryException dfe && dfe.errorType != null) return dfe.errorType;
            if (c.getCause() == c) break;
        }
        return null;
    }
}
