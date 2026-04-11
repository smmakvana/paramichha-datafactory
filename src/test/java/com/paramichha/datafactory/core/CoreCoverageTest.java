package com.paramichha.datafactory.core;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 100% coverage for core package:
 * ObjectAssembler   — all strategies, no-match IllegalStateException
 * LombokBuilderStrategy
 * RecordStrategy
 * AllArgsConstructorStrategy
 * SetterStrategy
 * FieldDescriptor         — all branches
 * ConstraintCase  — typeDefault flag, all factory methods
 * FieldBuilder   — toSourceCode all types, all branches
 */
@DisplayName("Core coverage")
class CoreCoverageTest {

    // ── fixtures ───────────────────────────────────────────────────────────

    // no strategy can handle this
    interface NotInstantiable {
    }

    @Value
    @Builder
    static class LombokRequest {
        @NotBlank
        String name;
        @Min(1)
        Integer count;
    }

    record SimpleRecord(String name, Integer count) {
    }

    /**
     * Static top-level record — annotations on canonical constructor params are readable
     */
    record StaticRecord(@NotBlank String name) {
    }

    static class AllArgsClass {
        final String name;
        final Integer count;

        AllArgsClass(String name, Integer count) {
            this.name = name;
            this.count = count;
        }
    }

    static class SetterClass {
        private String name;
        private Integer count;

