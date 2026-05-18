package com.paramichha.testkit.api;

import java.util.Map;

/**
 * Describes a single annotation instance.
 * Source-agnostic — works for reflection, PSI, or any other source.
 */
public interface AnnotationInfo {

    /** Simple name — e.g. "NotBlank", "Min". */
    String name();

    /** Fully qualified name — e.g. "jakarta.validation.constraints.NotBlank". */
    String qualifiedName();

    /** All attribute values keyed by attribute name. */
    Map<String, Object> attributes();

    /** Single attribute by name. Null if absent. */
    Object attribute(String name);

    /** The default "value" attribute — convenience for @Min(1), @Size(max=10) etc. */
    Object value();

    /** True if this is a Jakarta Bean Validation annotation. */
    boolean isValidation();
}
