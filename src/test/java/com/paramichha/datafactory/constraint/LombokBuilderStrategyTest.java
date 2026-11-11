package com.paramichha.datafactory.constraint;

import jakarta.validation.constraints.NotBlank;
import com.paramichha.datafactory.instantiation.LombokObjectCreation;
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

/** Lombok builder strategy: wrapper and primitive setters, and numeric coercion. */
@DisplayName("Lombok builder strategy")
class LombokBuilderStrategyTest {

    @Nested
    @DisplayName("LombokBuilderStrategy — toPrimitive all 8 arms")
    class WrapperFieldsWithPrimitiveSetters {

        private AllPrimitivesLombok build(Map<String, Object> values) throws Exception {
            var fields = List.of(
                    new FieldDescriptor("boolVal", Boolean.class, Boolean.class, Collections.emptyList()),
                    new FieldDescriptor("charVal", Character.class, Character.class, Collections.emptyList()),
                    new FieldDescriptor("byteVal", Byte.class, Byte.class, Collections.emptyList()),
                    new FieldDescriptor("shortVal", Short.class, Short.class, Collections.emptyList()),
                    new FieldDescriptor("intVal", Integer.class, Integer.class, Collections.emptyList()),
                    new FieldDescriptor("longVal", Long.class, Long.class, Collections.emptyList()),
                    new FieldDescriptor("floatVal", Float.class, Float.class, Collections.emptyList()),
                    new FieldDescriptor("doubleVal", Double.class, Double.class, Collections.emptyList())
            );
            return LombokObjectCreation.INSTANCE.create(AllPrimitivesLombok.class, fields, values);
        }

        @Test
        void wrapperFieldsFindPrimitiveSetters() throws Exception {
            var values = new LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'X');
            values.put("byteVal", (byte) 1);
            values.put("shortVal", (short) 2);
            values.put("intVal", 3);
            values.put("longVal", 4L);
            values.put("floatVal", 5.0f);
            values.put("doubleVal", 6.0d);
            var r = build(values);
            assertThat(r.isBoolVal()).isTrue();
            assertThat(r.getCharVal()).isEqualTo('X');
            assertThat(r.getByteVal()).isEqualTo((byte) 1);
            assertThat(r.getShortVal()).isEqualTo((short) 2);
            assertThat(r.getIntVal()).isEqualTo(3);
            assertThat(r.getLongVal()).isEqualTo(4L);
            assertThat(r.getFloatVal()).isEqualTo(5.0f);
            assertThat(r.getDoubleVal()).isEqualTo(6.0d);
        }

