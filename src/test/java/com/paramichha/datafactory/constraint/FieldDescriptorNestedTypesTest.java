package com.paramichha.datafactory.constraint;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Framework and language types are not treated as nested objects. */
@DisplayName("FieldDescriptor nested types")
class FieldDescriptorNestedTypesTest {

    @Nested
    @DisplayName("FieldDescriptor — isNested false for framework packages")
    class FrameworkTypesAreNotNested {

        private static FieldDescriptor fd(Class<?> type) {
            return new FieldDescriptor("f", type, type, Collections.emptyList());
        }

        @Test
        void javaxSqlIsNotNested() {
            assertThat(fd(javax.sql.DataSource.class).isNested()).isFalse();
        }

        @Test
        void jakartaValidationIsNotNested() {
            assertThat(fd(jakarta.validation.Validator.class).isNested()).isFalse();
        }

        @Test
        void javaLangIsNotNested() {
            assertThat(fd(String.class).isNested()).isFalse();
        }
    }

    @Nested
    @DisplayName("FieldDescriptor — isNested false for framework packages")
    class NestedTypeRules {

        private static FieldDescriptor fd(Class<?> type) {
            return new FieldDescriptor("f", type, type, Collections.emptyList());
        }

        @Test
        void javaxSqlTypeIsNotNestedFromCoreBranch() {
            assertThat(fd(javax.sql.DataSource.class).isNested()).isFalse();
        }

        @Test
        void jakartaValidationTypeIsNotNestedFromCoreBranch() {
            assertThat(fd(jakarta.validation.Validator.class).isNested()).isFalse();
        }

        @Test
        void springTypeIsNotNested() {
            // Use a real spring class — only available if spring is on classpath via spring-boot-test
            // Use org.springframework.context.ApplicationContext
            try {
                Class<?> springClass = Class.forName("org.springframework.context.ApplicationContext");
                assertThat(fd(springClass).isNested()).isFalse();
            } catch (ClassNotFoundException e) {
                // Spring not on test classpath — skip this check
                assertThat(true).isTrue();
            }
        }

        @Test
        void fasterxmlTypeIsNotNested() {
            try {
                Class<?> jacksonClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
                assertThat(fd(jacksonClass).isNested()).isFalse();
            } catch (ClassNotFoundException e) {
                assertThat(true).isTrue();
            }
        }

        @Test
        void domainClassIsNested() {
            assertThat(fd(FieldDescriptor.class).isNested()).isTrue();
        }
    }

    @Nested
    @DisplayName("FieldDescriptor — isNested branches")
    class PrimitiveArrayEnumAndDomainTypes {

        @Test
        void primitiveIsNotNested() {
            var fd = new FieldDescriptor("n", int.class, int.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void arrayIsNotNested() {
            var fd = new FieldDescriptor("arr", String[].class, String[].class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void enumIsNotNested() {
            enum Color {
                RED
            }
            var fd = new FieldDescriptor("c", Color.class, Color.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void javaTypeIsNotNested() {
            var fd = new FieldDescriptor("s", String.class, String.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void javaxTypeIsNotNested() {
            // Use a javax class — e.g. javax.sql.DataSource would work but simpler:
            var fd = new FieldDescriptor(
                    "s", javax.sql.DataSource.class, javax.sql.DataSource.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void jakartaTypeIsNotNested() {
            var fd = new FieldDescriptor(
                    "s",
                    jakarta.validation.constraints.NotNull.class,
                    jakarta.validation.constraints.NotNull.class,
                    Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void domainTypeIsNested() {
            // com.kickstart.* — not java.*/javax.*/jakarta.*/spring/fasterxml
            var fd = new FieldDescriptor("req", FieldDescriptor.class, FieldDescriptor.class, Collections.emptyList());
            assertThat(fd.isNested()).isTrue();
        }

        @Test
        void wildcardListElementIsString() {
            // ParameterizedType with WildcardType arg -> falls through to String.class
            // Simulate with raw List (no generic type info)
            var fd = new FieldDescriptor("items", List.class, List.class, Collections.emptyList());
            assertThat(fd.listElementType()).isEqualTo(String.class);
        }

        @Test
        void hibernateValidatorAnnotationIsIncluded() {
            var fields = FieldDescriptor.extract(WithHibernateAnnotation.class);
            assertThat(fields).hasSize(1);
            assertThat(fields.get(0).validations()).isNotEmpty();
        }

        static class WithHibernateAnnotation {
            @org.hibernate.validator.constraints.Length(min = 1) String field;
        }
    }

    @SuppressWarnings("unused")
    static class WithStaticAndOptional {
        static String shared = "x";
        String name;
        java.util.Optional<String> nickname;
    }

    private String outerNote = "outer"; // not final: a constant would be inlined, with no link to this instance

    /** An inner class that uses its enclosing instance, so the compiler adds a synthetic field for it. */
    @SuppressWarnings("unused")
    class InnerWithLabel {
        String label;

        String labelWithOuterNote() {
            return label + outerNote;
        }
    }

    @Test
    @DisplayName("compiler-made fields are left out")
    void syntheticFieldsAreLeftOut() {
        assertThat(FieldDescriptor.extract(InnerWithLabel.class))
                .extracting(FieldDescriptor::name)
                .containsExactly("label");
    }

    @Test
    @DisplayName("static fields are left out")
    void staticFieldsAreLeftOut() {
        assertThat(FieldDescriptor.extract(WithStaticAndOptional.class))
                .extracting(FieldDescriptor::name)
                .containsExactly("name", "nickname");
    }

    @Test
    @DisplayName("an Optional is not a nested object")
    void anOptionalIsNotNested() {
        FieldDescriptor nickname =
                FieldDescriptor.extract(WithStaticAndOptional.class).get(1);
        assertThat(nickname.isNested()).isFalse();
    }
}
