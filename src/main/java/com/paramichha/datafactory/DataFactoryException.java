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

    public DataFactoryException(String message) {
        super(message);
    }

    public DataFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