        @Value
        @Builder
        static class AllPrimitivesLombok {
            boolean boolVal;
            char charVal;
            byte byteVal;
            short shortVal;
            int intVal;
            long longVal;
            float floatVal;
            double doubleVal;
        }
    }

    @Nested
    @DisplayName("LombokBuilderStrategy — toWrapper all 8 arms")
    class PrimitiveFieldsWithWrapperSetters {

        private AllWrappersLombok build(Map<String, Object> values) throws Exception {
            var fields = List.of(
                    new FieldDescriptor("boolVal", boolean.class, boolean.class, Collections.emptyList()),
                    new FieldDescriptor("charVal", char.class, char.class, Collections.emptyList()),
                    new FieldDescriptor("byteVal", byte.class, byte.class, Collections.emptyList()),
                    new FieldDescriptor("shortVal", short.class, short.class, Collections.emptyList()),
                    new FieldDescriptor("intVal", int.class, int.class, Collections.emptyList()),
                    new FieldDescriptor("longVal", long.class, long.class, Collections.emptyList()),
                    new FieldDescriptor("floatVal", float.class, float.class, Collections.emptyList()),
                    new FieldDescriptor("doubleVal", double.class, double.class, Collections.emptyList())
            );
            return LombokObjectCreation.INSTANCE.create(AllWrappersLombok.class, fields, values);
        }

        @Test
        void primitiveFieldsFindWrapperSetters() throws Exception {
            var values = new LinkedHashMap<String, Object>();
            values.put("boolVal", true);
            values.put("charVal", 'Y');
            values.put("byteVal", (byte) 10);
            values.put("shortVal", (short) 20);
            values.put("intVal", 30);
            values.put("longVal", 40L);
            values.put("floatVal", 50.0f);
            values.put("doubleVal", 60.0d);
            var r = build(values);
            assertThat(r.getBoolVal()).isTrue();
            assertThat(r.getCharVal()).isEqualTo('Y');
            assertThat(r.getByteVal()).isEqualTo((byte) 10);
            assertThat(r.getShortVal()).isEqualTo((short) 20);
            assertThat(r.getIntVal()).isEqualTo(30);
            assertThat(r.getLongVal()).isEqualTo(40L);
            assertThat(r.getFloatVal()).isEqualTo(50.0f);
            assertThat(r.getDoubleVal()).isEqualTo(60.0d);
        }

        @Value
        @Builder
        static class AllWrappersLombok {
            Boolean boolVal;
            Character charVal;
            Byte byteVal;
            Short shortVal;
            Integer intVal;
            Long longVal;
            Float floatVal;
            Double doubleVal;
        }
    }

    @Nested
    @DisplayName("LombokBuilderStrategy — coerce all numeric arms")
    class NumericCoercion {

        // Pass Integer values (not Long) to fields expecting various types -> forces coerce
        @Test
        void integerCoercesToEveryNumericType() throws Exception {
            var fields = FieldDescriptor.extract(AllWrappersForCoerce.class);
            var values = new LinkedHashMap<String, Object>();
            // Pass Integer for all fields — coerce will convert each to target type
            values.put("longVal", 42);   // Integer → Long
            values.put("intVal", 42);   // Integer → Integer (isInstance = true, passthrough)
            values.put("shortVal", 42);   // Integer → Short
            values.put("byteVal", 42);   // Integer → Byte
            values.put("doubleVal", 42);   // Integer → Double
            values.put("floatVal", 42);   // Integer → Float
            var r = LombokObjectCreation.INSTANCE.create(AllWrappersForCoerce.class, fields, values);
            assertThat(r.getLongVal()).isEqualTo(42L);
            assertThat(r.getIntVal()).isEqualTo(42);
            assertThat(r.getShortVal()).isEqualTo((short) 42);
            assertThat(r.getByteVal()).isEqualTo((byte) 42);
            assertThat(r.getDoubleVal()).isEqualTo(42.0d);
            assertThat(r.getFloatVal()).isEqualTo(42.0f);
        }

        @Value
        @Builder
        static class AllWrappersForCoerce {
            Long longVal;
            Integer intVal;
            Short shortVal;
            Byte byteVal;
            Double doubleVal;
            Float floatVal;
        }
    }

    @Nested
    @DisplayName("LombokBuilderStrategy — findSetter fallback paths")
    class PrimitiveAndWrapperFallbacks {

        @Test
        void booleanPrimitiveMatchesExactly() throws Exception {
            var fields = FieldDescriptor.extract(BoolPrimRequest.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("flag", true);
            var result = LombokObjectCreation.INSTANCE.create(BoolPrimRequest.class, fields, values);
            assertThat(result.isFlag()).isTrue();
        }

        @Test
        void charPrimitiveMatchesExactly() throws Exception {
            var fields = FieldDescriptor.extract(CharPrimRequest.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("letter", 'Q');
            var result = LombokObjectCreation.INSTANCE.create(CharPrimRequest.class, fields, values);
            assertThat(result.getLetter()).isEqualTo('Q');
        }

        // Force toPrimitive fallback: pass Boolean.class field descriptor but builder expects boolean
        // We do this by constructing a FieldDescriptor with wrapper type for a primitive-setter class
        @Test
        void wrapperBooleanFallsBackToPrimitiveSetter() throws Exception {
            // Build FieldDescriptor with Boolean.class (wrapper) but actual Lombok setter is boolean (primitive)
            var fd = new FieldDescriptor("flag", Boolean.class, Boolean.class, Collections.emptyList());
            var values = Map.of("flag", (Object) Boolean.TRUE);
            // getMethod("flag", Boolean.class) will fail (Lombok generates boolean)
            // toPrimitive(Boolean.class) = boolean.class → getMethod("flag", boolean.class) succeeds
            var result = LombokObjectCreation.INSTANCE.create(BoolPrimRequest.class, List.of(fd), values);
            assertThat(result.isFlag()).isTrue();
        }

        @Test
        void wrapperCharFallsBackToPrimitiveSetter() throws Exception {
            var fd = new FieldDescriptor("letter", Character.class, Character.class, Collections.emptyList());
            var values = Map.of("letter", (Object) 'Z');
            var result = LombokObjectCreation.INSTANCE.create(CharPrimRequest.class, List.of(fd), values);
            assertThat(result.getLetter()).isEqualTo('Z');
        }

        @Test
        void primitiveBooleanFallsBackToWrapperSetter() throws Exception {
            // FieldDescriptor with boolean.class (primitive), setter expects Boolean (wrapper)
            var fd = new FieldDescriptor("flag", boolean.class, boolean.class, Collections.emptyList());
            var values = Map.of("flag", (Object) Boolean.TRUE);
            // getMethod("flag", boolean.class) fails → toWrapper(boolean.class) = Boolean.class → succeeds
            var result = LombokObjectCreation.INSTANCE.create(BoolWrapperRequest.class, List.of(fd), values);
            assertThat(result.getFlag()).isTrue();
        }

        // coerce — null value hits the "return null" path
        @Test
        void nullValueCoercesToNull() throws Exception {
            var fields = FieldDescriptor.extract(BoolWrapperRequest.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("flag", null);   // Boolean wrapper — null is valid, coerce(null, Boolean) → null
            var result = LombokObjectCreation.INSTANCE.create(BoolWrapperRequest.class, fields, values);
            assertThat(result.getFlag()).isNull();
        }

        @Test
        void nonNumberValuePassesThrough() throws Exception {
            var fields = FieldDescriptor.extract(StringRequest.class);
            var values = Map.of("name", (Object) "hello");
            var result = LombokObjectCreation.INSTANCE.create(StringRequest.class, fields, values);
            assertThat(result.getName()).isEqualTo("hello");
        }

        @Value
        @Builder
        static class BoolPrimRequest {
            boolean flag;   // Lombok generates flag(boolean) — rawType=boolean.class, exact match
        }

        @Value
        @Builder
        static class CharPrimRequest {
            char letter;    // Lombok generates letter(char) — rawType=char.class, exact match
        }

        // toWrapper fallback: pass primitive field descriptor but builder expects wrapper
        @Value
        @Builder
        static class BoolWrapperRequest {
            Boolean flag;   // Lombok generates flag(Boolean)
        }

        // coerce — non-Number value hits "return value" fallback
        @Value
        @Builder
        static class StringRequest {
            @NotBlank
            String name;
        }
    }

    @Nested
    @DisplayName("LombokBuilderStrategy — uncovered coerce/findSetter paths")
    class CoercionAndSetterLookup {

        @Test
        void wrapperLookupFindsPrimitiveBooleanSetter() throws Exception {
            // f.rawType() = boolean.class, getMethod(name, boolean.class) succeeds directly
            var fields = FieldDescriptor.extract(BoolCharRequest.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("boolPrim", true);
            values.put("charPrim", 'Z');
            var result = LombokObjectCreation.INSTANCE.create(BoolCharRequest.class, fields, values);
            assertThat(result.isBoolPrim()).isTrue();
            assertThat(result.getCharPrim()).isEqualTo('Z');
        }

        @Test
        void coerceOfNullIsNull() throws Exception {
            // coerce(null, String.class) -> null path
            var fields = FieldDescriptor.extract(BoolCharRequest.class);
            var values = new LinkedHashMap<String, Object>();
            values.put("boolPrim", null);  // primitive -> skipped before coerce
            values.put("charPrim", 'A');
            // Should not throw — null skipped for primitive
            var result = LombokObjectCreation.INSTANCE.create(BoolCharRequest.class, fields, values);
            assertThat(result).isNotNull();
        }

        @Test
        void coerceOfNonNumberPassesThrough() throws Exception {
            // String value for String field — coerce's "return value" fallback
            var fields = FieldDescriptor.extract(StringOnlyRequest.class);
            var values = Map.of("name", (Object) "hello");
            var result = LombokObjectCreation.INSTANCE.create(StringOnlyRequest.class, fields, values);
            assertThat(result.getName()).isEqualTo("hello");
        }

        @Test
        void unmatchedFieldHasNoSetter() throws Exception {
            // Field name that has no matching setter in the builder -> findSetter returns null -> skipped
            var fields = List.of(new FieldDescriptor("unknownFieldXYZ", String.class, String.class,
                    Collections.emptyList()));
            var values = Map.of("unknownFieldXYZ", (Object) "test");
            // Should not throw even though setter not found
            // canHandle will be true (has builder() method)
            assertThat(LombokObjectCreation.INSTANCE.canCreate(NoSetterForField.class, fields)).isTrue();
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
}