        public void setName(String name) {
            this.name = name;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    static class FieldAccessClass {
        String name;
        Integer count;
        // no setters — direct field access
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ObjectAssembler — strategy selection + no-match
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ObjectAssembler")
    class ObjectAssemblerCoverage {

        @Test
        void lombokBuilder_selected() throws Exception {
            List<FieldDescriptor> fields = FieldDescriptor.extract(LombokRequest.class);
            Map<String, Object> values = Map.of("name", "Alice", "count", 5);
            LombokRequest req = ObjectAssembler.instantiate(LombokRequest.class, fields, values);
            assertThat(req.getName()).isEqualTo("Alice");
            assertThat(req.getCount()).isEqualTo(5);
        }

        @Test
        void record_selected() throws Exception {
            List<FieldDescriptor> fields = FieldDescriptor.extract(SimpleRecord.class);
            Map<String, Object> values = Map.of("name", "Bob", "count", 3);
            SimpleRecord r = ObjectAssembler.instantiate(SimpleRecord.class, fields, values);
            assertThat(r.name()).isEqualTo("Bob");
        }

        @Test
        void allArgs_selected() throws Exception {
            List<FieldDescriptor> fields = FieldDescriptor.extract(AllArgsClass.class);
            Map<String, Object> values = new HashMap<>();
            values.put("name", "Charlie");
            values.put("count", 7);
            AllArgsClass obj = ObjectAssembler.instantiate(AllArgsClass.class, fields, values);
            assertThat(obj.name).isEqualTo("Charlie");
        }

        @Test
        void setter_selected() throws Exception {
            List<FieldDescriptor> fields = FieldDescriptor.extract(SetterClass.class);
            Map<String, Object> values = new HashMap<>();
            values.put("name", "Dave");
            values.put("count", 9);
            SetterClass obj = ObjectAssembler.instantiate(SetterClass.class, fields, values);
            assertThat(obj.name).isEqualTo("Dave");
        }

        @Test
        void fieldAccess_selected() throws Exception {
            List<FieldDescriptor> fields = FieldDescriptor.extract(FieldAccessClass.class);
            Map<String, Object> values = new HashMap<>();
            values.put("name", "Eve");
            values.put("count", 2);
            FieldAccessClass obj = ObjectAssembler.instantiate(FieldAccessClass.class, fields, values);
            assertThat(obj.name).isEqualTo("Eve");
        }

        @Test
        void noStrategy_throwsIllegalState() {
            assertThatThrownBy(() ->
                    ObjectAssembler.instantiate(NotInstantiable.class, List.of(), Map.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("NotInstantiable");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AllArgsConstructorStrategy — both constructor-finding paths
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("AllArgsConstructorStrategy")
    class AllArgsCoverage {

        @Test
        void exactTypeMatch() throws Exception {
            List<FieldDescriptor> fields = FieldDescriptor.extract(ExactMatch.class);
            Map<String, Object> values = Map.of("a", "x", "b", 1);
            ExactMatch obj = ObjectAssembler.instantiate(ExactMatch.class, fields, values);
            assertThat(obj.a).isEqualTo("x");
        }

        @Test
        void canHandle_true_exactMatch() {
            // ExactMatch has a 2-param constructor matching the 2 fields
            List<FieldDescriptor> fields = FieldDescriptor.extract(ExactMatch.class);
            assertThat(AllArgsConstructorStrategy.INSTANCE.canHandle(ExactMatch.class, fields)).isTrue();
        }

        @Test
        void canHandle_false_noMatchingConstructor() {
            // AllArgsClass has one 2-param constructor, but with 0 fields
            // getDeclaredConstructor() with 0 types fails, and count-match finds no 0-param constructor
            assertThat(AllArgsConstructorStrategy.INSTANCE.canHandle(AllArgsClass.class, List.of())).isFalse();
        }

        static class ExactMatch {
            final String a;
            final Integer b;

            ExactMatch(String a, Integer b) {
                this.a = a;
                this.b = b;
            }
        }

        static class CountMatch {
            final Object a;
            final Object b;

            CountMatch(Object a, Object b) {
                this.a = a;
                this.b = b;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SetterStrategy — primitive field skip + field access fallback
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SetterStrategy")
    class SetterStrategyCoverage {

        @Test
        void primitiveNull_skipped() throws Exception {
            List<FieldDescriptor> fields = FieldDescriptor.extract(WithPrimitive.class);
            Map<String, Object> values = new HashMap<>();
            values.put("count", null);   // null for primitive — must be skipped
            values.put("name", "test");
            WithPrimitive obj = ObjectAssembler.instantiate(WithPrimitive.class, fields, values);
            assertThat(obj.name).isEqualTo("test");
            assertThat(obj.count).isEqualTo(0); // default, not crashed
        }

        @Test
        void canHandle_false_whenNoNoArgCtor() {
            assertThat(SetterStrategy.INSTANCE.canHandle(AllArgsClass.class, List.of()))
                    .isFalse();
        }

        static class WithPrimitive {
            int count;
            String name;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FieldDescriptor — record vs class extraction, isNested, isList branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("FieldDescriptor")
    class FieldDescriptorCoverage {

        @Test
        void extract_record() {
            List<FieldDescriptor> fields = FieldDescriptor.extract(StaticRecord.class);
            assertThat(fields).hasSize(1);
            assertThat(fields.get(0).name()).isEqualTo("name");
            assertThat(fields.get(0).hasValidations()).isTrue();
        }

        @Test
        void extract_class() {
            List<FieldDescriptor> fields = FieldDescriptor.extract(LombokRequest.class);
            assertThat(fields).isNotEmpty();
        }

        @Test
        void isNested_true_for_domainClass() {
            List<FieldDescriptor> fields = FieldDescriptor.extract(WithList.class);
            FieldDescriptor nestedField = fields.stream()
                    .filter(f -> f.name().equals("nested")).findFirst().orElseThrow();
            assertThat(nestedField.isList()).isTrue();
            assertThat(nestedField.listElementType()).isEqualTo(LombokRequest.class);
        }

        @Test
        void isList_true_for_list() {
            List<FieldDescriptor> fields = FieldDescriptor.extract(WithList.class);
            FieldDescriptor tagsField = fields.stream()
                    .filter(f -> f.name().equals("tags")).findFirst().orElseThrow();
            assertThat(tagsField.isList()).isTrue();
            assertThat(tagsField.listElementType()).isEqualTo(String.class);
        }

        @Test
        void isNested_false_for_string() {
            List<FieldDescriptor> fields = FieldDescriptor.extract(LombokRequest.class);
            FieldDescriptor nameField = fields.stream()
                    .filter(f -> f.name().equals("name")).findFirst().orElseThrow();
            assertThat(nameField.isNested()).isFalse();
        }

        @Test
        void annotation_and_hasAnnotation() {
            List<FieldDescriptor> fields = FieldDescriptor.extract(LombokRequest.class);
            FieldDescriptor nameField = fields.stream()
                    .filter(f -> f.name().equals("name")).findFirst().orElseThrow();
            assertThat(nameField.hasAnnotation(NotBlank.class)).isTrue();
            assertThat(nameField.annotation(NotBlank.class)).isNotNull();
            assertThat(nameField.hasAnnotation(NotNull.class)).isFalse();
            assertThat(nameField.annotation(NotNull.class)).isNull();
        }

        @Test
        void isEnum_true() {
            enum Status {A, B}
            // FieldDescriptor for enum field
            var field = new FieldDescriptor("s", Status.class, Status.class,
                    Collections.emptyList());
            assertThat(field.isEnum()).isTrue();
        }

        @Test
        void listElementType_noGeneric_defaultsToString() {
            // Raw List field — no generic type info
            var field = new FieldDescriptor("tags", List.class, List.class,
                    Collections.emptyList());
            assertThat(field.listElementType()).isEqualTo(String.class);
        }

        static class WithList {
            List<String> tags;
            List<LombokRequest> nested;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ConstraintCase — typeDefault flag and factory methods
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ConstraintCase")
    class ConstraintCaseCoverage {

        @Test
        void of_isNotTypeDefault() {
            var c = ConstraintCase.of("f", "NotNull", "f_null", null, "null");
            assertThat(c.typeDefault()).isFalse();
            assertThat(c.isAnnotationDriven()).isTrue();
        }

        @Test
        void typeDefault_isTypeDefault() {
            var c = ConstraintCase.typeDefault("f", "Null", "f_null", null, "null");
            assertThat(c.typeDefault()).isTrue();
            assertThat(c.isAnnotationDriven()).isFalse();
        }

        @Test
        void testMethodName() {
            var c = ConstraintCase.of("email", "Email", "email_invalidFormat", "x", "\"x\"");
            assertThat(c.testMethodName()).isEqualTo("email_invalidFormat_shouldFailValidation");
        }

        @Test
        void allAccessors() {
            var c = ConstraintCase.of("age", "Min", "age_belowMin", 17, "17");
            assertThat(c.fieldName()).isEqualTo("age");
            assertThat(c.constraint()).isEqualTo("Min");
            assertThat(c.testNameSuffix()).isEqualTo("age_belowMin");
            assertThat(c.value()).isEqualTo(17);
            assertThat(c.sourceCode()).isEqualTo("17");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FieldBuilder — toSourceCode all branches
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("FieldBuilder — toSourceCode all types")
    class FieldBuilderSourceCode {

        private List<String> codeFor(String name, String type, String... annotations) {
            return FieldBuilder.of(name, type, List.of(annotations)).validSourceCode();
        }

        @Test
        void string_quoted() {
            codeFor("name", "String", "@NotBlank").forEach(s -> assertThat(s).startsWith("\""));
        }

        @Test
        void long_suffixed() {
            codeFor("n", "Long", "@Min(1)", "@Max(100)").forEach(s -> assertThat(s).endsWith("L"));
        }

        @Test
        void float_suffixed() {
            codeFor("n", "Float", "@Min(1)", "@Max(10)").forEach(s -> assertThat(s).endsWith("f"));
        }

        @Test
        void bigDecimal_newBigDecimal() {
            codeFor("price", "BigDecimal", "@Min(1)").forEach(s -> assertThat(s).contains("BigDecimal"));
        }

        @Test
        void instant_sourceCode() {
            var ftv = FieldBuilder.of("ts", "Instant", List.of("@Past"));
            assertThat(ftv.validSourceCode()).allMatch(s -> s.contains("Instant"));
        }

        @Test
        void localDate_sourceCode() {
            var ftv = FieldBuilder.of("dob", "LocalDate", List.of("@Past"));
            assertThat(ftv.validSourceCode()).allMatch(s -> s.contains("LocalDate"));
        }

        @Test
        void localDateTime_sourceCode() {
            var ftv = FieldBuilder.of("dt", "LocalDateTime", List.of("@Past"));
            assertThat(ftv.validSourceCode()).allMatch(s -> s.contains("LocalDateTime"));
        }

        @Test
        void zonedDateTime_sourceCode() {
            var ftv = FieldBuilder.of("dt", "ZonedDateTime", List.of("@Past"));
            assertThat(ftv.validSourceCode()).allMatch(s -> s.contains("ZonedDateTime"));
        }

        @Test
        void offsetDateTime_sourceCode() {
            var ftv = FieldBuilder.of("dt", "OffsetDateTime", List.of("@Past"));
            assertThat(ftv.validSourceCode()).allMatch(s -> s.contains("OffsetDateTime"));
        }

        @Test
        void uuid_sourceCode() {
            var ftv = FieldBuilder.of("id", "UUID", List.of());
            assertThat(ftv.validSourceCode()).allMatch(s -> s.contains("UUID"));
        }

        @Test
        void null_sourceCode() {
            var ftv = FieldBuilder.of("name", "String", List.of());
            assertThat(ftv.constraintCases()).anyMatch(c -> "null".equals(c.sourceCode()));
        }

        @Test
        void emptyList_sourceCode() {
            var ftv = FieldBuilder.of("items", "List", List.of());
            assertThat(ftv.constraintCases()).anyMatch(c -> "List.of()".equals(c.sourceCode()));
        }

        @Test
        void integer_noSuffix() {
            codeFor("n", "Integer", "@Min(1)", "@Max(10)").forEach(s ->
                    assertThat(s).doesNotEndWith("L").doesNotEndWith("f"));
        }

        @Test
        void boolean_sourceCode() {
            var ftv = FieldBuilder.of("active", "Boolean", List.of("@AssertTrue"));
            assertThat(ftv.validSourceCode()).contains("true");
        }

        @Test
        void invalidValues_convenience() {
            var ftv = FieldBuilder.of("email", "String", List.of("@NotBlank", "@Email"));
            assertThat(ftv.constraintCases().stream().map(ConstraintCase::value).toList()).isNotEmpty();
        }

        @Test
        void analyzed_accessor() {
            var ftv = FieldBuilder.of("name", "String", List.of("@NotBlank"));
            assertThat(ftv.constraints()).isNotNull();
            assertThat(ftv.constraints().fieldName()).isEqualTo("name");
        }

        @Test
        void castNumeric_bigInteger() {
            // BigInteger path in castNumeric — hit via invalidCases with @Min on BigInteger field
            var ftv = FieldBuilder.of("n", "BigInteger", List.of("@Min(5)", "@Max(100)"));
            assertThat(ftv.constraintCases()).anyMatch(c -> c.constraint().equals("Min"));
        }

        @Test
        void futureTemporal_allTypes() {
            // @Past fields → futureTemporal called for each temporal type
            for (String type : List.of("LocalDate", "LocalDateTime", "ZonedDateTime", "OffsetDateTime", "Instant")) {
                var ftv = FieldBuilder.of("dt", type, List.of("@Past"));
                assertThat(ftv.constraintCases())
                        .anyMatch(c -> c.constraint().equals("Past"));
            }
        }

        @Test
        void pastTemporal_allTypes() {
            // @Future fields → pastTemporal called for each temporal type
            for (String type : List.of("LocalDate", "LocalDateTime", "ZonedDateTime", "OffsetDateTime", "Instant")) {
                var ftv = FieldBuilder.of("dt", type, List.of("@Future"));
                assertThat(ftv.constraintCases())
                        .anyMatch(c -> c.constraint().equals("Future"));
            }
        }

        @Test
        void sizeMin_zero_noTooShort() {
            // min=0 → tooShort case skipped (min > 0 guard)
            var ftv = FieldBuilder.of("name", "String", List.of("@Size(min=0, max=10)"));
            assertThat(ftv.constraintCases())
                    .noneMatch(c -> c.testNameSuffix().contains("tooShort"));
        }

        @Test
        void isList_collection_type() {
            // Collection field → isList = true
            var ftv = FieldBuilder.of("items", "Collection", List.of());
            assertThat(ftv.constraintCases()).isNotEmpty();
        }
    }
}
