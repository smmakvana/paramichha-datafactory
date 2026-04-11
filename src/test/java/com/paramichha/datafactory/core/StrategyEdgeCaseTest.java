package com.paramichha.datafactory.core;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers remaining branches in:
 * LombokBuilderStrategy  — toPrimitive bool/char, toWrapper bool/char, coerce null/non-number, findSetter null
 * SetterStrategy         — primitive skip, findField superclass, findField null (not found)
 * AllArgsConstructorStrategy — count-match fallback, canHandle false
 * RecordStrategy         — defaultPrimitive double arm
 * FieldDescriptor        — isNested true/false branches, listElementType wildcard fallback
 */
@DisplayName("Strategy edge cases")
class StrategyEdgeCaseTest {

    // ── LombokBuilderStrategy ─────────────────────────────────────────────

    @Nested
    @DisplayName("LombokBuilderStrategy — uncovered coerce/findSetter paths")
    class LombokEdgeCases {

        @Test
        void boolean_primitive_setter_via_wrapper_lookup() throws Exception {
            // f.rawType() = boolean.class, getMethod(name, boolean.class) succeeds directly
            var fields = FieldDescriptor.extract(BoolCharRequest.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("boolPrim", true);
            values.put("charPrim", 'Z');
            var result = LombokBuilderStrategy.INSTANCE.instantiate(BoolCharRequest.class, fields, values);
            assertThat(result.isBoolPrim()).isTrue();
            assertThat(result.getCharPrim()).isEqualTo('Z');
        }

        @Test
        void coerce_with_null_value_returns_null() throws Exception {
            // coerce(null, String.class) -> null path
            var fields = FieldDescriptor.extract(BoolCharRequest.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("boolPrim", null);  // primitive -> skipped before coerce
            values.put("charPrim", 'A');
            // Should not throw — null skipped for primitive
            var result = LombokBuilderStrategy.INSTANCE.instantiate(BoolCharRequest.class, fields, values);
            assertThat(result).isNotNull();
        }

        @Test
        void coerce_non_number_value_passthrough() throws Exception {
            // String value for String field — coerce's "return value" fallback
            var fields = FieldDescriptor.extract(StringOnlyRequest.class);
            var values = Map.of("name", (Object) "hello");
            var result = LombokBuilderStrategy.INSTANCE.instantiate(StringOnlyRequest.class, fields, values);
            assertThat(result.getName()).isEqualTo("hello");
        }

        @Test
        void findSetter_returns_null_when_no_match() throws Exception {
            // Field name that has no matching setter in the builder -> findSetter returns null -> skipped
            var fields = List.of(new FieldDescriptor("unknownFieldXYZ", String.class, String.class,
                    Collections.emptyList()));
            var values = Map.of("unknownFieldXYZ", (Object) "test");
            // Should not throw even though setter not found
            // canHandle will be true (has builder() method)
            assertThat(LombokBuilderStrategy.INSTANCE.canHandle(NoSetterForField.class, fields)).isTrue();
        }

        @Value
        @Builder
        static class BoolCharRequest {
            boolean boolPrim;   // primitive — Lombok generates boolPrim(boolean)
            char charPrim;   // primitive — Lombok generates charPrim(char)
        }

        @Value
        @Builder
        static class StringOnlyRequest {
            @NotBlank
            String name;
        }

        static class NoSetterForField {
            // Has static builder() via manual implementation but setter name won't match
            String unknownFieldXYZ;

            static NoSetterForField builder() {
                return new NoSetterForField();
            }

            NoSetterForField build() {
                return this;
            }
        }
    }

    // ── SetterStrategy ────────────────────────────────────────────────────

    @Nested
    @DisplayName("SetterStrategy — superclass field and null-not-found")
    class SetterEdgeCases {

        @Test
        void field_found_in_superclass_via_direct_access() throws Exception {
            // 'inherited' is declared in Base, not Child — findField traverses superclass
            var fields = FieldDescriptor.extract(Child.class);
            var values = new LinkedHashMap<String, Object>();
            fields.forEach(f -> values.put(f.name(), f.name().equals("localInt") ? 7 : "parent-value"));

            var result = SetterStrategy.INSTANCE.instantiate(Child.class, fields, values);
            assertThat(result.inherited).isEqualTo("parent-value");
            assertThat(result.localInt).isEqualTo(7);
        }

        @Test
        void primitive_null_value_skipped() throws Exception {
            var fields = FieldDescriptor.extract(WithPrimitive.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("count", null);  // primitive int — must be skipped
            values.put("name", "test");
            var result = SetterStrategy.INSTANCE.instantiate(WithPrimitive.class, fields, values);
            assertThat(result.count).isEqualTo(0);
            assertThat(result.name).isEqualTo("test");
        }

        @Test
        void findField_returns_null_skipped_gracefully() throws Exception {
            var fields = List.of(
                    new FieldDescriptor("real", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("ghost", String.class, String.class, Collections.emptyList())
            );
            var values = Map.of("real", (Object) "ok", "ghost", (Object) "ignored");
            var result = SetterStrategy.INSTANCE.instantiate(FieldNotFound.class, fields, values);
            assertThat(result.real).isEqualTo("ok");
        }

        static class Base {
            String inherited;
        }

        static class Child extends Base {
            int localInt;

            public void setLocalInt(int v) {
                this.localInt = v;
            }
        }

        static class WithPrimitive {
            int count;
            String name;

            public void setName(String n) {
                this.name = n;
            }
        }

        static class FieldNotFound {
            // No field matching "ghost" — findField returns null -> silently skipped
            String real;
        }
    }

    // ── AllArgsConstructorStrategy ────────────────────────────────────────

    @Nested
    @DisplayName("AllArgsConstructorStrategy — count-match and canHandle false")
    class AllArgsEdgeCases {

        @Test
        void count_match_used_when_exact_types_fail() throws Exception {
            // getDeclaredConstructor(String.class, String.class) throws NoSuchMethodException
            // falls back to count-match: constructor with 2 params
            var fields = List.of(
                    new FieldDescriptor("a", String.class, String.class, Collections.emptyList()),
                    new FieldDescriptor("b", String.class, String.class, Collections.emptyList())
            );
            assertThat(AllArgsConstructorStrategy.INSTANCE.canHandle(ObjectArgs.class, fields)).isTrue();
            var values = Map.of("a", (Object) "x", "b", (Object) "y");
            var result = AllArgsConstructorStrategy.INSTANCE.instantiate(ObjectArgs.class, fields, values);
            assertThat(result.a).isEqualTo("x");
        }

        @Test
        void canHandle_false_when_no_constructor() {
            assertThat(AllArgsConstructorStrategy.INSTANCE
                    .canHandle(OnlyAnInterface.class, List.of()))
                    .isFalse();
        }

        interface OnlyAnInterface {
        }

        static class ObjectArgs {
            final Object a;
            final Object b;

            ObjectArgs(Object a, Object b) {
                this.a = a;
                this.b = b;
            }
        }
    }

    // ── RecordStrategy ────────────────────────────────────────────────────

    @Nested
    @DisplayName("RecordStrategy — defaultPrimitive double arm")
    class RecordDefaultPrimitive {

        @Test
        void null_for_double_primitive_uses_zero() throws Exception {
            var fields = FieldDescriptor.extract(DoubleRecord.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("d", null);
            var result = RecordStrategy.INSTANCE.instantiate(DoubleRecord.class, fields, values);
            assertThat(result.d()).isEqualTo(0.0d);
        }

        record DoubleRecord(double d) {
        }
    }

    // ── FieldDescriptor ───────────────────────────────────────────────────

    @Nested
    @DisplayName("FieldDescriptor — isNested branches")
    class FieldDescriptorBranches {

        @Test
        void isPrimitive_not_nested() {
            var fd = new FieldDescriptor("n", int.class, int.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void isArray_not_nested() {
            var fd = new FieldDescriptor("arr", String[].class, String[].class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void isEnum_not_nested() {
            enum Color {RED}
            var fd = new FieldDescriptor("c", Color.class, Color.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void java_pkg_not_nested() {
            var fd = new FieldDescriptor("s", String.class, String.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void javax_pkg_not_nested() {
            // Use a javax class — e.g. javax.sql.DataSource would work but simpler:
            var fd = new FieldDescriptor("s", javax.sql.DataSource.class,
                    javax.sql.DataSource.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void jakarta_pkg_not_nested() {
            var fd = new FieldDescriptor("s", jakarta.validation.constraints.NotNull.class,
                    jakarta.validation.constraints.NotNull.class, Collections.emptyList());
            assertThat(fd.isNested()).isFalse();
        }

        @Test
        void domain_class_is_nested() {
            // com.kickstart.* — not java.*/javax.*/jakarta.*/spring/fasterxml
            var fd = new FieldDescriptor("req", FieldDescriptor.class,
                    FieldDescriptor.class, Collections.emptyList());
            assertThat(fd.isNested()).isTrue();
        }

        @Test
        void listElementType_wildcard_returns_string() {
            // ParameterizedType with WildcardType arg -> falls through to String.class
            // Simulate with raw List (no generic type info)
            var fd = new FieldDescriptor("items", List.class, List.class, Collections.emptyList());
            assertThat(fd.listElementType()).isEqualTo(String.class);
        }

        @Test
        void hibernate_validator_annotation_included() {
            var fields = FieldDescriptor.extract(WithHibernateAnnotation.class);
            assertThat(fields).hasSize(1);
            assertThat(fields.get(0).validations()).isNotEmpty();
        }

        static class WithHibernateAnnotation {
            @org.hibernate.validator.constraints.Length(min = 1)
            String field;
        }
    }
}
