package com.paramichha.datafactory.core;

import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Targets LombokBuilderStrategy — coerce(), findSetter(), toPrimitive(), toWrapper() branches.
 * Uses fixtures with primitive fields, wrapper fields, and numeric type mismatches.
 */
@DisplayName("LombokBuilderStrategy — coerce and findSetter branches")
class LombokStrategyTest {

    // ── fixtures ───────────────────────────────────────────────────────────

    private static List<FieldDescriptor> fields(Class<?> type) {
        return FieldDescriptor.extract(type);
    }

    @Value
    @Builder
    static class AllPrimitivesRequest {
        boolean boolVal;
        char charVal;
        byte byteVal;
        short shortVal;
        int intVal;
        long longVal;
        float floatVal;
        double doubleVal;
    }

    @Value
    @Builder
    static class AllWrappersRequest {
        Boolean boolVal;
        Character charVal;
        Byte byteVal;
        Short shortVal;
        Integer intVal;
        Long longVal;
        Float floatVal;
        Double doubleVal;
        BigDecimal bigDecVal;
        BigInteger bigIntVal;
    }

    // ── helpers ────────────────────────────────────────────────────────────

    static class NoBuilderClass {
        final String name;

        // plain class — no Lombok, no static builder() method
        NoBuilderClass(String name) {
            this.name = name;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // canHandle
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("canHandle")
    class CanHandle {
        @Test
        void true_for_lombok_builder() {
            assertThat(LombokBuilderStrategy.INSTANCE
                    .canHandle(AllPrimitivesRequest.class, fields(AllPrimitivesRequest.class)))
                    .isTrue();
        }

        @Test
        void false_for_no_builder() {
            assertThat(LombokBuilderStrategy.INSTANCE
                    .canHandle(NoBuilderClass.class, fields(NoBuilderClass.class)))
                    .isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // All primitive types via instantiate — hits toPrimitive all 8 arms
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("primitive fields — toPrimitive all arms")
    class PrimitiveFields {

        private AllPrimitivesRequest build(Map<String, Object> values) throws Exception {
            return LombokBuilderStrategy.INSTANCE.instantiate(
                    AllPrimitivesRequest.class, fields(AllPrimitivesRequest.class), values);
        }

        @Test
        void all_primitives_populated() throws Exception {
            var values = Map.of(
                    "boolVal", true, "charVal", 'X',
                    "byteVal", (byte) 1, "shortVal", (short) 2,
                    "intVal", 3, "longVal", 4L,
                    "floatVal", 5.0f, "doubleVal", 6.0d);
            var req = build(new java.util.LinkedHashMap<>(values));
            assertThat(req.isBoolVal()).isTrue();
            assertThat(req.getCharVal()).isEqualTo('X');
            assertThat(req.getByteVal()).isEqualTo((byte) 1);
            assertThat(req.getShortVal()).isEqualTo((short) 2);
            assertThat(req.getIntVal()).isEqualTo(3);
            assertThat(req.getLongVal()).isEqualTo(4L);
            assertThat(req.getFloatVal()).isEqualTo(5.0f);
            assertThat(req.getDoubleVal()).isEqualTo(6.0d);
        }

        @Test
        void null_for_boolean_primitive_skipped() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", null);
            values.put("charVal", 'A');
            values.put("byteVal", (byte) 0);
            values.put("shortVal", (short) 0);
            values.put("intVal", 0);
            values.put("longVal", 0L);
            values.put("floatVal", 0.0f);
            values.put("doubleVal", 0.0d);
            var req = build(values);
            assertThat(req.isBoolVal()).isFalse(); // default false, null was skipped
        }

        @Test
        void null_for_char_primitive_skipped() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", false);
            values.put("charVal", null);   // primitive — should be skipped
            values.put("byteVal", (byte) 0);
            values.put("shortVal", (short) 0);
            values.put("intVal", 0);
            values.put("longVal", 0L);
            values.put("floatVal", 0.0f);
            values.put("doubleVal", 0.0d);
            var req = build(values);
            assertThat(req.getCharVal()).isEqualTo('\0'); // default
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Wrapper types with coerce — hits toWrapper and coerce Number arms
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("wrapper fields — coerce Number arms")
    class WrapperFields {

        private AllWrappersRequest build(Map<String, Object> values) throws Exception {
            return LombokBuilderStrategy.INSTANCE.instantiate(
                    AllWrappersRequest.class, fields(AllWrappersRequest.class), values);
        }

        @Test
        void long_coerced_to_biginteger() throws Exception {
            // Simulate castNumeric returning long for BigInteger field
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'A');
            values.put("byteVal", (byte) 1);
            values.put("shortVal", (short) 1);
            values.put("intVal", 1);
            values.put("longVal", 1L);
            values.put("floatVal", 1.0f);
            values.put("doubleVal", 1.0d);
            values.put("bigDecVal", BigDecimal.ONE);
            values.put("bigIntVal", 42L);  // Long passed for BigInteger field — coerce must handle
            var req = build(values);
            assertThat(req.getBigIntVal()).isEqualTo(BigInteger.valueOf(42));
        }

        @Test
        void int_coerced_to_bigdecimal() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", false);
            values.put("charVal", 'B');
            values.put("byteVal", (byte) 2);
            values.put("shortVal", (short) 2);
            values.put("intVal", 2);
            values.put("longVal", 2L);
            values.put("floatVal", 2.0f);
            values.put("doubleVal", 2.0d);
            values.put("bigDecVal", 10L);  // Long passed for BigDecimal — coerce must handle
            values.put("bigIntVal", BigInteger.TWO);
            var req = build(values);
            assertThat(req.getBigDecVal()).isEqualByComparingTo("10");
        }

        @Test
        void coerce_long_to_byte() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'C');
            values.put("byteVal", 5L);     // Long passed for Byte — coerce
            values.put("shortVal", (short) 1);
            values.put("intVal", 1);
            values.put("longVal", 1L);
            values.put("floatVal", 1.0f);
            values.put("doubleVal", 1.0d);
            values.put("bigDecVal", BigDecimal.ONE);
            values.put("bigIntVal", BigInteger.ONE);
            var req = build(values);
            assertThat(req.getByteVal()).isEqualTo((byte) 5);
        }

