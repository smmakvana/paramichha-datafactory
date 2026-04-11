package com.paramichha.datafactory;

/**
 * Thrown when an annotation string passed to {@link FieldBuilder#with(String)} cannot be parsed.
 *
 * <p>This is a programmer error — it means the annotation string is malformed.
 * The string must start with {@code @} and follow Jakarta annotation syntax:
 * <pre>
 * .with("@Min(18)")            // correct
 * .with("@Size(min=2, max=50)") // correct
 * .with("Min(18)")             // wrong — missing @
 * .with("@Min(abc)")           // wrong — value is not a number
 * </pre>
 *
 * <p>DataFactory does not wrap this in {@link DataFactoryException} because
 * it is always a coding mistake, not a runtime condition. Fix the annotation
 * string and it will not recur.
 */
public class AnnotationParseException extends RuntimeException {

    public AnnotationParseException(String message) {
        super(message);
    }

    public AnnotationParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
