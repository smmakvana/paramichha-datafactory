package com.paramichha.datafactory.core;

import com.paramichha.datafactory.DataBuilder;
import com.paramichha.datafactory.analyzer.AnnotationAnalyzer;
import com.paramichha.datafactory.analyzer.FieldConstraints;

import java.util.List;

/**
 * Field-level API — produces valid values and constraint cases for a single field.
 *
 * <p>Obtain via the static factory methods:
 * <pre>
 * FieldBuilder fb = FieldBuilder.of(fieldDescriptor);
 * FieldBuilder fb = FieldBuilder.of("email", "String", List.of("@NotBlank", "@Email"));
 * </pre>
 */
public interface FieldBuilder {

    /**
     * Creates a {@code FieldBuilder} from a reflected field descriptor.
     */
    static FieldBuilder of(FieldDescriptor field) {
        return new DefaultFieldBuilder(AnnotationAnalyzer.analyze(
                field.name(), field.rawType(), field.validations()));
    }

    /**
     * Creates a {@code FieldBuilder} from annotation strings — used in codegen.
     */
    static FieldBuilder of(String fieldName, String fieldType, List<String> annotations) {
        return new DefaultFieldBuilder(AnnotationAnalyzer.analyzeFromStrings(
                fieldName, fieldType, annotations));
    }

    /**
     * Returns all valid boundary-covering values for the field.
     */
    List<Object> validValues();

    /**
     * Returns valid values as Java source-code literals.
     */
    List<String> validSourceCode();

    /**
     * Returns all constraint cases — both type-level defaults and annotation-driven.
     * Use {@link #annotationCases()} when only guaranteed Jakarta violations are needed.
     */
    List<ConstraintCase> constraintCases();

    /**
     * Returns only annotation-driven cases — guaranteed to produce a Jakarta violation.
     * This is the subset used by {@link DataBuilder#violations()}.
     */
    List<ConstraintCase> annotationCases();

    /**
     * Returns the resolved constraints for the field.
     */
    FieldConstraints constraints();
}
