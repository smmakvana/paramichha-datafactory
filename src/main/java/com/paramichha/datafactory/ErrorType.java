package com.paramichha.datafactory;

/**
 * Classifies why a DataFactory generation failed.
 * Present on {@link DataFactoryResult} when {@link ResultStatus} is {@code FAILED}.
 */
public enum ErrorType {

    /** The class or field type has no registered creator in DataFactory. */
    UNSUPPORTED_CLASS_TYPE,

    /** One or more annotations on the field are not supported by DataFactory. */
    UNSUPPORTED_ANNOTATION,

    /** Annotation constraints conflict — e.g. {@code @Min(5)} and {@code @Max(3)} produce an impossible range. */
    INVALID_CONFIGURATION,

    /** The type cannot be instantiated — interface, abstract class, or non-static inner class. */
    NOT_INSTANTIABLE,

    /** A field name passed to the builder (e.g. {@code invalidFor("x")}) does not exist on the class. */
    UNKNOWN_FIELD,

    /** DataFactory failed internally. Check {@link DataFactoryResult#reason()} for details. */
    INTERNAL_ERROR
}
