package com.paramichha.datafactory.constraint;

import com.paramichha.datafactory.builder.FieldValueBuilder;
import com.paramichha.datafactory.builder.ValueBuilderFactory;
import com.paramichha.datafactory.instantiation.ConstructorObjectCreation;
import com.paramichha.datafactory.instantiation.RecordObjectCreation;
import jakarta.validation.constraints.*;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Collection types and primitive defaults in the field builder. */
@DisplayName("DefaultFieldBuilder — collections and primitive defaults")
class CollectionAndPrimitiveDefaultsTest {

    @Nested
    @DisplayName("isList — Collection type")
    class CollectionDefaults {

        @Test
        void collectionTypeDefaultNullAndEmpty() {
            var cases = ((FieldValueBuilder<?>) ValueBuilderFactory.create(Collection.class)).constraintCases();
            assertThat(cases).anyMatch(c -> c.value() == null);
            assertThat(cases).anyMatch(c -> c.value() instanceof List<?> l && l.isEmpty());
        }
    }

    @Nested
    @DisplayName("defaultPrimitive — all 8 primitive types")
    class PrimitiveDefaults {

        @Test
        void allArgs_nulls_for_all_primitives_use_defaults() throws Exception {
            var fields = FieldDescriptor.extract(AllArgsClass.class);
            var values = new java.util.LinkedHashMap<String, Object>();
            fields.forEach(f -> values.put(f.name(), null));

            var obj = ConstructorObjectCreation.INSTANCE.create(
                    AllArgsClass.class, fields, values);
            assertThat(obj.boolV).isFalse();
            assertThat(obj.charV).isEqualTo('\0');
            assertThat(obj.byteV).isEqualTo((byte) 0);
            assertThat(obj.shortV).isEqualTo((short) 0);
            assertThat(obj.intV).isEqualTo(0);
            assertThat(obj.longV).isEqualTo(0L);
            assertThat(obj.floatV).isEqualTo(0.0f);
            assertThat(obj.doubleV).isEqualTo(0.0d);
        }

        @Test
        void recordNullsForAllPrimitivesUseDefaults() throws Exception {
            var fields = FieldDescriptor.extract(AllArgsRecord.class);
            var values = new java.util.LinkedHashMap<String, Object>();
            fields.forEach(f -> values.put(f.name(), null));

            var obj = RecordObjectCreation.INSTANCE.create(
                    AllArgsRecord.class, fields, values);
            assertThat(obj.boolV()).isFalse();
            assertThat(obj.charV()).isEqualTo('\0');
            assertThat(obj.byteV()).isEqualTo((byte) 0);
            assertThat(obj.shortV()).isEqualTo((short) 0);
            assertThat(obj.intV()).isEqualTo(0);
            assertThat(obj.longV()).isEqualTo(0L);
            assertThat(obj.floatV()).isEqualTo(0.0f);
            assertThat(obj.doubleV()).isEqualTo(0.0d);
        }

        static class AllArgsClass {
            final boolean boolV;
            final char charV;
            final byte byteV;
            final short shortV;
            final int intV;
            final long longV;
            final float floatV;
            final double doubleV;

            AllArgsClass(boolean boolV, char charV, byte byteV, short shortV,
                         int intV, long longV, float floatV, double doubleV) {
                this.boolV = boolV;
                this.charV = charV;
                this.byteV = byteV;
                this.shortV = shortV;
                this.intV = intV;
                this.longV = longV;
                this.floatV = floatV;
                this.doubleV = doubleV;
            }
        }

        record AllArgsRecord(boolean boolV, char charV, byte byteV, short shortV,
                             int intV, long longV, float floatV, double doubleV) {
        }
    }
}
