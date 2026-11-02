package com.paramichha.datafactory.regression;

import com.paramichha.datafactory.DataFactory;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The discovery lists are typed by hand, so keep them honest: every name must be a real
 * Bean Validation (or Hibernate) constraint annotation, and every type must be real.
 */
class DiscoveryListRegressionTest {

    private static final String[] PACKAGES = {
            "jakarta.validation.constraints.",
            "jakarta.validation.",            // @Valid: the cascade annotation, not a constraint
            "org.hibernate.validator.constraints."
    };

    @Test
    void everySupportedAnnotationIsARealConstraintAnnotation() {
        List<String> unknown = new ArrayList<>();
        for (String name : DataFactory.supportedAnnotations()) {
            if (!isConstraintAnnotation(name)) unknown.add(name);
        }
        assertThat(unknown).as("names that are not constraint annotations").isEmpty();
    }

    @Test
    void supportedAnnotationsAreUnique() {
        List<String> names = new ArrayList<>(DataFactory.supportedAnnotations());
        assertThat(names).doesNotHaveDuplicates();
    }

    @Test
    void canHandleSupportedTypes() {
        for (Class<?> type : DataFactory.supportedTypes()) {
            assertThat(DataFactory.canHandle(type)).as(type.getName()).isTrue();
        }
    }

    private static boolean isConstraintAnnotation(String simpleName) {
        for (String pkg : PACKAGES) {
            try {
                Class<?> c = Class.forName(pkg + simpleName);
                if (c.isAnnotation()) return true;
            } catch (ClassNotFoundException ignored) {
            }
        }
        return false;
    }
}
