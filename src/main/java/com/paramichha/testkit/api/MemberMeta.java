package com.paramichha.testkit.api;

import java.util.List;

/**
 * Describes a field of a class or record.
 * Structure only — no generated values.
 */
public interface MemberMeta {

    String name();

    TypeInfo typeInfo();

    Visibility visibility();

    /** Jakarta validation annotations on this field. */
    List<AnnotationInfo> validations();

    boolean isConstrained();

    boolean isRequired();       // has @NotNull / @NotBlank / @NotEmpty

    boolean hasGetter();

    boolean hasSetter();

    /** Computed accessor name — getName(), isActive() etc. */
    String accessorName();
}
