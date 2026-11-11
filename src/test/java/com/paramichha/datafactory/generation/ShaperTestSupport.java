package com.paramichha.datafactory.generation;

import com.paramichha.datafactory.constraint.AnnotationResolver;
import com.paramichha.datafactory.constraint.FieldConstraints;
import java.util.List;

/** Helpers shared by the shaper tests: building field constraints and boundary targets. */
final class ShaperTestSupport {

    private ShaperTestSupport() {}

    static FieldConstraints field(String name, Class<?> type, String... annotations) {
        return AnnotationResolver.analyzeFromStrings(name, type.getSimpleName(), List.of(annotations));
    }

    static FieldConstraints fieldOf(String name, Class<?> type) {
        return AnnotationResolver.analyzeFromStrings(name, type.getSimpleName(), List.of());
    }

    static BoundaryTarget semantic() {
        return BoundaryTarget.semantic();
    }

    static BoundaryTarget atMin(long v) {
        return BoundaryTarget.atMin(v);
    }
}
