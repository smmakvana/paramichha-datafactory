package com.paramichha.datafactory.constraint;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects annotation combinations on a single field that no value can satisfy.
 *
 * <p>Runs on the resolved {@link FieldConstraints} before any generation, so an
 * impossible field is reported as a configuration problem instead of surfacing
 * later as a generation or validation failure.
 *
 * <p>Detected combinations:
 * <ul>
 *   <li>{@code @Null} together with {@code @NotNull}, {@code @NotBlank} or {@code @NotEmpty}</li>
 *   <li>{@code @Null} on a primitive (a primitive can never be null)</li>
 *   <li>a lower bound above the upper bound — {@code @Min(5) @Max(3)},
 *       {@code @Size(min = 5, max = 3)}, {@code @Positive @Negative},
 *       {@code @DecimalMin("2.0") @DecimalMax("1.0")}</li>
 *   <li>{@code @AssertTrue} together with {@code @AssertFalse}</li>
 * </ul>
 */
public final class ConstraintConflicts {

    private ConstraintConflicts() {}

    /** Returns one human-readable message per conflict; empty when the field is satisfiable. */
    public static List<String> find(FieldConstraints c) {
        List<String> conflicts = new ArrayList<>();
        String field = "field '" + c.fieldName() + "'";

        if (c.mustBeNull() && !c.nullable()) {
            conflicts.add(field + ": @Null conflicts with @NotNull/@NotBlank/@NotEmpty");
        }
        if (c.mustBeNull() && c.fieldType().isPrimitive()) {
            conflicts.add(field + ": @Null on primitive type " + c.fieldType().getName() + " can never be satisfied");
        }
        if (c.bounds().isContradictory()) {
            conflicts.add(field + ": impossible range — lower bound " + lower(c.bounds())
                    + " is greater than upper bound " + upper(c.bounds()));
        }
        if (c.assertTrueRequired() && c.assertFalseRequired()) {
            conflicts.add(field + ": @AssertTrue conflicts with @AssertFalse");
        }
        return List.copyOf(conflicts);
    }

    private static Object lower(NumericBounds b) {
        return b instanceof NumericBounds.DecimalBounds d ? d.decMin() : b.min();
    }

    private static Object upper(NumericBounds b) {
        return b instanceof NumericBounds.DecimalBounds d ? d.decMax() : b.max();
    }
}