        @Test
        void coerce_long_to_short() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'D');
            values.put("byteVal", (byte) 1);
            values.put("shortVal", 100L);  // Long for Short — coerce
            values.put("intVal", 1);
            values.put("longVal", 1L);
            values.put("floatVal", 1.0f);
            values.put("doubleVal", 1.0d);
            values.put("bigDecVal", BigDecimal.ONE);
            values.put("bigIntVal", BigInteger.ONE);
            var req = build(values);
            assertThat(req.getShortVal()).isEqualTo((short) 100);
        }

        @Test
        void coerce_long_to_int() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'E');
            values.put("byteVal", (byte) 1);
            values.put("shortVal", (short) 1);
            values.put("intVal", 99L);  // Long for Integer — coerce
            values.put("longVal", 1L);
            values.put("floatVal", 1.0f);
            values.put("doubleVal", 1.0d);
            values.put("bigDecVal", BigDecimal.ONE);
            values.put("bigIntVal", BigInteger.ONE);
            var req = build(values);
            assertThat(req.getIntVal()).isEqualTo(99);
        }

        @Test
        void coerce_long_to_float() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'F');
            values.put("byteVal", (byte) 1);
            values.put("shortVal", (short) 1);
            values.put("intVal", 1);
            values.put("longVal", 1L);
            values.put("floatVal", 7L);  // Long for Float — coerce
            values.put("doubleVal", 1.0d);
            values.put("bigDecVal", BigDecimal.ONE);
            values.put("bigIntVal", BigInteger.ONE);
            var req = build(values);
            assertThat(req.getFloatVal()).isEqualTo(7.0f);
        }

        @Test
        void coerce_long_to_double() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'G');
            values.put("byteVal", (byte) 1);
            values.put("shortVal", (short) 1);
            values.put("intVal", 1);
            values.put("longVal", 1L);
            values.put("floatVal", 1.0f);
            values.put("doubleVal", 8L);  // Long for Double — coerce
            values.put("bigDecVal", BigDecimal.ONE);
            values.put("bigIntVal", BigInteger.ONE);
            var req = build(values);
            assertThat(req.getDoubleVal()).isEqualTo(8.0d);
        }

        @Test
        void null_for_wrapper_allowed() throws Exception {
            var values = new java.util.LinkedHashMap<String, Object>();
            values.put("boolVal", null);    // wrapper — null is valid
            values.put("charVal", null);
            values.put("byteVal", null);
            values.put("shortVal", null);
            values.put("intVal", null);
            values.put("longVal", null);
            values.put("floatVal", null);
            values.put("doubleVal", null);
            values.put("bigDecVal", null);
            values.put("bigIntVal", null);
            var req = build(values);
            assertThat(req.getBoolVal()).isNull();
            assertThat(req.getIntVal()).isNull();
        }
    }
}
